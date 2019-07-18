package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKeyProducer;
import org.springframework.http.server.reactive.ServerHttpRequest;

public class SHA2CacheKeyProducer implements CacheKeyProducer {

  @Override
  public void createKey(final ServerHttpRequest request, final KeyBuilder builder) {

  }


}
