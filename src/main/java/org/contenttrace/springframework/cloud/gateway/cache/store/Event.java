package org.contenttrace.springframework.cloud.gateway.cache.store;

import java.time.Instant;
import java.time.LocalDateTime;

public interface Event {

  LocalDateTime getTime();

}
