package io.rector.metrics;

/**
 * Application Performance Index Monitor
 * The Apdex score is between 0 and 1 is calculated using the following:
 * <pre>
 *     ( Satisfied requests + ( Tolerating requests / 2 ) ) ) / Total number of requests
 * </pre>
 */
public class Apdex implements Monitor<Number>, AutoCloseable
{
    public Apdex()
    {

    }

    @Override
    public Number getValue()
    {
        return null;
    }

    @Override
    public Type getType()
    {
        return null;
    }

    @Override
    public MetricType getMonitorType() {
        return null;
    }

    @Override
    public void close() throws Exception
    {
        // NoOp
    }
}
