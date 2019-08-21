package org.contenttrace.springframework.cloud.gateway.cache.store;

import java.io.Serializable;

/**
 * The cache key.
 */
public interface CacheKey extends Serializable {

  /**
   * Returns a string representation of the key. The representation can be used with {@link Store#find(String)} method
   * to find entries.
   *
   * @return the string representation
   */
  String serializeAsString();

}
