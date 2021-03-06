package io.rector.metrics;

import java.util.Arrays;
import java.util.Random;

/**
 * A random sampling reservoir of a stream of {@code long}s. Uses Vitter's
 * Algorithm R to produce a statistically representative sample.
 *
 * @see <a href="http://www.cs.umd.edu/~samir/498/vitter.pdf">Random Sampling
 *      with a Reservoir</a>
 */
public class UniformReservoir implements Reservoir
{
    private static final int DEFAULT_SIZE = 1028;
    private static final int BITS_PER_LONG = 63;
    private long count;
    private final long[] values;

    /**
     * Creates a new {@link UniformReservoir} of 1028 elements, which offers a
     * 99.9% confidence level with a 5% margin of error assuming a normal
     * distribution.
     */
    public UniformReservoir()
    {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a new {@link UniformReservoir}.
     *
     * @param size
     *            the number of samples to keep in the sampling reservoir
     */
    public UniformReservoir(final int size)
    {
        this.values = new long[size];
        Arrays.fill(values, 0);
        count = 0;
    }

    @Override
    public int size()
    {
        final long c = count;
        if (c > values.length){
            return values.length;
        }
        return (int) c;
    }

    @Override
    public void update(final long value) {
        count = count + 1;
        final long c = count;
        if (c <= values.length) {
            values[(int) c - 1] = value;
        } else {
            final long r = nextLong(c);
            if (r < values.length) {
                values[(int) r] = value;
            }
        }
    }

    /**
     * Get a pseudo-random long uniformly between 0 and n-1. Stolen from
     * {@link java.util.Random#nextInt()}.
     *
     * @param n
     *            the bound
     * @return a value select randomly from the range {@code [0..n)}.
     */
    private static long nextLong(final long n) {
        long bits, val;
        do {
            bits = new Random().nextLong() & (~(1L << BITS_PER_LONG));
            val = bits % n;
        } while (bits - val + (n - 1) < 0L);
        return val;
    }

    @Override
    public Snapshot getSnapshot()
    {
        return new UniformSnapshot(values);
    }
}
