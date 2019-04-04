package io.rector.metrics.test;

import io.rector.metrics.Clock;

import java.util.concurrent.TimeUnit;

public class TestClock extends Clock
{
    private long tick;

    public void advance(TimeUnit unit, long time)
    {
        tick += unit.toNanos(time);
    }

    @Override
    public long getTick()
    {
        return tick;
    }
}
