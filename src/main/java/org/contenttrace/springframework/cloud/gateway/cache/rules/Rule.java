package org.contenttrace.springframework.cloud.gateway.cache.rules;

import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
