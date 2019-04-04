package io.rector.metrics;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ApdexProvider
{
    private Reservoir reservoir;

    private long millis;

    public ApdexProvider(final Reservoir reservoir, final ApdexOptions options)
    {
        Objects.requireNonNull(reservoir);
        Objects.requireNonNull(options);

        final long duration = options.getDuration();
        final TimeUnit unit = options.getUnit();

        this.reservoir = reservoir;
        this.millis = unit.toMillis(duration);
    }

    public void update(long value)
    {
        reservoir.update(value);
    }

    public ApdexSnapshot getSnapshot()
    {
        final Snapshot snapshot = reservoir.getSnapshot();
        return new ApdexSnapshot(snapshot.getValues(), millis);
    }
}
