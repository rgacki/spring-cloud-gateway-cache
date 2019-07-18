package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import org.contenttrace.springframework.cloud.gateway.cache.store.CacheKeyProducer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

class SHA2CacheKeyBuilder implements CacheKeyProducer.KeyBuilder {

  static class SHA2CacheKey {

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
