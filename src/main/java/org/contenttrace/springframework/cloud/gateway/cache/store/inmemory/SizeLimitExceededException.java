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

import java.io.IOException;

/**
 * Thrown if a size limit has been exceeded during an IO operation.
 *
 * @author Robert Gacki, robert.gacki@cgi.com
 */
public class SizeLimitExceededException extends IOException {

	private static final long serialVersionUID = -7330482368164293210L;

	private final long maxmimumSize;

	public SizeLimitExceededException(final long maxmimumSize, final String message) {
		super(message);
		this.maxmimumSize = maxmimumSize;
	}

	public SizeLimitExceededException(final long maxmimumSize, final String message, final Throwable cause) {
		super(message, cause);
		this.maxmimumSize = maxmimumSize;
	}

	public long getMaxmimumSize() {
		return maxmimumSize;
	}
}
