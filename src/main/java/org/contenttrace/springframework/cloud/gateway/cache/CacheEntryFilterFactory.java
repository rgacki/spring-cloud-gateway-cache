package org.contenttrace.springframework.cloud.gateway.cache;

import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;

public interface CacheEntryFilterFactory<C> extends GatewayFilterFactory<C> {

  String CACHE_ENTRY_ATTRIBUTE_NAME = CacheEntryFilterFactory.class.getName() + ".cacheEntry";

}
