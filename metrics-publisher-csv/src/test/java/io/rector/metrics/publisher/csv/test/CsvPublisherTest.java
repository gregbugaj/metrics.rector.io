package io.rector.metrics.publisher.csv.test;

import io.rector.metrics.Counter;
import io.rector.metrics.MonitorRegistry;
import io.rector.metrics.publisher.csv.CsvPublisher;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class CsvPublisherTest
{

    @Test
    public void test001() throws InterruptedException
    {
        final MonitorRegistry registry = MonitorRegistry.get("test-001");
        final CsvPublisher publisher = CsvPublisher
                    .of(registry, Paths.get("./report.csv"))
                    .interval(1l, TimeUnit.SECONDS)
                    .build();

        publisher.start();

        final Counter c1 = registry.counter("sample.counter.a");
        System.out.println(c1);
        c1.inc();

        System.out.println(c1);



        Thread.sleep(5000);
    }
}
