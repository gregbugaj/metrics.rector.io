package io.rector.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Application Performance Index Monitor
 *
 * The Apdex score is between 0 and 1 is calculated using the following:
 * <pre>
 *     ( Satisfied requests + ( Tolerating requests / 2 ) ) ) / Total number of requests
 * </pre>
 *
 * Apdex provides three thresholds estimating end user satisfaction, satisfied, tolerating and frustrating.
 *
 * <ul>
 *     <li>Satisfied: Response time less than or equal to T seconds.</li>
 *     <li>Tolerating: Response time between T seconds and 4T seconds.</li>
 *     <li>Frustrating: Response time greater than 4 T seconds.</li>
 * </ul>
 *
 */
public class Apdex
{
    private static final Logger log = LoggerFactory.getLogger(Apdex.class);

    private Clock clock;

    private ApdexProvider provider;

    public Apdex(int size)
    {
        this(size, Clock.defaultClock());
    }

    public Apdex(int size, final Clock clock)
    {
        Objects.requireNonNull(clock);
        if (size < 0)
            throw new IllegalArgumentException("Size needs to be greater than 0 got " + size);

        Reservoir reservoir;

        if (size == 0)
            reservoir = new UniformReservoir();
        else
            reservoir = new SlidingWindowReservoir(size);

        final long seconds  = 5;
        this.clock = clock;
        this.provider = new ApdexProvider(reservoir, seconds);
    }

    public static ApdexContext track(final String tag)
    {

        return new ApdexContext();
    }

    public <T> T track(final Supplier<T> action)
    {
        Objects.requireNonNull(action);
        final long s = clock.getTick();
        try
        {
            return action.get();
        }
        finally
        {
            track(clock.getTick() - s);
        }
    }

    public void track(final long duration)
    {
        if(duration < 0)
            return ;

        provider.update(duration);

        if(log.isDebugEnabled())
            log.debug("apdex track ::{} ms | {} s", duration, TimeUnit.NANOSECONDS.toSeconds(duration));
    }
}
