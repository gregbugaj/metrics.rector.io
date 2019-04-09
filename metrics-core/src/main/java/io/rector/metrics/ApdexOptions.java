package io.rector.metrics;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Duration options
 */
public class ApdexOptions
{
    /**
     * Duration in nanoseconds
     */
    private final long duration;

    private final TimeUnit unit;

    public ApdexOptions(long duration, TimeUnit unit)
    {
        this.duration = unit.toNanos(duration);
        this.unit = unit;
    }

    public ApdexOptions(final Duration duration)
    {
        this(duration.getNano(), TimeUnit.NANOSECONDS);
    }

    public static ApdexOptions of(long duration, final TimeUnit unit)
    {
        return new ApdexOptions(duration, unit);
    }

    public static ApdexOptions of(final Duration duration)
    {
        return new ApdexOptions(duration);
    }

    /**
     * Get duration in nanoseconds
     * @return
     */
    public long getDuration()
    {
        return duration;
    }

    public TimeUnit getUnit()
    {
        return unit;
    }


}
