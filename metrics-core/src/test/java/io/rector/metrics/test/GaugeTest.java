package io.rector.metrics.test;

import io.rector.metrics.Gauge;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GaugeTest
{

    @Test
    public void supplierTest() throws Exception
    {
        Gauge gauge =  new Gauge();
        gauge.setValue(10L);

        assertThat(gauge.getValue(), is(10));

        gauge.setValue(5);
        assertThat(gauge.getValue(), is(5));
    }

    @Test
    public void gageValue() throws Exception
    {
        List<Integer> items = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        Gauge gauge = new Gauge();
        gauge.setValue(items.size());

        assertThat(gauge.getValue(),is(10));
    }
}
