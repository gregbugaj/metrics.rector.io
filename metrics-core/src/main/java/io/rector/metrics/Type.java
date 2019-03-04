package io.rector.metrics;

/**
 * Type value of collected metric
 * This is a hint to the aggregator on how to process this value.
 *
 * @see  Gauge
 * @see Counter
 */
public enum Type
{
    NUMBER,
    STRING
}
