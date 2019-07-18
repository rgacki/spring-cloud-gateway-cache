package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

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

import static java.util.Objects.requireNonNull;

public class InMemoryStore implements Store {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryStore.class);

  private final Map<SHA2CacheKeyBuilder.SHA2CacheKey, Bag> bags;
  private final CacheKeyProducer cacheKeyProducer;
  private final InMemoryStoreConfiguration configuration;

  InMemoryStore(final CacheKeyProducer cacheKeyProducer,
                final InMemoryStoreConfiguration configuration) {
    Objects.requireNonNull(cacheKeyProducer, "A cache key producer is required!");
    Objects.requireNonNull(configuration, "A configuration is required!");
    this.cacheKeyProducer = cacheKeyProducer;
    this.bags = new ConcurrentHashMap<>();
    this.configuration = configuration;
  }

  private SHA2CacheKeyBuilder.SHA2CacheKey createKey(final ServerHttpRequest request) {
    final SHA2CacheKeyBuilder cacheKeyBuilder = new SHA2CacheKeyBuilder();
    cacheKeyProducer.createKey(request, cacheKeyBuilder);
    return cacheKeyBuilder.build();
  }

  @Override
  public Optional<? extends Entry> find(final ServerHttpRequest request) {
    requireNonNull(request, "'request' must not be null!");

    final SHA2CacheKeyBuilder.SHA2CacheKey key = createKey(request);
    final Bag bag = bags.get(key);

    if (bag == null) {
      return Optional.empty();
    }

    if (bag instanceof NegotiatedRepresentationBag) {
      final NegotiatedRepresentationBag negotiatedBag = ((NegotiatedRepresentationBag) bag);
      final NegotiatedRepresentation representation = negotiatedBag.find(request).orElse(null);
      if (representation != null) {
        return Optional.of(new InMemoryEntry(bag, representation));
      }
    } else if (bag instanceof SimpleRepresentationBag) {
      return Optional.of(new InMemoryEntry(bag, ((SimpleRepresentationBag) bag).representation));
    }

    return Optional.empty();
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

    final byte[] copyBuffer = new byte[1024 * 8];

    final ServerHttpResponseDecorator decorator = new ServerHttpResponseDecorator(exchange.getResponse()) {

      @Override
      public Mono<Void> writeWith(final Publisher<? extends DataBuffer> body) {

        final Flux<? extends DataBuffer> flux = asFlux(body)
          .map(buffer -> {

            if (sink.dirty) {
              return buffer;
            }

            final int initialReadPosition = buffer.readPosition();
            int lastReadPosition = initialReadPosition;
            while (buffer.readableByteCount() > 0) {
              buffer.read(copyBuffer);
              final int currentReadPosition = buffer.readPosition();
              final int bytesRead = currentReadPosition - lastReadPosition;
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
    if (vary.isEmpty()) {
      createSimpleEntry(exchange, sink);
    } else {
      createNegotiatedEntry(exchange, sink, vary);
    }
  }

  private void createSimpleEntry(final ServerWebExchange exchange,
                                 final PayloadSink sink) {
    final SHA2CacheKeyBuilder.SHA2CacheKey key = createKey(exchange.getRequest());
    final Representation entry = new Representation(
      responseHeaders(exchange.getResponse()),
      sink.outputStream.getFrames(),
      sink.outputStream.size()
    );
    final SimpleRepresentationBag bag = new SimpleRepresentationBag(exchange.getRequest(), entry);
    bags.put(key, bag);
  }

  private void createNegotiatedEntry(final ServerWebExchange exchange,
                                     final PayloadSink sink,
                                     final List<String> vary) {
    final SHA2CacheKeyBuilder.SHA2CacheKey key = createKey(exchange.getRequest());

    final HttpHeaders varyHeaders = new HttpHeaders();
    vary.forEach(header -> varyHeaders.addAll(header, exchange.getRequest().getHeaders().get(header)));

    final NegotiatedRepresentation entry = new NegotiatedRepresentation(
      responseHeaders(exchange.getResponse()),
      sink.outputStream.getFrames(),
      sink.outputStream.size(),
      varyHeaders
    );

    bags.compute(key, (existingKey, existingBag) -> {
      if (!(existingBag instanceof NegotiatedRepresentationBag)) {
        return new NegotiatedRepresentationBag(
          exchange.getRequest(),
          new LinkedHashSet<>(vary)
        ).add(entry);
      }
      return ((NegotiatedRepresentationBag) existingBag).add(entry);
    });
  }

  private static class InMemoryEntry implements Entry {

    private final Bag bag;
    private final Representation representation;

    private InMemoryEntry(final Bag bag,
                          final Representation representation) {
      this.bag = bag;
      this.representation = representation;
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

    private Representation(final HttpHeaders responseHeaders,
                           final List<byte[]> payload,
                           final long size) {
      requireNonNull(responseHeaders, "'responseHeaders' must not be null!");
      requireNonNull(payload, "'payload' must not be null!");
      this.responseHeaders = responseHeaders;
      this.payload = payload;
      this.size = size;
    }

    boolean matches(final ServerHttpRequest request) {
      return true;
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

    private final String method;
    private final String host;
    private final String path;
    private final String query;

    Bag(final ServerHttpRequest request) {
      this.method = request.getMethodValue();
      this.host = request.getHeaders().getFirst("Host");
      this.path = request.getURI().getPath();
      this.query = request.getURI().getQuery();
    }

    abstract Optional<? extends Representation> find(ServerHttpRequest request);

  }

  private static class SimpleRepresentationBag extends Bag {

    private final Representation representation;

    private SimpleRepresentationBag(final ServerHttpRequest request,
                                    final Representation representation) {
      super(request);
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

    private NegotiatedRepresentationBag(final ServerHttpRequest request,
                                        final Set<String> varyHeaders) {
      super(request);
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
