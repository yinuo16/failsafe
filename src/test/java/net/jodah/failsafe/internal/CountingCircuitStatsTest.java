package net.jodah.failsafe.internal;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.function.Predicate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class CountingCircuitStatsTest extends CircuitStatsTest<CountingCircuitStats> {
  @Override
  CountingCircuitStats stats(int size) {
    return new CountingCircuitStats(size, null);
  }

  public void shouldReturnUnitializedValues() {
    stats = stats(100);
    for (int i = 0; i < 100; i++) {
      assertEquals(stats.setNext(true), -1);
    }

    assertEquals(stats.setNext(true), 1);
    assertEquals(stats.setNext(true), 1);
  }

  public void testMetrics() {
    stats = stats(100);
    assertEquals(stats.getSuccessRate(), 0);
    assertEquals(stats.getFailureRate(), 0);

    recordExecutions(stats, 50, i -> i % 3 == 0);

    assertEquals(stats.getSuccessCount(), 17);
    assertEquals(stats.getSuccessRate(), 34);
    assertEquals(stats.getFailureCount(), 33);
    assertEquals(stats.getFailureRate(), 66);

    recordExecutions(stats, 100, i -> true);

    assertEquals(stats.getSuccessCount(), 100);
    assertEquals(stats.getSuccessRate(), 100);
    assertEquals(stats.getFailureCount(), 0);
    assertEquals(stats.getFailureRate(), 0);
  }

  public void testCopyBitsToEqualSizedSet() {
    stats = stats(5);
    recordExecutions(stats, 2, i -> true);
    recordExecutions(stats, 3, i -> false);

    stats.nextIndex = 0;
    CountingCircuitStats right = new CountingCircuitStats(5, stats);
    assertValues(right, true, true, false, false, false);

    stats.nextIndex = 2;
    right = new CountingCircuitStats(5, stats);
    assertValues(right, false, false, false, true, true);

    stats.nextIndex = 4;
    right = new CountingCircuitStats(5, stats);
    assertValues(right, false, true, true, false, false);
  }

  public void testCopyBitsToSmallerSet() {
    stats = new CountingCircuitStats(10, null);
    recordExecutions(stats, 5, i -> true);
    recordExecutions(stats, 5, i -> false);

    stats.nextIndex = 0;
    CountingCircuitStats right = new CountingCircuitStats(4, stats);
    assertValues(right, false, false, false, false);

    stats.nextIndex = 2;
    right = new CountingCircuitStats(4, stats);
    assertValues(right, false, false, true, true);

    stats.nextIndex = 7;
    right = new CountingCircuitStats(4, stats);
    assertValues(right, true, true, false, false);
  }

  public void testCopyBitsToLargerSet() {
    stats = stats(5);
    recordExecutions(stats, 2, i -> true);
    recordExecutions(stats, 3, i -> false);

    stats.nextIndex = 0;
    CountingCircuitStats right = new CountingCircuitStats(6, stats);
    assertValues(right, true, true, false, false, false);

    stats.nextIndex = 2;
    right = new CountingCircuitStats(6, stats);
    assertValues(right, false, false, false, true, true);

    stats.nextIndex = 4;
    right = new CountingCircuitStats(6, stats);
    assertValues(right, false, true, true, false, false);
  }

  private static boolean[] valuesFor(CountingCircuitStats stats) {
    boolean[] values = new boolean[stats.getExecutionCount()];
    for (int i = 0; i < values.length; i++)
      values[i] = stats.bitSet.get(i);
    return values;
  }

  private static void assertValues(CountingCircuitStats bs, boolean... right) {
    boolean[] left = valuesFor(bs);
    assertTrue(Arrays.equals(left, right), Arrays.toString(left) + " != " + Arrays.toString(right));
  }

  private static void recordExecutions(CountingCircuitStats stats, int count,
    Predicate<Integer> successPredicate) {
    for (int i = 0; i < count; i++) {
      if (successPredicate.test(i))
        stats.recordSuccess();
      else
        stats.recordFailure();
    }
  }
}
