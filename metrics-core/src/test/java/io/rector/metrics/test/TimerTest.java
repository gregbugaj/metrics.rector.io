package io.rector.metrics.test;

import io.rector.metrics.Gauge;
import io.rector.metrics.Timer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TimerTest
{

    @Test
    public void supplierTest() throws Exception
    {
        Timer timer = new Timer();

        try(Timer.Context context = timer.time())
        {

        }
    }

}
