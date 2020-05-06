package io.rector.metrics;

import java.util.concurrent.TimeUnit;

public abstract class CorePublisherBuilder<T>
{
    protected long time;

    protected TimeUnit unit;

    protected boolean resetOnReporting;

    public T withInterval(long time, TimeUnit unit)
    {
        this.time = time;
        this.unit = unit;
        return (T) this;
    }

    public T withResetOnReporting(boolean reset)
    {
        this.resetOnReporting = reset;
        return (T) this;
    }
}
