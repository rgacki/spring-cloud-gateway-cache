package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import org.contenttrace.springframework.cloud.gateway.cache.store.Metrics;

public class InMemoryMetrics implements Metrics {

  @Override
  public long getStoredEntries() {
    return 0;
  }
}
