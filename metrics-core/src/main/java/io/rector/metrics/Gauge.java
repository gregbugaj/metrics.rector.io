package io.rector.metrics;

/**
 * A gauge metric is an instantaneous reading of value in time.
 * @param <T> Type of the gauge
 *
 * @see
 */
public interface Gauge<T> extends Monitor<T>
{
    @Override
    default  Type getType()
    {
        return Type.NUMBER;
    }

    @Override
    default MetricType getMonitorType()
    {
        return MetricType.GAUGE;
    }
}
