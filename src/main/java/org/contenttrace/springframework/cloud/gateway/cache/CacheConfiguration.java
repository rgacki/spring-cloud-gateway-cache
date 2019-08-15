package org.contenttrace.springframework.cloud.gateway.cache;

public class CacheConfiguration {

  private boolean exposeCacheEventHeader = false;
  private String cacheEventHeaderName = "X-Cache";
  private String onValidCacheEntryRequestUrl = "forward://serve-cache-entry";

  public boolean isExposeCacheEventHeader() {
    return exposeCacheEventHeader;
  }

  public void setExposeCacheEventHeader(boolean exposeCacheEventHeader) {
    this.exposeCacheEventHeader = exposeCacheEventHeader;
  }

  public String getCacheEventHeaderName() {
    return cacheEventHeaderName;
  }

  public void setCacheEventHeaderName(String cacheEventHeaderName) {
    this.cacheEventHeaderName = cacheEventHeaderName;
  }

  public String getOnValidCacheEntryRequestUrl() {
    return onValidCacheEntryRequestUrl;
  }

  public void setOnValidCacheEntryRequestUrl(String onValidCacheEntryRequestUrl) {
    this.onValidCacheEntryRequestUrl = onValidCacheEntryRequestUrl;
  }
}
