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
package org.contenttrace.springframework.cloud.gateway.cache.rules;

import org.springframework.web.server.ServerWebExchange;

import java.util.*;

/**
 * A rule that must apply in order for a cache entry to be generated.
 */
public interface Rule {

	boolean applies(ServerWebExchange exchange);

	static Rule not(final Rule rule) {
		return exchange -> !rule.applies(exchange);
	}

	static Rule and(final Rule... rules) {
		return and(Arrays.asList(rules));
	}

	abstract class ConjunctionRule implements Rule {

		final List<Rule> members;

		ConjunctionRule(final List<Rule> members) {
			this.members = Collections.unmodifiableList(members);
		}

	}

	static Rule and(final Collection<Rule> rules) {
		return new ConjunctionRule(new ArrayList<>(rules)) {
			@Override
			public boolean applies(final ServerWebExchange exchange) {
				for (final Rule member : this.members) {
					if (!member.applies(exchange)) {
						return false;
					}
				}
				return true;
			}
		};
	}

	static Rule or(final Rule... rules) {
		return or(Arrays.asList(rules));
	}

	static Rule or(final Collection<Rule> rules) {
		return new ConjunctionRule(new ArrayList<>(rules)) {
			@Override
			public boolean applies(final ServerWebExchange exchange) {
				for (final Rule member : this.members) {
					if (member.applies(exchange)) {
						return true;
					}
				}
				return false;
			}
		};
	}

}
