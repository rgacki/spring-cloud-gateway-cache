package org.contenttrace.springframework.cloud.gateway.cache.store;

/**
 * A factory that creates a Store instance.
 *
 * @param <T> the concrete store configuration
 */
public interface StoreFactory<T extends StoreFactory.StoreConfiguration> {

  interface StoreConfiguration {

  }

  Store create(CacheKeyProducer cacheKeyProducer, T configuration);

}
