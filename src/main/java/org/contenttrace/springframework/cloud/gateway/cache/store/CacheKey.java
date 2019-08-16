package org.contenttrace.springframework.cloud.gateway.cache.store;

import java.io.Serializable;

/**
 * The cache key.
 *
 * <p>An implementation must provide a {@link #toString()} implementation.</p>
 */
public interface CacheKey extends Serializable {

}
