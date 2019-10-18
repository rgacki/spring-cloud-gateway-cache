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
import org.contenttrace.springframework.cloud.gateway.cache.store.ResourceCachedEvent;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class CacheIntegrationTests extends IntegrationTests {

	static final String CACHED_HOST = "www.cached.org";

	TestEvents testEvents;

	abstract WebTestClient client();

	abstract Store store();

	@BeforeEach
	void setup() {
		testEvents = new TestEvents();
		testEvents.registerWith(store());
	}

	@AfterEach
	void tearDown() {
		testEvents.unregisterFrom(store());
		testEvents = null;
	}

	@Test
	void shouldCacheRequest() {

		// When
		final EntityExchangeResult<byte[]> firstResult = client().get().uri("/cacheable").header("Host", CACHED_HOST)
				.header("X-Test", "shouldCacheRequest(#1)")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().valueEquals("X-Cache", "store")
				.expectBody()
				.returnResult();

		// Then
		testEvents.assertLast().isOfType(ResourceCachedEvent.class).satisfies(event -> {
			assertNotNull(event.getEntry());
			final Entry entry = event.getEntry();
			assertThat(entry.getKey()).isNotNull();
			assertThat(entry.getKey().serializeAsString()).isNotNull().isNotEmpty();
			assertEquals("GET", entry.getHttpMethod());
			assertEquals(CACHED_HOST, entry.getHost().orElse(null));
			assertEquals("/cacheable", entry.getPath());
		});
		assertNotNull(firstResult);

		// When
		final EntityExchangeResult<byte[]> secondResult = client().get().uri("/cacheable").header("Host", CACHED_HOST)
				.header("X-Test", "shouldCacheRequest(#2)")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().valueEquals("X-Cache", "hit")
				.expectBody()
				.returnResult();

		// Then
		assertNotNull(secondResult);
		Assertions.assertArrayEquals(firstResult.getResponseBody(), secondResult.getResponseBody());
	}

}
