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
package org.contenttrace.springframework.cloud.gateway.cache.store;

/**
 * Provides read-only access to store events.
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
