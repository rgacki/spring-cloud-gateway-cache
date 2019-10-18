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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(CacheAutoConfiguration.class)
public class InMemoryCacheTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(InMemoryCacheTestApplication.class, args);
	}

	@Value("${test.uri:http://test-backend.org:80}")
	String backendUri;

	@Bean
	public CacheConfiguration cacheConfiguration() {
		final CacheConfiguration configuration = new CacheConfiguration();
		configuration.setExposeCacheEventHeader(true);
		return configuration;
	}

	@Bean
	public RouteLocator customRouteLocator(final RouteLocatorBuilder builder,
																				 final CreateCacheEntryFilterFactory createCacheEntryFilterFactory,
																				 final FindCacheEntryFilterFactory findCacheEntryFilterFactory) {

		final GatewayFilter createCacheEntry =
				createCacheEntryFilterFactory.apply(new CreateCacheEntryFilterFactory.Config());
		final GatewayFilter findCacheEntry =
				findCacheEntryFilterFactory.apply(new FindCacheEntryFilterFactory.Config());

		return builder.routes()
				.route(r -> r.host("cached.org", "**.cached.org")
						.filters(
								filters -> filters.filters(createCacheEntry, findCacheEntry)
						)
						.uri(backendUri))
				.build();
	}

	@Bean
	public CacheTestController integrationTestController() {
		return new CacheTestController();
	}
}
