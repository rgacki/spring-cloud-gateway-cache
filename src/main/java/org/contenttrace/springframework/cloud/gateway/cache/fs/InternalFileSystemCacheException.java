package org.contenttrace.springframework.cloud.gateway.cache.fs;

public class InternalFileSystemCacheException extends RuntimeException {

  public InternalFileSystemCacheException(String message) {
    super(message);
  }

  public InternalFileSystemCacheException(String message, Throwable cause) {
    super(message, cause);
  }
}
