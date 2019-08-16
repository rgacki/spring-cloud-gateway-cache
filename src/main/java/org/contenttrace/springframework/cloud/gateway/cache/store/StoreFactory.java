package org.contenttrace.springframework.cloud.gateway.cache.store;

/**
 * A factory that creates a Store instance.
 *
 */
public interface StoreFactory {

  /**
   * Creates a new instance of the store.
   *
   * @param cacheKeyProducer the producer of the cache key
   * @return the instance
   */
  Store createInstance(CacheKeyProducer cacheKeyProducer);

}
