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
package org.contenttrace.springframework.cloud.gateway.cache.store;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A cache store.
 */
public interface Store {

	/**
	 * <p>Mutates the exchange to have its response written to the store and returns it. Only works if a response is
	 * available in the exchange, which has not be read from, yet.</p>
	 *
	 * @param exchange the exchange
	 * @return the exchange
	 */
	ServerWebExchange write(ServerWebExchange exchange);

	/**
	 * Finds a stored cache entry for the given request. Returns nothing if the request does not match any stored entries.
	 *
	 * @param request the request
	 * @return the entry
	 */
	Optional<? extends Entry> find(ServerHttpRequest request);

	/**
	 * Finds entries by its key. The key is provided as string representation and may be parsed by the store. If
	 * the key cannot be parsed, simply no entry is returned.
	 *
	 * @param cacheKey the cache key
	 * @return the stream of entries
	 */
	Stream<? extends Entry> find(String cacheKey);

	/**
	 * Finds a cache entry by its key. The store may not return the entry if the key is unknown or if the key
	 * implementation type is unknown.
	 *
	 * @param cacheKey the cache key
	 * @return the entry
	 */
	Stream<? extends Entry> find(CacheKey cacheKey);

	/**
	 * Returns the events accessor for this store.
	 *
	 * @return the events accessor
	 */
	Events events();

	/**
	 * Returns the metrics accessor for this store.
	 *
	 * @return the metrics accessor
	 */
	Metrics metrics();

}
