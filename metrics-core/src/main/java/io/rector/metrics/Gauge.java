package io.rector.metrics;


/**
 * A gauge metric is an instantaneous reading of value in time.
 *
 */
public class Gauge implements Monitor<Long>
{
    private long value;

    public void setValue(final long value)
    {
        this.value = value;
    }

    @Override
    public Long getValue()
    {
        return value;
    }

    @Override
    public Type getType()
    {
        return Type.NUMBER;
    }

    @Override
    public MetricType getMonitorType()
    {
        return MetricType.GAUGE;
    }

    @Override
    public String toString()
    {
        return getMonitorType()+ " : " + getValue();
    }
}
