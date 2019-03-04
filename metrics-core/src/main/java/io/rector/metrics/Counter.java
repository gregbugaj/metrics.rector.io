package io.rector.metrics;

import java.util.concurrent.atomic.LongAdder;

import static io.rector.metrics.MetricType.COUNTER;
import static io.rector.metrics.Type.NUMBER;

public class Counter implements Monitor<Number>
{
    private final LongAdder count;

    public Counter()
    {
        count = new LongAdder();
    }

    @Override
    public Number getValue()
    {
        return count.longValue();
    }

    @Override
    public Type getType()
    {
        return NUMBER;
    }

    @Override
    public MetricType getMonitorType()
    {
        return COUNTER;
    }

    /**
     * Increment counter value by one
     */
    public void inc()
    {
        inc(1);
    }

    /**
     * Increment count by {@code n}
     * @param n  the amount by which the counter will be incremented
     */
    public void inc(long n) {
        count.add(n);
    }

    /**
     * Decrement value by 1
     */
    public void dec()
    {
        dec(1);
    }

    /**
     * Decrement value by {@code n}
     * @param n the amount by which the counter will be decremented
     */
    public void dec(long n)
    {
        count.add(-n);
    }

    @Override
    public String toString()
    {
        return getMonitorType()+ " : " + getValue();
    }
}
