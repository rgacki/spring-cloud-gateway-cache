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
package org.contenttrace.springframework.cloud.gateway.cache.rules;

import org.contenttrace.springframework.cloud.gateway.cache.CacheControl;
import org.springframework.web.server.ServerWebExchange;

public class CacheControlRules {

	private static final Rule RESPONSE_HAS_NO_CACHE_ENABLED =
			exchange -> responseCacheControlFor(exchange).has(CacheControl.NO_CACHE);

	private static final Rule RESPONSE_HAS_NO_STORE_ENABLED =
			exchange -> responseCacheControlFor(exchange).has(CacheControl.NO_STORE);

	private static final Rule RESPONSE_IS_PRIVATE =
			exchange -> responseCacheControlFor(exchange).has(CacheControl.PRIVATE);

	public static Rule responseHasNoCacheEnabled() {
		return RESPONSE_HAS_NO_CACHE_ENABLED;
	}

	public static Rule responseHasNoStoreEnabled() {
		return RESPONSE_HAS_NO_STORE_ENABLED;
	}

	public static Rule responseIsPrivate() {
		return RESPONSE_IS_PRIVATE;
	}

	private static final String PARSED_CACHE_CONTROL_ATTRIBUTE_NAME = CacheControlRules.class.getName() + ".response.parsed";

	private static CacheControl responseCacheControlFor(final ServerWebExchange exchange) {
		final CacheControl existing = exchange.getAttribute(PARSED_CACHE_CONTROL_ATTRIBUTE_NAME);
		if (existing != null) {
			return existing;
		}
		final CacheControl cacheControl = CacheControl.parse(exchange.getResponse().getHeaders().getCacheControl());
		exchange.getAttributes().put(PARSED_CACHE_CONTROL_ATTRIBUTE_NAME, cacheControl);
		return cacheControl;
	}

}
