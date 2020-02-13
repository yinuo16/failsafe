/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package net.jodah.failsafe.internal;

import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.CircuitBreaker.State;
import net.jodah.failsafe.ExecutionContext;

public class ClosedState extends CircuitState {
  private final CircuitBreakerInternals internals;

  public ClosedState(CircuitBreaker breaker, CircuitBreakerInternals internals) {
    super(breaker, CircuitStats.create(breaker, false, null));
    this.internals = internals;
  }

  @Override
  public boolean allowsExecution() {
    return true;
  }

  @Override
  public State getState() {
    return State.CLOSED;
  }

  @Override
  public synchronized void handleConfigChange() {
    stats = CircuitStats.create(breaker, false, stats);
  }

  /**
   * Checks to see if the the executions and failure thresholds have been exceeded, opening the circuit if so.
   */
  @Override
  synchronized void checkThreshold(ExecutionContext context) {
    int executionThreshold = breaker.getFailureExecutionThreshold();
    if (stats.getExecutionCount() > executionThreshold) {
      double failureRateThreshold = breaker.getFailureRateThreshold();
      if ((failureRateThreshold != 0 && stats.getFailureRate() >= failureRateThreshold) || (failureRateThreshold == 0
        && stats.getFailureCount() >= breaker.getFailureThreshold()))
        internals.open(context);
    }
  }
}