package io.rector.metrics.metrics;

/**
 * Monitor of specific type
 *
 * @param <T> type of the value we are monitoring
 */
public interface Monitor<T>
{
    /**
     * Returns the current value for the monitor
     */
    T getValue();

    /**
     * Get value type of this monitor
     *
     * @return
     */
    Type getType();

    /**
     * Return metric type of the monitor
     * 
     * @return {@link MetricType}
     */
    MetricType getMonitorType();
}
