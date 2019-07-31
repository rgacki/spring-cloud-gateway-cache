package org.contenttrace.springframework.cloud.gateway.cache.store;

import java.net.URI;

public interface ResourceCachedEvent extends Event {

  URI getURI();

}
