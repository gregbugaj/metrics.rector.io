package io.rector.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Sliding window reservoir that stores only the measurements made in the last N seconds
 */
public class SlidingWindowReservoir implements Reservoir
{
    private long count;

    private final long[] values;

    public SlidingWindowReservoir(int size)
    {
        this.values = new long[size];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = 0;
        }

        count = 0;
    }

    @Override
    public int size()
    {
        return (int) Math.min(count, values.length);
    }

    @Override
    public void update(final long value)
    {
        long cnt = ++count;
        int index  = (int) ((cnt - 1) % values.length);
        this.values[index] = value;
    }

    @Override
    public Snapshot getSnapshot()
    {
        final int s = size();
        if(s == 0)
            return new UniformSnapshot(new long[0]);

        final List<Long> copy = new ArrayList<>(s);
        for (int i = 0; i < s; i++)
        {
            copy.add(values[i]);
        }
        return new UniformSnapshot(copy);
    }

}
