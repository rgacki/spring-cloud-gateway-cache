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
import org.contenttrace.springframework.cloud.gateway.cache.store.Store;
import org.contenttrace.springframework.cloud.gateway.cache.store.StoreFactory;

import java.util.Objects;

public class InMemoryStoreFactory implements StoreFactory {

	private final InMemoryStoreConfiguration configuration;

	public InMemoryStoreFactory(InMemoryStoreConfiguration configuration) {
		Objects.requireNonNull(configuration, "A configuration is required!");
		this.configuration = configuration;
	}

	@Override
	public Store createInstance(final CacheKeyProducer cacheKeyProducer) {
		Objects.requireNonNull(cacheKeyProducer, "A cache key producer is required!");
		return new InMemoryStore(cacheKeyProducer, configuration);
	}
}
