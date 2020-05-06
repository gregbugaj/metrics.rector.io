package io.rector.metrics.publisher.csv.test;

import io.rector.metrics.*;
import io.rector.metrics.publisher.csv.CsvPublisher;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class CsvPublisherTest
{
    @Test
    public void counterTest001() throws InterruptedException
    {
        final MonitorRegistry registry = MonitorRegistry.get("test-001");

        final Publisher publisher = CsvPublisher
                .of(registry, Paths.get("./report.csv"))
                .withInterval(2l, TimeUnit.SECONDS)
                .withResetOnReporting(false)
                .build();

        publisher.start();

        final Counter c1 = registry.counter("sample.counter.requests");

        while (true)
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

        final Publisher publisher = CsvPublisher
                .of(registry, Paths.get("./report.csv"))
                .withInterval(2l, TimeUnit.SECONDS)
                .withResetOnReporting(false)
                .build();

        publisher.start();

        final Gauge gauge = registry.gauge("sample.gauge.memory");

        while (true)
        {
            System.out.println(gauge);
            gauge.setValue(System.currentTimeMillis());
            Thread.sleep(500);
        }
    }

    @Test
    public void apdexTest001() throws InterruptedException
    {
        final MonitorRegistry registry = MonitorRegistry.get("test-001");
        StringWriter sw = new StringWriter();
        final PrintWriter writer = new PrintWriter(sw, true);

        final Publisher publisherxx = CsvPublisher
                .of(registry, writer)
                .withInterval(100L, TimeUnit.MILLISECONDS)
                .withResetOnReporting(false)
                .build();

        final Publisher publisher = ConsolePublisher
                .of(registry)
                .withInterval(100L, TimeUnit.MILLISECONDS)
                .withResetOnReporting(false)
                .build();

        publisher.start();

        final Apdex apdex = registry.apdex("metric.apdex", ApdexOptions.of(100, TimeUnit.MILLISECONDS));
        final Gauge frustrating = registry.gauge("metric.apdex.frustrating");
        final Gauge satisfied = registry.gauge("metric.apdex.satisfied");
        final Gauge gauge = registry.gauge("metric.apdex.tolerating");

        int counter = 0;
        while (counter++ < 20)
        {
            try (final ApdexContext context = apdex.newContext())
            {
                final SecureRandom sr = new SecureRandom();
                final int sleep = sr.nextInt(450);

                final ApdexSnapshot snapshot = apdex.getSnapshot();
                frustrating.setValue(snapshot.getFrustratingSize());
                satisfied.setValue(snapshot.getSatisfiedSize());
                gauge.setValue(snapshot.getToleratingSize());

                Thread.sleep(sleep);
            }
        }
        final ApdexSnapshot snapshot = apdex.getSnapshot();
        System.out.println(snapshot);
        System.out.println(sw);
    }
}
