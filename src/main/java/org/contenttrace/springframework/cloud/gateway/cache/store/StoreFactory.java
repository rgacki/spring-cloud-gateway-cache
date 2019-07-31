package org.contenttrace.springframework.cloud.gateway.cache.store;

/**
 * A factory that creates a Store instance.
 *
 * @param <T> the concrete store configuration
 */
public interface StoreFactory<T extends StoreConfiguration> {

  /**
   * Creates a new instance of the store.
   *
   * @param cacheKeyProducer the producer of the cache key
   * @param configuration the configuration
   * @return the instance
   */
  Store createInstance(CacheKeyProducer cacheKeyProducer, T configuration);

}
