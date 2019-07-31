package org.contenttrace.springframework.cloud.gateway.cache.store;

/**
 * Provides read-only access to store events.
 *
 * <p>Whether and what events are captured depends on the store configuration. For production environments it is
 * recommended to only have errors to be recorded.</p>
 *
 * <p>Events must be propagated synchronously.</p>
 */
public interface Events {

  /**
   * Registers an event listener. Has no effect, if the listener is already registered.
   *
   * @param listener the listener to register
   */
  void register(EventListener listener);

  /**
   * Unregister a listener. Has no effect, if the listener is not registered.
   *
   * @param listener the listener to unregister
   */
  void unregister(EventListener listener);

}
