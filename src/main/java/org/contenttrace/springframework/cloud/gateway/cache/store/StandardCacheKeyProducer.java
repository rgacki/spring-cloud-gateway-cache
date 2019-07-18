package org.contenttrace.springframework.cloud.gateway.cache.store;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public abstract class StandardCacheKeyProducer implements CacheKeyProducer {

  private static final byte[] METHOD_FIELD = "method=".getBytes(StandardCharsets.UTF_8);
  private static final byte[] PATH_FIELD = "path=".getBytes(StandardCharsets.UTF_8);
  private static final byte[] PATH_FIELD_ROOT_TOKEN = "/".getBytes(StandardCharsets.UTF_8);
  private static final byte[] QUERY_FIELD = "query=".getBytes(StandardCharsets.UTF_8);
  private static final byte[] HOST_FIELD = "host=".getBytes(StandardCharsets.UTF_8);
  private static final byte[] HOST_DEFAULT_TOKEN = "_default".getBytes(StandardCharsets.UTF_8);

  private static final StandardCacheKeyProducer METHOD_PATH_QUERY = new StandardCacheKeyProducer() {
    @Override
    public void createKey(ServerHttpRequest request, KeyBuilder builder) {
      builder.add(METHOD_FIELD);
      builder.add(request.getMethodValue().toUpperCase(Locale.ENGLISH).getBytes(StandardCharsets.UTF_8));

      builder.add(PATH_FIELD);
      final String path = request.getURI().getPath();
      if (path == null) {
        builder.add(PATH_FIELD_ROOT_TOKEN);
      } else {
        builder.add(path.getBytes(StandardCharsets.UTF_8));
      }

      builder.add(QUERY_FIELD);
      final String query = request.getURI().getQuery();
      if (query != null) {
        builder.add(query.getBytes(StandardCharsets.UTF_8));
      }
    }
  };

  public static StandardCacheKeyProducer getInstance() {
    return METHOD_PATH_QUERY;
  }

  private StandardCacheKeyProducer() {
    // void
  }

  public StandardCacheKeyProducer includeHost() {
    return new StandardCacheKeyProducer() {
      @Override
      public void createKey(ServerHttpRequest request, KeyBuilder builder) {
        StandardCacheKeyProducer.this.createKey(request, builder);

        builder.add(HOST_FIELD);
        final String host = request.getHeaders().getFirst("Host");
        if (host != null) {
          builder.add(host.getBytes(StandardCharsets.UTF_8));
        } else {
          builder.add(HOST_DEFAULT_TOKEN);
        }
      }
    };
  }

}
