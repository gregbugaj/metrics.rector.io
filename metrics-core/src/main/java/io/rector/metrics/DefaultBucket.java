package io.rector.metrics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultBucket implements Bucket
{
    private final List<Long> values;

    private long runningTotal = 0;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public DefaultBucket()
    {
        values = new ArrayList<>(1024);
    }

    @Override
    public void add(final long value)
    {
        lock.writeLock().lock();
        try
        {
            values.add(value);
            runningTotal += value;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Snapshot getSnapshot()
    {
        lock.readLock().lock();

        try
        {
            final int size = values.size();

            if(size == 0)
                throw new IllegalStateException();

            final double mean = runningTotal / size;
            long total = 0;

            for (final long val : values)
            {
                total += Math.abs(val - mean);
            }

            final double mad = total / size;


            return new UniformSnapshot(values);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public Iterator<Long> iterator()
    {
        return values.iterator();
    }

    @Override
    public int size()
    {
        lock.readLock().lock();
        try
        {
            return values.size();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
}
