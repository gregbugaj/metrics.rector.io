package io.rector.metrics;

public interface Bucket extends Iterable<Long>
{
    /**
     * Add new value to the bucket
     * @param value
     */
    void add(final long value);

    /**
     * Size of the bucket
     *
     * @return number of items in the bucket
     */
    int size();

    /**
     * Get snapshot 
     * @return
     */
    Snapshot getSnapshot();
}
