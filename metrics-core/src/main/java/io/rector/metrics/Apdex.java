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

    private final int size;

    private final ApdexOptions options;

    private Clock clock;

    private ApdexProvider provider;

    public Apdex(int size, final ApdexOptions options)
    {
        this(size, options, Clock.defaultClock());
    }

    public Apdex(int size, final ApdexOptions options, final Clock clock)
    {
        if (size < 0)
            throw new IllegalArgumentException("Size needs to be greater than 0 got " + size);

        Objects.requireNonNull(clock);
        Objects.requireNonNull(options);

        this.size = size;
        this.clock = clock;
        this.options = options;
        this.provider = new ApdexProvider(createReservoir(size), options);
    }

    private Reservoir createReservoir(int size)
    {
        if (size == 0)
            return  new UniformReservoir();
        else
            return  new SlidingWindowReservoir(size);
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

    public ApdexContext newContext()
    {
        return new ApdexContext(clock, provider);
    }

    public ApdexSnapshot getSnapshot()
    {
        return provider.getSnapshot();
    }

    /**
     * Reset current state
     */
    public void reset()
    {
        provider = new ApdexProvider(createReservoir(size), options);
    }
}
