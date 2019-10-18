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

import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKey;
import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKeyProducer;
import org.springframework.util.Base64Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

class SHA2CacheKeyBuilder implements CacheKeyProducer.KeyBuilder {

	static final String CACHE_KEY_PREFIX = "inmemory://";

	static class SHA2CacheKey implements CacheKey {

		private final byte[] key;

		SHA2CacheKey(byte[] key) {
			this.key = key;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SHA2CacheKey that = (SHA2CacheKey) o;
			return Arrays.equals(key, that.key);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(key);
		}

		@Override
		public String serializeAsString() {
			// TODO: Instead of Base64, use a hex
			return String.format("%s%s", CACHE_KEY_PREFIX, Base64Utils.encodeToUrlSafeString(key));
		}
	}

	public static SHA2CacheKey parse(String input) {
		Objects.requireNonNull(input, "Input must not be null!");
		if (!input.startsWith(CACHE_KEY_PREFIX) || input.length() <= CACHE_KEY_PREFIX.length()) {
			throw new IllegalArgumentException(String.format("Input [%s] is not a in-memory cache key!", input));
		}
		return new SHA2CacheKey(Base64Utils.decodeFromUrlSafeString(input.substring(CACHE_KEY_PREFIX.length())));
	}

	private final MessageDigest digest;

	public SHA2CacheKeyBuilder() {
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Unsupported digest algorithm!");
		}
	}

	@Override
	public CacheKeyProducer.KeyBuilder add(byte[] material) {
		requireNonNull(material, "'material' must not be null!");
		digest.update(material);
		return this;
	}

	@Override
	public CacheKeyProducer.KeyBuilder add(byte[] material, int offset, int length) {
		requireNonNull(material, "'material' must not be null!");
		digest.update(material, offset, length);
		return this;
	}

	SHA2CacheKey build() {
		return new SHA2CacheKey(digest.digest());
	}
}
