package io.rector.metrics.publisher.timescaledb;

import io.rector.metrics.Apdex;
import io.rector.metrics.ApdexContext;
import io.rector.metrics.ApdexOptions;
import io.rector.metrics.MonitorRegistry;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ApdexTimescaleDbPublisherTest
{
    public static void main(String[] args) throws InterruptedException
    {
        new ApdexTimescaleDbPublisherTest().run(args);
    }

    private void run(final String[] args) throws InterruptedException
    {
        final MonitorRegistry registry = MonitorRegistry.get("apdex-001");

        final DatabaseConfig options = new DatabaseConfig();
        options
                .setUsername("service")
                .setPassword("service")
                .setUrl("jdbc:postgresql://localhost:5432/telemetry")
                .setDriverClassName("org.postgresql.Driver")
                .setMaximumPoolSize(10)
                .setSkipConnectionTest(false);

        final TimescaleDbPublisher publisher =   TimescaleDbPublisher
                .with(registry, options)
                .withInterval(2l, TimeUnit.SECONDS)
                .withResetOnReporting(false)
                .build();

        publisher.start();

        final Apdex apdex = registry.apdex("apdex.sample", ApdexOptions.of(100, TimeUnit.MILLISECONDS));

        int counter = 0;
        while(counter++ < 100)
        {
            try(final ApdexContext context = apdex.newContext())
            {
                final SecureRandom sr = new SecureRandom();
                final int sleep = sr.nextInt(350);
                final Duration elapsed = context.elapsed();
                System.out.println("Elapsed : " + elapsed);
                Thread.sleep(sleep);
            }
        }
    }
}
