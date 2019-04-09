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
public class Apdex implements Monitor<Double>
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

    /**
     * Track supplied action
     * @param action the action to track
     * @param <T> the return type value
     * @return
     */
    public <T> T track(final Supplier<T> action)
    {
        if(action == null)
            return null;

        final long s = clock.getTick();

        try
        {
            return action.get();
        }
        finally
        {
            _track(clock.getTick() - s);
        }
    }

    /**
     * Add new data point with given duration with time conversion applied from supplied {@link ApdexOptions}
     * @param duration the duration in TimeUnit from {@link ApdexOptions}
     */
    public void track(final long duration)
    {
        if(duration < 0)
            return ;

        final TimeUnit unit = options.getUnit();
        final long durationInNanos = unit.toNanos(duration);

        _track(durationInNanos);
    }

    private void _track(final long durationInNanon)
    {
        if(durationInNanon < 0)
            return ;

        provider.update(durationInNanon);

        if(log.isDebugEnabled())
            log.debug("apdex track ::{} ms | {} s", durationInNanon, TimeUnit.NANOSECONDS.toMillis(durationInNanon));
    }

    /**
     * Add new data point with supplied duration
     * @param unit
     * @param duration
     */
    public void track(final TimeUnit unit, final long duration)
    {
        _track(unit.toNanos(duration));
    }

    /**
     * Obtain new {@link ApdexContext}
     * @return
     */
    public ApdexContext newContext()
    {
        return new ApdexContext(clock, provider);
    }

    /**
     * Get data snapshot {@link ApdexSnapshot}
     * @return
     */
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

    /**
     * Get the score for this Apdex
     *  0 : Failing
     *  1 : Satisfied
     * ( Satisfied requests + ( Tolerating requests / 2 ) ) ) / Total number of requests
     * @return score in 0..1 range
     */
    @Override
    public Double getValue()
    {
        final ApdexSnapshot snapshot = getSnapshot();
        if(snapshot == null || snapshot.getSize() == 0)
            return 0.0D;

        final int satisfied = snapshot.getSatisfiedSize();
        final int tolerating = snapshot.getToleratingSize();
        final int total = snapshot.getSize();
        double score = (satisfied + (tolerating / 2.0)) / total;

        return score;
    }

    @Override
    public Type getType()
    {
        return Type.NUMBER;
    }

    @Override
    public MetricType getMonitorType()
    {
        return MetricType.APDEX;
    }
}
