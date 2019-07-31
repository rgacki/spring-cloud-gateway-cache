package org.contenttrace.springframework.cloud.gateway.cache;

public class CacheConfiguration {

  private boolean exposeCacheEventHeader = false;

  public boolean isExposeCacheEventHeader() {
    return exposeCacheEventHeader;
  }

  public void setExposeCacheEventHeader(boolean exposeCacheEventHeader) {
    this.exposeCacheEventHeader = exposeCacheEventHeader;
  }
}
