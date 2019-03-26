package io.rector.metrics;

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
