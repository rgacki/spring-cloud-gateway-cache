package org.contenttrace.springframework.cloud.gateway.cache.store;

/**
 * An event listener.
 */
public interface EventListener {

  /**
   * Called with the event that was dispatched.
   *
   * @param event the event
   */
  void dispatched(Event event);

}
