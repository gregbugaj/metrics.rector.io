package io.rector.metrics;

import java.util.concurrent.TimeUnit;

/**
 * A timer metric which aggregates timing durations
 * codahale
 */
public class Timer implements Monitor<Long>
{
    /**
     * A timing context.
     *
     * @see Timer#time()
     */
    public static class Context implements AutoCloseable
    {
        private final Timer timer;

        private final Clock clock;

        private final long startTime;

        private Context(final Timer timer, final Clock clock)
        {
            this.timer = timer;
            this.clock = clock;
            this.startTime = clock.getTick();
        }

        /**
         * Updates the timer with the difference between current and start time. Call to this method will
         * not reset the start time. Multiple calls result in multiple updates.
         *
         * @return the elapsed time in nanoseconds
         */
        public long stop()
        {
            final long elapsed = clock.getTick() - startTime;
            timer.update(elapsed, TimeUnit.NANOSECONDS);
            return elapsed;
        }

        /**
         * Equivalent to calling {@link #stop()}.
         */
        @Override
        public void close()
        {
            stop();
        }
    }

    private final Clock clock;

    public Timer()
    {
        this.clock = Clock.defaultClock();
    }

    /**
     * Returns a new {@link Context}.
     *
     * @return a new {@link Context}
     * @see Context
     */
    public Context time()
    {
        return new Context(this, clock);
    }

    /**
     * Adds a recorded duration.
     *
     * @param duration the length of the duration
     * @param unit     the scale unit of {@code duration}
     */
    public void update(long duration, TimeUnit unit)
    {
        update(unit.toNanos(duration));
    }

    private void update(long duration)
    {
        if (duration >= 0)
        {
            //histogram.update(duration);
            //meter.mark();
        }
    }

    @Override
    public Long getValue()
    {
        return 0L;
    }

    @Override
    public Type getType()
    {
        return Type.NUMBER;
    }

    @Override
    public MetricType getMonitorType()
    {
        return MetricType.TIMER;
    }
}
