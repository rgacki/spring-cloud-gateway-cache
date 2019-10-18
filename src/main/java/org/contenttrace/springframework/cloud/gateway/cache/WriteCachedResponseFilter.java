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

import org.contenttrace.springframework.cloud.gateway.cache.store.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.util.Assert.notNull;

public class WriteCachedResponseFilter implements GlobalFilter, Ordered {

	private static final Logger LOG = LoggerFactory.getLogger(WriteCachedResponseFilter.class);

	private final DataBufferFactory dataBufferFactory;
	private final List<MediaType> streamingMediaTypes;

	public WriteCachedResponseFilter(final DataBufferFactory dataBufferFactory,
									 final List<MediaType> streamingMediaTypes) {
		notNull(dataBufferFactory, "'dataBufferFactory' must not be null!");
		this.dataBufferFactory = dataBufferFactory;
		this.streamingMediaTypes = streamingMediaTypes;
	}

	@Override
	public int getOrder() {
		return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER;
	}

	@Override
	public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {

		final Entry entry = (Entry) exchange.getAttributes().get(FindCacheEntryFilterFactory.CACHE_ENTRY_ATTRIBUTE_NAME);

		if (entry == null) {
			return chain.filter(exchange);
		}

		final ServerHttpResponse response = exchange.getResponse();

		final Flux<DataBuffer> body = DataBufferUtils.readInputStream(entry::openStream, dataBufferFactory, 8 * 1024);

		MediaType contentType = null;
		try {
			contentType = response.getHeaders().getContentType();
		} catch (Exception e) {
			LOG.trace("invalid media type", e);
		}

		return (isStreamingMediaType(contentType)
				? response.writeAndFlushWith(body.map(Flux::just))
				: response.writeWith(body));
	}

	// TODO: use framework if possible
	// TODO: port to WebClientWriteResponseFilter
	private boolean isStreamingMediaType(@Nullable MediaType contentType) {
		return (contentType != null && this.streamingMediaTypes.stream()
				.anyMatch(contentType::isCompatibleWith));
	}
}
