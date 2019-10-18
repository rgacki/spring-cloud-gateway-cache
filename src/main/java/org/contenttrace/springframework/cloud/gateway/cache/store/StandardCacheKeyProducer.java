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

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public abstract class StandardCacheKeyProducer implements CacheKeyProducer {

	private static final byte[] METHOD_FIELD = "method=".getBytes(StandardCharsets.UTF_8);
	private static final byte[] PATH_FIELD = "path=".getBytes(StandardCharsets.UTF_8);
	private static final byte[] PATH_FIELD_ROOT_TOKEN = "/".getBytes(StandardCharsets.UTF_8);
	private static final byte[] QUERY_FIELD = "query=".getBytes(StandardCharsets.UTF_8);
	private static final byte[] HOST_FIELD = "host=".getBytes(StandardCharsets.UTF_8);
	private static final byte[] HOST_DEFAULT_TOKEN = "_default".getBytes(StandardCharsets.UTF_8);

	private static final StandardCacheKeyProducer METHOD_PATH_QUERY = new StandardCacheKeyProducer() {
		@Override
		public void createKey(ServerHttpRequest request, KeyBuilder builder) {
			builder.add(METHOD_FIELD);
			builder.add(request.getMethodValue().toUpperCase(Locale.ENGLISH).getBytes(StandardCharsets.UTF_8));

			builder.add(PATH_FIELD);
			final String path = request.getURI().getPath();
			if (path == null) {
				builder.add(PATH_FIELD_ROOT_TOKEN);
			} else {
				builder.add(path.getBytes(StandardCharsets.UTF_8));
			}

			builder.add(QUERY_FIELD);
			final String query = request.getURI().getQuery();
			if (query != null) {
				builder.add(query.getBytes(StandardCharsets.UTF_8));
			}
		}
	};

	public static StandardCacheKeyProducer getInstance() {
		return METHOD_PATH_QUERY;
	}

	private StandardCacheKeyProducer() {
		// void
	}

	public StandardCacheKeyProducer includeHost() {
		return new StandardCacheKeyProducer() {
			@Override
			public void createKey(ServerHttpRequest request, KeyBuilder builder) {
				StandardCacheKeyProducer.this.createKey(request, builder);

				builder.add(HOST_FIELD);
				final String host = request.getHeaders().getFirst("Host");
				if (host != null) {
					builder.add(host.getBytes(StandardCharsets.UTF_8));
				} else {
					builder.add(HOST_DEFAULT_TOKEN);
				}
			}
		};
	}

}
