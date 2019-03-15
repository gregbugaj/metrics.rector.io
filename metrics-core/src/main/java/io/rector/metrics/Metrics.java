package io.rector.metrics;

public class Metrics
{
    public static Counter newCounter()
    {
        return new Counter();
    }

    public static Gauge newGauge()
    {
        return new Gauge();
    }

    public static Monitor<?>  newTimer()
    {
        return null;
    }

}
