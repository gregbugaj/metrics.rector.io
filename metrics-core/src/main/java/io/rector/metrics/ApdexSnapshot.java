package io.rector.metrics;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

public class ApdexSnapshot
{
    private int frustratingSize;

    private int satisfiedSize;

    private int toleratingSize;

    private int size;

    public ApdexSnapshot(final Snapshot snapshot, final long millis)
    {
        this(snapshot.getValues(), millis);
    }

    public ApdexSnapshot(final long[] values, final long durationInMillis)
    {
        size = values.length;
        final long durationInNanos = TimeUnit.MILLISECONDS.toNanos(durationInMillis);
        final long factorOfFour = 4 * durationInNanos;

        satisfiedSize = (int) LongStream.of(values).filter(t -> t <= durationInNanos).count();
        frustratingSize = (int) LongStream.of(values).filter(t -> t > factorOfFour).count();
        toleratingSize = (int) LongStream.of(values).filter(t -> t > durationInNanos && t <= factorOfFour).count();
    }

    public int getFrustratingSize()
    {
        return frustratingSize;
    }

    public int getSatisfiedSize()
    {
        return satisfiedSize;
    }

    public int getToleratingSize()
    {
        return toleratingSize;
    }


    public int getSize()
    {
        return size;
    }

    @Override
    public String toString()
    {
        return String.format("size, satisfied, tolerating, frustrating [%s, %s, %s, %s]",
                             size, satisfiedSize, toleratingSize, frustratingSize);
    }

}
