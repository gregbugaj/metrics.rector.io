package io.rector.metrics.test;

import io.rector.metrics.Message;
import io.rector.metrics.Publisher;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorConfigTest
{

    @Test
    public void duplicateGaugeRegistration() throws InterruptedException
    {

/*
        final Monitor<Number> gauge1 = new Gauge(MonitorConfig.builder("counter_1")
            .interval(1, TimeUnit.SECONDS).build())
        {
            private int fired;

            @Override
            public Number getValue()
            {
                System.out.println("firing : " + fired);
                return ++fired;
            }
        };

        Monitors.register(gauge1);
        Monitors.register(gauge1);*/

        Thread.sleep(5000);
    }

    //    @Test
    public void testNumberGaugeConfig() throws InterruptedException
    {
/*
        final Monitor<Number> gauge1 = new Gauge(MonitorConfig.builder("counter_1")
            .interval(1, TimeUnit.SECONDS).build())
        {
            private int fired;

            @Override
            public Number getValue()
            {
                System.out.println("firing : " + fired);
                return ++fired;
            }
        };

        Monitors.register(gauge1);*/
        Thread.sleep(5000);
    }

    public static class CountingPublisher implements Publisher
    {
        private static final Logger logger = LoggerFactory.getLogger(CountingPublisher.class);

        @Override public void start()
        {

        }
    }
}
