package io.rector.metrics.metrics;

/**
 * Type of the monitor
 */
public enum MetricType
{
    GAUGE,
    COUNTER,
    TIMER,
    HISTOGRAM,
    COMPOSITE
}