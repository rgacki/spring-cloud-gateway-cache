package org.contenttrace.springframework.cloud.gateway.cache.store;

import java.net.URI;

/**
 * An event that is propagated when a new cache entry is created.
 */
public interface ResourceCachedEvent extends Event {

  /**
   * Returns the URI of the cache entry.
   *
   * @return the URI
   */
  URI getURI();

  /**
   * Returns the entry itself.
   *
   * @return the entry
   */
  Entry getEntry();

}
