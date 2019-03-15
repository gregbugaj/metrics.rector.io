package io.rector.metrics.publisher.csv.test;

import io.rector.metrics.Counter;
import io.rector.metrics.Gauge;
import io.rector.metrics.MonitorRegistry;
import io.rector.metrics.publisher.csv.CsvPublisher;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class CsvPublisherTest
{

    @Test
    public void counterTest001() throws InterruptedException
    {
        final MonitorRegistry registry = MonitorRegistry.get("test-001");

        final CsvPublisher publisher = CsvPublisher
                    .of(registry, Paths.get("./report.csv"))
                    .withInterval(2l, TimeUnit.SECONDS)
                    .withResetOnReporting(false)
                    .build();

        publisher.start();

        final Counter c1 = registry.counter("sample.counter.requests");

        while(true)
        {
            c1.increment();
            System.out.println(c1);
            Thread.sleep(500);
        }
    }

    @Test
    public void gaugeTest001() throws InterruptedException
    {
        final MonitorRegistry registry = MonitorRegistry.get("test-001");

        final CsvPublisher publisher = CsvPublisher
                .of(registry, Paths.get("./report.csv"))
                .withInterval(2l, TimeUnit.SECONDS)
                .withResetOnReporting(false)
                .build();

        publisher.start();

        Gauge gauge = registry.gauge("sample.gauge.memory");

        while(true)
        {
            System.out.println(gauge);
            gauge.setValue(System.currentTimeMillis());
            Thread.sleep(500);
        }
    }
}
