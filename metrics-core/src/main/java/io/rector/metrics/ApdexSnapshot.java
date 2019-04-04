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
        this.size = values.length;
        final long durationInNanos = TimeUnit.MILLISECONDS.toNanos(durationInMillis);
        final long factorOfFour = 4 * durationInNanos;

        // values are in ml so need to convert them nanos
        for(int  i= 0; i < values.length;++i)
        {
            long val = values[i];
            long t = TimeUnit.MILLISECONDS.toNanos(val);

            if(t <= durationInNanos)
                satisfiedSize++;
            else if(t > factorOfFour)
                frustratingSize++;
            else  if(t > durationInNanos && t <= factorOfFour)
                toleratingSize++;
        }
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
