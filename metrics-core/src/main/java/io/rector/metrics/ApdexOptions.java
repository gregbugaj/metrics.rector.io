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

    private String name;

    public ApdexOptions(final String name, long duration, TimeUnit unit)
    {
        this.duration = unit.toNanos(duration);
        this.name = name;
        this.unit = unit;
    }

    public ApdexOptions(final String name, final Duration duration)
    {
        this(name, duration.getNano(), TimeUnit.NANOSECONDS);
    }

    public static ApdexOptions of(final String name, long duration, final TimeUnit unit)
    {
        return new ApdexOptions(name, duration, unit);
    }

    public static ApdexOptions of(final String name, final Duration duration)
    {
        return new ApdexOptions(name, duration);
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
