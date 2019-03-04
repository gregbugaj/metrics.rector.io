package io.rector.metrics.metrics;

public class Metrics
{
    public static Counter newCounter()
    {
        return new Counter();
    }

    public static Gauge<?> newGauge()
    {
        throw new RuntimeException("Not yet implemented");
    }

    public static Monitor<?>  newTimer()
    {
        return null;
    }

}
