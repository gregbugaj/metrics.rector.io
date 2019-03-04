package io.rector.metrics.test;

import io.rector.metrics.metrics.Counter;
import org.junit.jupiter.api.Test;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CounterTest
{
    private final Counter counter = new Counter();

    @Test
    public void startsAtZero() throws Exception
    {
        assertThat(counter.getValue(),is(0L));
    }

    @Test
    public void incrementsByOne() throws Exception {
        counter.inc();
        assertThat(counter.getValue(), is(1L));
    }

    @Test
    public void incrementsByAnArbitraryDelta() throws Exception
    {
        counter.inc(12);
        assertThat(counter.getValue(), is(12L));
    }

    @Test
    public void decrementsByOne() throws Exception {
        counter.dec();
        assertThat(counter.getValue(), is(-1L));
    }

    @Test
    public void decrementsByAnArbitraryDelta() throws Exception {
        counter.dec(12);
        assertThat(counter.getValue(), is(-12L));
    }

    @Test
    public void incrementByNegativeDelta() throws Exception {
        counter.inc(-12);
        assertThat(counter.getValue(), is(-12L));
    }

    @Test
    public void decrementByNegativeDelta() throws Exception {
        counter.dec(-12);
        assertThat(counter.getValue(), is(12L));
    }
}
