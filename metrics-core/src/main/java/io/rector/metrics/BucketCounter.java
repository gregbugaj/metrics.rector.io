package io.rector.metrics;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BucketCounter
{
    private final long[] limits;

    private final Bucket[] buckets;

    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public BucketCounter(final long... limits)
    {
        for (int i = 1, n = limits.length; i < n; ++i)
        {
            if (limits[i - 1] >= limits[i])
                throw new MonitoringException("Bounds need to be in ascending order");
        }

        this.limits = limits;
        this.buckets = new DefaultBucket[limits.length];

        for (int i = 0, n = limits.length; i < n; ++i)
        {
            this.buckets[i] = new DefaultBucket();
        }
    }

    public void add(final long time)
    {
        final Bucket bucket = buckets[findBucket(time)];

        lock.writeLock().lock();
        try
        {
            bucket.add(time);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public int findBucket(final long time)
    {
        int low = 0;
        int high = limits.length - 1;
        int i = 0;

        while (low <= high)
        {
            i = (low + high) / 2;
            final long d = limits[i] - time;

            if (d < 0)
            {
                low = i + 1;
            }
            else if (d > 0)
            {
                high = i - 1;
            }
            else
            {
                return i;
            }
        }

        // Bound check < min  return -1, > than max lim return i
        return (limits[i] >= time) ? i - 1 : i;
    }

    public static BucketCounter createSpacedEvenly(final long lower, final long upper, final int count)
    {
        if (count <= 0)
            throw new IllegalArgumentException("Counter needs to be greater than 0");

        final long[] limits = new long[count];
        final long d = (upper - lower) / (count - 1);

        for (int i = 0; i < count; ++i)
        {
            limits[i] = d * i;
        }

        return new BucketCounter(limits);
    }

    public static BucketCounter createSpacedOverInterval(final long lower, final long upper, final int interval)
    {
        if (interval <= 0)
            throw new IllegalArgumentException("Interval needs to be greater than 0");

        final int count = (int) ((upper - lower) / interval);
        final long[] limits = new long[count + 1];

        for (int i = 0; i <= count; ++i)
        {
            limits[i] = interval * i;
        }

        return new BucketCounter(limits);
    }

    public void update()
    {
        for (final Bucket bucket : buckets)
        {
            bucket.getSnapshot();
        }
    }

    public void update(final int bucket)
    {
         getSnapshot(bucket);
    }

    /**
     * Get given snapshot recalculated at this given time
     * @param bucket to update
     * @return updated {@link Snapshot}
     */
    public Snapshot getSnapshot(final int bucket)
    {
        return buckets[bucket].getSnapshot();
    }

    public Bucket getBucket(final int bucket)
    {
        return buckets[bucket];
    }

}
