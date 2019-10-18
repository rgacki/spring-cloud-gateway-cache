/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.contenttrace.springframework.cloud.gateway.cache;

import org.contenttrace.springframework.cloud.gateway.cache.rules.Rule;
import org.contenttrace.springframework.cloud.gateway.cache.store.Entry;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

public class FindCacheEntryFilterFactory extends AbstractGatewayFilterFactory<FindCacheEntryFilterFactory.Config>
		implements GatewayFilterFactory<FindCacheEntryFilterFactory.Config> {

	private static final Logger LOG = LoggerFactory.getLogger(FindCacheEntryFilterFactory.class);

	public static final String CACHE_ENTRY_ATTRIBUTE_NAME = FindCacheEntryFilterFactory.class.getName() + ".cacheEntry";

	private final CacheConfiguration cacheConfiguration;
	private final Store store;
	private final Rule rule;

	private final ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider;

	// do not use this headersFilters directly, use getHeadersFilters() instead.
	private volatile List<HttpHeadersFilter> headersFilters;

	public FindCacheEntryFilterFactory(final CacheConfiguration cacheConfiguration,
																		 final Store store,
																		 final Rule rule,
																		 final ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider) {
		this.cacheConfiguration = cacheConfiguration;
		this.store = store;
		this.rule = rule;
		this.headersFiltersProvider = headersFiltersProvider;
	}

	protected boolean canCache(final ServerWebExchange exchange) {
		return rule.applies(exchange);
	}

	protected boolean isValid(final Entry entry) {
		// TODO: validate the entry.
		return true;
	}

	public List<HttpHeadersFilter> getHeadersFilters() {
		if (headersFilters == null) {
			headersFilters = headersFiltersProvider.getIfAvailable();
		}
		return headersFilters;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return new OrderedGatewayFilter(((exchange, chain) -> {

			if (isAlreadyRouted(exchange)) {
				return chain.filter(exchange);
			}

			if (!canCache(exchange)) {
				LOG.debug("Exchange [{}] cannot be cached. Avoiding cache lookup.", exchange);
				return chain.filter(exchange);
			}

			store.find(exchange.getRequest()).ifPresent(entry -> {
				if (isValid(entry)) {

					setAlreadyRouted(exchange);

					final ServerHttpResponse response = exchange.getResponse();

					response.setStatusCode(HttpStatus.OK);

					final String contentTypeValue = entry.getResponseHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
					if (StringUtils.hasLength(contentTypeValue)) {
						exchange.getAttributes().put(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR, contentTypeValue);
					}

					// make sure headers filters run after setting status so it is
					// available in response
					final HttpHeaders filteredResponseHeaders = HttpHeadersFilter.filter(
							getHeadersFilters(), entry.getResponseHeaders(), exchange, HttpHeadersFilter.Type.RESPONSE);

					if (!filteredResponseHeaders
							.containsKey(HttpHeaders.TRANSFER_ENCODING)
							&& filteredResponseHeaders
							.containsKey(HttpHeaders.CONTENT_LENGTH)) {
						// It is not valid to have both the transfer-encoding header and
						// the content-length header
						// remove the transfer-encoding header in the response if the
						// content-length header is present
						response.getHeaders().remove(HttpHeaders.TRANSFER_ENCODING);
					}

					exchange.getAttributes().put(CLIENT_RESPONSE_HEADER_NAMES, filteredResponseHeaders.keySet());

					// Remove or replace the cache event header
					filteredResponseHeaders.remove(cacheConfiguration.getCacheEventHeaderName());
					if (cacheConfiguration.isExposeCacheEventHeader()) {
						filteredResponseHeaders.add(cacheConfiguration.getCacheEventHeaderName(), "hit");
					}

					response.getHeaders().putAll(filteredResponseHeaders);

					exchange.getAttributes().put(CACHE_ENTRY_ATTRIBUTE_NAME, entry);
					LOG.debug("Found cache entry [{}] for exchange [{}].", entry, exchange);
				} else {
					entry.invalidate();
					LOG.debug("Found cache entry [{}] for exchange [{}], but is invalid.", entry, exchange);
				}
			});

			return chain.filter(exchange);
		}), OrderedGatewayFilter.HIGHEST_PRECEDENCE + 1);
	}

	public static class Config {

	}
}
