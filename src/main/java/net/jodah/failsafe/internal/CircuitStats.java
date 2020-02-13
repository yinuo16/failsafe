/*
 * Copyright 2018 the original author or authors.
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

import java.time.Duration;

/**
 * Stats for a circuit breaker.
 */
public interface CircuitStats {
  static CircuitStats create(CircuitBreaker breaker, boolean halfOpenState, CircuitStats oldStats) {
    Duration measurementPeriod = breaker.getFailureThresholdingPeriod();
    if (!halfOpenState && measurementPeriod != null)
      return new TimedCircuitStats(breaker.getFailureExecutionThreshold(), measurementPeriod, oldStats);
    else {
      int capacity = halfOpenState && breaker.getSuccessThresholdingCapacity() != 0 ?
        breaker.getSuccessThresholdingCapacity() :
        breaker.getFailureThresholdingCapacity();
      if (capacity > 1)
        return new CountingCircuitStats(capacity, oldStats);
      else
        return new DefaultCircuitStats();
    }
  }

  int getFailureCount();

  int getExecutionCount();

  int getSuccessCount();

  int getFailureRate();

  int getSuccessRate();

  void recordFailure();

  void recordSuccess();
}
