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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
		prefix = "cloud.gateway.cache"
)
public class CacheConfiguration {

	private boolean exposeCacheEventHeader = false;
	private String cacheEventHeaderName = "X-Cache";

	public boolean isExposeCacheEventHeader() {
		return exposeCacheEventHeader;
	}

	public void setExposeCacheEventHeader(boolean exposeCacheEventHeader) {
		this.exposeCacheEventHeader = exposeCacheEventHeader;
	}

	public String getCacheEventHeaderName() {
		return cacheEventHeaderName;
	}

	public void setCacheEventHeaderName(String cacheEventHeaderName) {
		this.cacheEventHeaderName = cacheEventHeaderName;
	}

}
