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

/**
 * A factory that creates a Store instance.
 */
public interface StoreFactory {

	/**
	 * Creates a new instance of the store.
	 *
	 * @param cacheKeyProducer the producer of the cache key
	 * @return the instance
	 */
	Store createInstance(CacheKeyProducer cacheKeyProducer);

}
