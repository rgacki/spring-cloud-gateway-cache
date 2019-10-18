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

import org.contenttrace.springframework.cloud.gateway.cache.store.StoreConfiguration;

public class InMemoryStoreConfiguration implements StoreConfiguration {

	private long initialBufferMinimumSize = 256_000L; // 256kb
	private long initialBufferMaximumSize = 1_000_000L;

	public long getInitialBufferMinimumSize() {
		return initialBufferMinimumSize;
	}

	public void setInitialBufferMinimumSize(long initialBufferMinimumSize) {
		this.initialBufferMinimumSize = initialBufferMinimumSize;
	}

	public long getInitialBufferMaximumSize() {
		return initialBufferMaximumSize;
	}

	public void setInitialBufferMaximumSize(long initialBufferMaximumSize) {
		this.initialBufferMaximumSize = initialBufferMaximumSize;
	}
}
