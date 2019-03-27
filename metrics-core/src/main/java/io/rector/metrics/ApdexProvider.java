package io.rector.metrics;

public class ApdexProvider
{
    private Reservoir reservoir;

    private long apdexTSeconds;

    public ApdexProvider(final Reservoir reservoir, final long seconds)
    {
        this.reservoir = reservoir;
        this.apdexTSeconds = seconds;
    }

    public void update(long value)
    {
        reservoir.update(value);
    }

    public ApdexSnapshot getSnapshot()
    {
        final Snapshot snapshot = reservoir.getSnapshot();

        return new ApdexSnapshot(snapshot.getValues(), apdexTSeconds);
    }
}
