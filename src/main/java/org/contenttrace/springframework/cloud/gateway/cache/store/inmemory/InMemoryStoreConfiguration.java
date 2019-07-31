package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import org.contenttrace.springframework.cloud.gateway.cache.store.StoreConfiguration;

public class InMemoryStoreConfiguration implements StoreConfiguration {

  private long initialBufferMinimumSize = 256_000L; // 256kb
  private long initialBufferMaximumSize = 1_000_000L;

  public long getInitialBufferMinimumSize() {
    return initialBufferMinimumSize;
  }

  public void setInitialBufferMinimumSize(long initialBufferMinimumSize) {
    this.initialBufferMinimumSize = initialBufferMinimumSize;
  }

  public long getInitialBufferMaximumSize() {
    return initialBufferMaximumSize;
  }

  public void setInitialBufferMaximumSize(long initialBufferMaximumSize) {
    this.initialBufferMaximumSize = initialBufferMaximumSize;
  }
}
