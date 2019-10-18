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
package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.store.StandardCacheKeyProducer;
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryStoreFactoryTest {

	@Test
	void shouldCreateStore() {

		// Given
		final CacheKeyProducer cacheKeyProducer = StandardCacheKeyProducer.getInstance();
		final InMemoryStoreConfiguration configuration = new InMemoryStoreConfiguration();
		final InMemoryStoreFactory factory = new InMemoryStoreFactory(configuration);

		// When
		final Store store = factory.createStore(cacheKeyProducer);

		// Then
		assertNotNull(store);
	}

}