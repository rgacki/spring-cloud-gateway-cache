package org.contenttrace.springframework.cloud.gateway.cache.store;

/**
 * Provides read-only access to store metrics.
 */
public interface Metrics {

  long getStoredEntries();

}
