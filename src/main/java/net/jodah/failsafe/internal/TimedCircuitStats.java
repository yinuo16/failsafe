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

import java.time.Duration;

/**
 * A CircuitBreakerStats implementation that counts execution results within a time period, and buckets results to
 * minimize overhead.
 */
class TimedCircuitStats implements CircuitStats {
  private static final int DEFAULT_BUCKET_COUNT = 10;

  private final int executionThreshold;
  private final Duration measurementPeriod;
  private final Bucket[] buckets;
  private final Summary summary;
  /* Index to write next entry to */
  volatile int nextIndex;

  private static abstract class Stat {
    volatile int executions;
    volatile int successes;
    volatile int failures;
  }

  private static class Bucket extends Stat {
    Duration startTime;
  }

  private static class Summary extends Stat {

  }

  public TimedCircuitStats(int executionThreshold, Duration measurementPeriod, CircuitStats oldStats) {
    this.executionThreshold = executionThreshold;
    this.measurementPeriod = measurementPeriod;
    this.buckets = new Bucket[DEFAULT_BUCKET_COUNT];
    this.summary = new Summary();
  }

  @Override
  public void recordSuccess() {
  }

  @Override
  public void recordFailure() {
  }

  @Override
  public int getExecutionCount() {
    return summary.executions;
  }

  @Override
  public int getFailureCount() {
    return summary.failures;
  }

  @Override
  public int getFailureRate() {
    return 0;
  }

  @Override
  public int getSuccessCount() {
    return summary.successes;
  }

  @Override
  public int getSuccessRate() {
    return 0;
  }
}
