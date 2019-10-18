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

import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * A stored cache entry. An entry is a single representation of a cached resource. For a single resource, several
 * entries may exist, sharing the same key.
 */
public interface Entry {

	/**
	 * Returns the cache key of the entry.
	 *
	 * @return the cache key
	 */
	CacheKey getKey();

	/**
	 * Returns the HTTP method the entry was created for.
	 *
	 * @return the HTTP method
	 */
	String getHttpMethod();

	/**
	 * Returns the host the entry was created for.
	 *
	 * @return the host
	 */
	Optional<String> getHost();

	/**
	 * Returns the path the entry was created for.
	 *
	 * @return the path
	 */
	String getPath();

	/**
	 * Returns the query the entry was created for.
	 *
	 * @return the query
	 */
	Optional<String> getQuery();

	/**
	 * Returns the request headers, the entry was created for. The headers may be empty, if the original response does not
	 * provide a "Vary" header. The headers returned are only those mentioned by the "Vary" header.
	 *
	 * @return the request headers
	 */
	HttpHeaders getRequestHeaders();

	/**
	 * Returns the headers of the original response. The headers are not filtered.
	 *
	 * @return the response headers
	 */
	HttpHeaders getResponseHeaders();

	/**
	 * Returns the data of the entry.
	 *
	 * @return the data
	 * @throws IOException if the stream cannot be produced
	 */
	InputStream openStream() throws IOException;

	/**
	 * Returns the size of the data. May return {@code -1L} if there is no data.
	 *
	 * @return the size or {@code -1L} if there is no data
	 */
	long size();

	/**
	 * Invalidates this cache entry. This method must be side-effect free. So for example, if the entry is already
	 * invalidated or if an I/O related issue occurs, the method should return without an error. Any errors must be
	 * handled by the implementation internally.
	 */
	void invalidate();

}
