package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKey;
import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.store.Entry;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class InMemoryStore implements Store {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryStore.class);

  private final Map<SHA2CacheKeyBuilder.SHA2CacheKey, Bag> bags;
  private final CacheKeyProducer cacheKeyProducer;
  private final InMemoryStoreConfiguration configuration;

  private final InMemoryEvents events;
  private final InMemoryMetrics metrics;

  InMemoryStore(final CacheKeyProducer cacheKeyProducer,
                final InMemoryStoreConfiguration configuration) {
    Objects.requireNonNull(cacheKeyProducer, "A cache key producer is required!");
    Objects.requireNonNull(configuration, "A configuration is required!");
    this.cacheKeyProducer = cacheKeyProducer;
    this.bags = new ConcurrentHashMap<>();
    this.configuration = configuration;
    this.events = new InMemoryEvents();
    this.metrics = new InMemoryMetrics();
  }

  @Override
  public InMemoryEvents events() {
    return events;
  }

  @Override
  public InMemoryMetrics metrics() {
    return metrics;
  }

  private SHA2CacheKeyBuilder.SHA2CacheKey createKey(final ServerHttpRequest request) {
    final SHA2CacheKeyBuilder cacheKeyBuilder = new SHA2CacheKeyBuilder();
    cacheKeyProducer.createKey(request, cacheKeyBuilder);
    return cacheKeyBuilder.build();
  }

  @Override
  public Optional<? extends Entry> find(final ServerHttpRequest request) {
    requireNonNull(request, "'request' must not be null!");

    final SHA2CacheKeyBuilder.SHA2CacheKey cacheKey = createKey(request);
    final Bag bag = bags.get(cacheKey);

    if (bag == null) {
      return Optional.empty();
    }

    if (bag instanceof NegotiatedRepresentationBag) {
      final NegotiatedRepresentationBag negotiatedBag = ((NegotiatedRepresentationBag) bag);
      final NegotiatedRepresentation representation = negotiatedBag.find(request).orElse(null);
      if (representation != null && representation.isValid()) {
        return Optional.of(new InMemoryEntry(this, cacheKey, bag, representation));
      }
    } else if (bag instanceof SimpleRepresentationBag && ((SimpleRepresentationBag) bag).representation.isValid()) {
      return Optional.of(new InMemoryEntry(this, cacheKey, bag, ((SimpleRepresentationBag) bag).representation));
    }

    return Optional.empty();
  }

  @Override
  public Stream<InMemoryEntry> find(final String cacheKey) {
    Objects.requireNonNull(cacheKey, "CacheKey must not be null!");
    try {
      return find(SHA2CacheKeyBuilder.parse(cacheKey));
    } catch (final IllegalArgumentException e) {
      LOG.debug("Invalid cache key [{}]!", cacheKey, e);
      return Stream.empty();
    }
  }

  @Override
  public Stream<InMemoryEntry> find(final CacheKey cacheKey) {
    Objects.requireNonNull(cacheKey, "CacheKey must not be null!");
    if (cacheKey instanceof SHA2CacheKeyBuilder.SHA2CacheKey) {
      return find(((SHA2CacheKeyBuilder.SHA2CacheKey) cacheKey));
    }
    return Stream.empty();
  }

  private Stream<InMemoryEntry> find(final SHA2CacheKeyBuilder.SHA2CacheKey cacheKey) {
    Objects.requireNonNull(cacheKey, "CacheKey must not be null!");

    final Bag bag = bags.get(cacheKey);

    if (bag == null) {
      return Stream.empty();
    }

    if (bag instanceof NegotiatedRepresentationBag) {
      final NegotiatedRepresentationBag negotiatedBag = ((NegotiatedRepresentationBag) bag);
      return negotiatedBag.entries.stream()
        .map(representation -> new InMemoryEntry(this, cacheKey, negotiatedBag, representation));
    } else if (bag instanceof SimpleRepresentationBag && ((SimpleRepresentationBag) bag).representation.isValid()) {
      return Stream.of(new InMemoryEntry(this, cacheKey, bag, ((SimpleRepresentationBag) bag).representation));
    }

    return Stream.empty();
  }

  @SuppressWarnings("unchecked")
  private static Flux<? extends DataBuffer> asFlux(final Publisher<? extends DataBuffer> publisher) {
    if (publisher instanceof Flux) {
      return ((Flux<? extends DataBuffer>) publisher);
    } else if (publisher instanceof Mono) {
      return ((Mono<? extends DataBuffer>) publisher).flux();
    }
    throw new IllegalArgumentException(String.format("Type of [%s] is not supported!", publisher));
  }

  @Override
  public ServerWebExchange write(final ServerWebExchange exchange) {
    requireNonNull(exchange, "'exchange' must not be null!");

    final long contentLength = exchange.getResponse().getHeaders().getContentLength();
    final long initialBufferSize = contentLength < 1L
      ? configuration.getInitialBufferMinimumSize()
      : Math.max(contentLength, configuration.getInitialBufferMaximumSize());

    final FrameOutputStream outputStream = new FrameOutputStream(initialBufferSize);

    final PayloadSink sink = new PayloadSink(
      outputStream
    );

    final boolean traceEnabled = LOG.isTraceEnabled();

    final byte[] copyBuffer = new byte[8 * 1024];

    final ServerHttpResponseDecorator decorator = new ServerHttpResponseDecorator(exchange.getResponse()) {

      @Override
      public Mono<Void> writeWith(final Publisher<? extends DataBuffer> body) {

        final Flux<? extends DataBuffer> flux = asFlux(body)
          .map(buffer -> {

            if (sink.dirty) {
              return buffer;
            }

            if (traceEnabled) {
              LOG.trace("initial state - readPos: {}, writePos: {}, readable: {}", buffer.readPosition(), buffer.writePosition(), buffer.readableByteCount());
            }

            final int initialReadPosition = buffer.readPosition();

            int lastReadPosition = initialReadPosition;
            int readableByteCount = buffer.readableByteCount();

            while (readableByteCount > 0) {

              if (traceEnabled) {
                LOG.trace("loop - readPos: {}, writePos: {}, readable: {}", buffer.readPosition(), buffer.writePosition(), buffer.readableByteCount());
              }

              buffer.read(copyBuffer, 0, Math.min(readableByteCount, copyBuffer.length));

              final int currentReadPosition = buffer.readPosition();
              final int bytesRead = currentReadPosition - lastReadPosition;
              readableByteCount = readableByteCount - bytesRead;

              try {
                sink.outputStream.write(copyBuffer, 0, bytesRead);
              } catch (IOException e) {
                LOG.error("Failed to write to sink for exchange [{}]!", exchange, e);
                sink.dirty = true;
                return buffer.readPosition(initialReadPosition);
              }

              lastReadPosition = currentReadPosition;
            }

            return buffer.readPosition(initialReadPosition);
          })
          .doFinally(signalType -> {
            try {
              sink.outputStream.close();
            } catch (IOException e) {
              LOG.error("Failed to close sink for exchange [{}]!", exchange, e);
            }
            if (!sink.dirty) {
              try {
                createEntry(exchange, sink);
              } catch (final Exception e) {
                LOG.error("Failed to create cache representation for exchange [{}]!", exchange, e);
              }
            } else {
              LOG.debug("Sink is dirty for exchange [{}].", exchange);
            }
          });

        return super.writeWith(flux);
      }
    };

    return exchange.mutate().response(decorator).build();
  }

  private HttpHeaders responseHeaders(final ServerHttpResponse response) {
    return HttpHeaders.readOnlyHttpHeaders(response.getHeaders());
  }

  private void createEntry(final ServerWebExchange exchange,
                           final PayloadSink sink) {
    final List<String> vary = exchange.getResponse().getHeaders().getVary();
    final InMemoryEntry entry;
    if (vary.isEmpty()) {
      entry = createSimpleEntry(exchange, sink);
    } else {
      entry = createNegotiatedEntry(exchange, sink, vary);
    }

    events().publishResourceCached(exchange, entry);
  }

  private InMemoryEntry createSimpleEntry(final ServerWebExchange exchange,
                                          final PayloadSink sink) {
    final SHA2CacheKeyBuilder.SHA2CacheKey key = createKey(exchange.getRequest());
    final Representation representation = new Representation(
      responseHeaders(exchange.getResponse()),
      sink.outputStream.getFrames(),
      sink.outputStream.size()
    );
    final SimpleRepresentationBag bag = new SimpleRepresentationBag(key, exchange.getRequest(), representation);
    bags.put(key, bag);
    return new InMemoryEntry(this, key, bag, representation);
  }

  private InMemoryEntry createNegotiatedEntry(final ServerWebExchange exchange,
                                              final PayloadSink sink,
                                              final List<String> vary) {
    final SHA2CacheKeyBuilder.SHA2CacheKey key = createKey(exchange.getRequest());

    final HttpHeaders varyHeaders = new HttpHeaders();
    vary.forEach(header -> varyHeaders.addAll(header, exchange.getRequest().getHeaders().get(header)));

    final NegotiatedRepresentation representation = new NegotiatedRepresentation(
      responseHeaders(exchange.getResponse()),
      sink.outputStream.getFrames(),
      sink.outputStream.size(),
      varyHeaders
    );

    final Bag bag = bags.compute(key, (existingKey, existingBag) -> {
      if (!(existingBag instanceof NegotiatedRepresentationBag)) {
        return new NegotiatedRepresentationBag(
          key,
          exchange.getRequest(),
          new LinkedHashSet<>(vary)
        ).add(representation);
      }
      return ((NegotiatedRepresentationBag) existingBag).add(representation);
    });

    return new InMemoryEntry(this, key, bag, representation);
  }

  private void remove(final Bag bag, final Representation representation) {

  }

  private static class InMemoryEntry implements Entry {

    private final SHA2CacheKeyBuilder.SHA2CacheKey cacheKey;
    private final InMemoryStore store;
    private final Bag bag;
    private final Representation representation;

    private InMemoryEntry(final InMemoryStore store,
                          final SHA2CacheKeyBuilder.SHA2CacheKey cacheKey,
                          final Bag bag,
                          final Representation representation) {
      this.cacheKey = cacheKey;
      this.store = store;
      this.bag = bag;
      this.representation = representation;
    }

    @Override
    public CacheKey getKey() {
      return cacheKey;
    }

    @Override
    public String getHttpMethod() {
      return bag.method;
    }

    @Override
    public Optional<String> getHost() {
      return Optional.ofNullable(bag.host);
    }

    @Override
    public String getPath() {
      return bag.path;
    }

    @Override
    public Optional<String> getQuery() {
      return Optional.ofNullable(bag.query);
    }

    @Override
    public HttpHeaders getRequestHeaders() {
      if (representation instanceof NegotiatedRepresentation) {
        return ((NegotiatedRepresentation) representation).varyRequestHeaders;
      }
      return HttpHeaders.EMPTY;
    }

    @Override
    public HttpHeaders getResponseHeaders() {
      return representation.responseHeaders;
    }

    @Override
    public InputStream openStream() throws IOException {
      return null;
    }

    @Override
    public long size() {
      return representation.size;
    }

    @Override
    public void invalidate() {
      representation.invalidate();
      store.remove(bag, representation);
    }

    @Override
    public String toString() {
      return "InMemoryEntry{" +
        "bag=" + bag +
        ", representation=" + representation +
        '}';
    }
  }

  private static class PayloadSink {

    private final FrameOutputStream outputStream;
    private boolean dirty;

    private PayloadSink(final FrameOutputStream frameOutputStream) {
      this.outputStream = frameOutputStream;
      this.dirty = false;
    }
  }

  private static class Representation {

    private final HttpHeaders responseHeaders;
    private final List<byte[]> payload;
    private final long size;
    private final AtomicBoolean invalidated;

    private Representation(final HttpHeaders responseHeaders,
                           final List<byte[]> payload,
                           final long size) {
      requireNonNull(responseHeaders, "'responseHeaders' must not be null!");
      requireNonNull(payload, "'payload' must not be null!");
      this.responseHeaders = responseHeaders;
      this.payload = payload;
      this.size = size;
      this.invalidated = new AtomicBoolean(false);
    }

    void invalidate() {
      invalidated.compareAndSet(false, true);
    }

    boolean isValid() {
      return !invalidated.get();
    }

    boolean matches(final ServerHttpRequest request) {
      return true;
    }

    @Override
    public String toString() {
      return "Representation{" +
        "size=" + size +
        ", invalidated=" + invalidated +
        '}';
    }
  }

  private static class NegotiatedRepresentation extends Representation {

    private final HttpHeaders varyRequestHeaders;

    private NegotiatedRepresentation(final HttpHeaders responseHeaders,
                                     final List<byte[]> payload,
                                     final long size,
                                     final HttpHeaders varyRequestHeaders) {
      super(responseHeaders, payload, size);
      requireNonNull(varyRequestHeaders, "'varyRequestHeaders' must not be null!");
      this.varyRequestHeaders = varyRequestHeaders;
    }

    boolean matches(final ServerHttpRequest request) {
      final HttpHeaders requestHeaders = request.getHeaders();
      for (final Map.Entry<String, List<String>> varyHeader : varyRequestHeaders.entrySet()) {
        final List<String> requestHeaderValues = requestHeaders.get(varyHeader.getKey());
        if (requestHeaderValues == null || requestHeaderValues.isEmpty()) {
          return false;
        }
        if (varyHeader.getValue().size() != requestHeaderValues.size()) {
          return false;
        }
        if (varyHeader.getValue().size() == 1) {
          if (!varyHeader.getValue().get(0).equals(requestHeaderValues.get(0))) {
            return false;
          }
        } else {
          final List<String> leftList = new ArrayList<>(varyHeader.getValue());
          final List<String> rightList = new ArrayList<>(requestHeaderValues);
          final Iterator<String> leftListIterator = leftList.iterator();
          while (leftListIterator.hasNext()) {
            if (!rightList.remove(leftListIterator.next())) {
              return false;
            }
            leftListIterator.remove();
          }
          if (!rightList.isEmpty()) {
            return false;
          }
        }
      }
      return true;
    }

  }

  private static abstract class Bag {

    private final SHA2CacheKeyBuilder.SHA2CacheKey key;
    private final String method;
    private final String host;
    private final String path;
    private final String query;

    Bag(final SHA2CacheKeyBuilder.SHA2CacheKey key,
        final ServerHttpRequest request) {
      this.key = key;
      this.method = request.getMethodValue();
      this.host = request.getHeaders().getFirst("Host");
      this.path = request.getURI().getPath();
      this.query = request.getURI().getQuery();
    }

    public SHA2CacheKeyBuilder.SHA2CacheKey getKey() {
      return key;
    }

    abstract Optional<? extends Representation> find(ServerHttpRequest request);

  }

  private static class SimpleRepresentationBag extends Bag {

    private final Representation representation;

    private SimpleRepresentationBag(final SHA2CacheKeyBuilder.SHA2CacheKey key,
                                    final ServerHttpRequest request,
                                    final Representation representation) {
      super(key, request);
      this.representation = representation;
    }

    @Override
    Optional<? extends Representation> find(final ServerHttpRequest request) {
      return Optional.of(representation);
    }
  }

  private static class NegotiatedRepresentationBag extends Bag {

    private final Set<String> varyHeaders;
    private final List<NegotiatedRepresentation> entries;

    private NegotiatedRepresentationBag(final SHA2CacheKeyBuilder.SHA2CacheKey key,
                                        final ServerHttpRequest request,
                                        final Set<String> varyHeaders) {
      super(key, request);
      this.varyHeaders = varyHeaders;
      this.entries = new CopyOnWriteArrayList<>();
    }

    @Override
    Optional<? extends NegotiatedRepresentation> find(final ServerHttpRequest request) {
      return entries.stream().filter(entry -> entry.matches(request)).findAny();
    }

    boolean sameVary(final Set<String> other) {
      return
        other.size() == varyHeaders.size()
          && varyHeaders.containsAll(other)
          && other.containsAll(varyHeaders);
    }

    NegotiatedRepresentationBag add(final NegotiatedRepresentation entry) {
      // TODO: Match existing representation!
      this.entries.add(entry);
      return this;
    }
  }

}
