package io.rector.metrics;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class ApdexContext implements AutoCloseable
{
    private final ApdexProvider provider;

    private final Clock clock;

    private long start;

    public ApdexContext(final Clock clock, final ApdexProvider provider)
    {
        this.provider = Objects.requireNonNull(provider);
        this.clock = Objects.requireNonNull(clock);
        this.start = clock.getTick();
    }

    @Override
    public void close()
    {
        long duration = clock.getTick() - start;
        provider.update(duration);
    }

    /**
     * Gets the currently elapsed time from when the instance has been created
     *
     * @return elapsed time in nanoseconds
     */
    public Duration elapsed()
    {
        return Duration.of(clock.getTick() - start, ChronoUnit.NANOS);
    }
}
