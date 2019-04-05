package io.rector.metrics;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

public class ApdexSnapshot
{
    private int frustratingSize;

    private int satisfiedSize;

    private int toleratingSize;

    private int size;

    public ApdexSnapshot(final Snapshot snapshot, final long nanos)
    {
        this(snapshot.getValues(), nanos);
    }

    public ApdexSnapshot(final long[] values, final long durationInNanos)
    {
        this.size = values.length;
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

    /**
     * Get number of frustrating requests
     * @return
     */
    public int getFrustratingSize()
    {
        return frustratingSize;
    }

    /**
     * Get number of satisfied requests
     * @return
     */
    public int getSatisfiedSize()
    {
        return satisfiedSize;
    }

    /**
     * Get number of tolerating requests
     * @return
     */
    public int getToleratingSize()
    {
        return toleratingSize;
    }

    /**
     * Get sample size
     * @return
     */
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
