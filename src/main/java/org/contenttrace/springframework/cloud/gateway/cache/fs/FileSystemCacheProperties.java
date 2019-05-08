package org.contenttrace.springframework.cloud.gateway.cache.fs;

import java.nio.file.Path;

public class FileSystemCacheProperties {

  private Path root;

  public Path getRoot() {
    return root;
  }

  public void setRoot(Path root) {
    this.root = root;
  }
}
