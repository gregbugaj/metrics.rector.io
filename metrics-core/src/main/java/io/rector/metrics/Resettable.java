package io.rector.metrics;

/**
 * Interface indicating that the monitor can be reset
 */
public interface Resettable
{
    /**
     * Reset metric that implements resettable
     */
    void reset();
}
