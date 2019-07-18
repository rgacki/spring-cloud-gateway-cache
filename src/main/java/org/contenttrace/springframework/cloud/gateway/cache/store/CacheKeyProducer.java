package org.contenttrace.springframework.cloud.gateway.cache.store;

import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * A producer of cache keys.
 */
public interface CacheKeyProducer {

  /**
   * The key builder that is passed to the producer. The implementation is provided by the Store
   * because the internal key representation is an implementation detail.
   */
  interface KeyBuilder {

    /**
     * Adds key material.
     *
     * @param material the material
     * @return the builder instance
     */
    KeyBuilder add(byte[] material);

    /**
     * Adds key material.
     *
     * @param material the material
     * @return the builder instance
     */
    KeyBuilder add(byte[] material, int offset, int length);

  }

  /**
   * Invoked to create a cache key for the given request.
   *
   * @param request the request to create the cache key for
   * @param builder the builder of the key
   */
  void createKey(ServerHttpRequest request, KeyBuilder builder);

  /**
   * Concatenates this producers with another.
   *
   * @param other the other producer
   * @return the new producer
   */
  default CacheKeyProducer concat(final CacheKeyProducer other) {
    return (request, builder) -> {
      CacheKeyProducer.this.createKey(request, builder);
      other.createKey(request, builder);
    };
  }

}
