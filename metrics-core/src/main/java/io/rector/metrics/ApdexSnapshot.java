package io.rector.metrics;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

public class ApdexSnapshot
{
    private int frustratingSize;

    private int satisfiedSize;

    private int toleratingSize;

    public ApdexSnapshot(final Snapshot snapshot, final long apdexTSeconds)
    {
        this(snapshot.getValues(), apdexTSeconds);
    }

    public ApdexSnapshot(final long[] values, final long durationInSeconds)
    {
        final long durationInNanos = TimeUnit.SECONDS.toNanos(durationInSeconds);
        final double factorOfFour = 4.0 * durationInNanos;

        frustratingSize = (int) LongStream.of(values).filter(t -> t > factorOfFour).count();
        satisfiedSize = (int) LongStream.of(values).filter(t -> t < durationInNanos).count();
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
}
