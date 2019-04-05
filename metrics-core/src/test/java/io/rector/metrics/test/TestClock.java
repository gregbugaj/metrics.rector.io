package io.rector.metrics.test;

import io.rector.metrics.Clock;

import java.util.concurrent.TimeUnit;

/**
 * Test clock used for testing
 */
public class TestClock extends Clock
{
    private long tick;

    /**
     * Advance clock by unit of time
     *
     * @param unit the unit of time
     * @param time the time to advance
     */
    public void advance(final TimeUnit unit, long time)
    {
        tick += unit.toNanos(time);
    }

    /**
     * Advance clock by time in nanoseconds
     * @param timeInNanos
     */
    public void advance(long timeInNanos)
    {
        tick += timeInNanos;
    }

    @Override
    public long getTick()
    {
        return tick;
    }
}
