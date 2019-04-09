package io.rector.metrics.publisher.timescaledb;

import io.rector.metrics.MonitorRegistry;

import java.util.concurrent.TimeUnit;

public class ApdexTimescaleDbPublisherTest
{
    public static void main(String[] args) throws InterruptedException
    {
        new ApdexTimescaleDbPublisherTest().run(args);
    }

    private void run(final String[] args) throws InterruptedException
    {
        final MonitorRegistry registry = MonitorRegistry.get("test-001");

        final DatabaseConfig options = new DatabaseConfig();
        options
                .setUsername("service")
                .setPassword("service")
                .setUrl("jdbc:postgresql://localhost/telemetry")
                .setDriverClassName("org.postgresql.Driver")
                .setMaximumPoolSize(10)
                .setSkipConnectionTest(false);

        final TimescaleDbPublisher publisher =   TimescaleDbPublisher
                .with(registry, options)
                .withInterval(2l, TimeUnit.SECONDS)
                .withResetOnReporting(false)
                .build();

        publisher.start();

        Thread.currentThread().join();

     /*   final Gauge gauge = registry.gauge("sample.gauge.memory");

        while(true)
        {
            System.out.println(gauge);
            gauge.setValue(System.currentTimeMillis());
            Thread.sleep(500);
        }*/
    }
}
