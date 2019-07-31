package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.contenttrace.springframework.cloud.gateway.cache.store.StoreFactory;

import java.util.Objects;

public class InMemoryStoreFactory implements StoreFactory<InMemoryStoreConfiguration> {
  @Override
  public Store createInstance(final CacheKeyProducer cacheKeyProducer,
                              final InMemoryStoreConfiguration configuration) {
    Objects.requireNonNull(cacheKeyProducer, "A cache key producer is required!");
    Objects.requireNonNull(configuration, "A configuration is required!");
    return new InMemoryStore(cacheKeyProducer, configuration);
  }
}
