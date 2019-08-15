package org.contenttrace.springframework.cloud.gateway.cache;

import org.springframework.cloud.gateway.filter.GatewayFilter;

public interface CacheEntryFilter extends GatewayFilter {

  String CACHE_ENTRY_ATTRIBUTE_NAME = CacheEntryFilter.class.getName() + ".cacheEntry";

}
