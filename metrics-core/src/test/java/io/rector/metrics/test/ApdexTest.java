package io.rector.metrics.test;

import io.rector.metrics.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApdexTest
{

    @Test
    void testContextUsage()
    {
        final TestClock clock = new TestClock();
        final ApdexOptions options = ApdexOptions.of(100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        try (final ApdexContext context = apdex.newContext())
        {
            clock.advance(TimeUnit.MILLISECONDS, 20);
        }

        try (final ApdexContext context = apdex.newContext())
        {
            clock.advance(TimeUnit.MILLISECONDS, 20);
            final Duration elapsed = context.elapsed();
            System.out.println(elapsed);
        }

        final ApdexSnapshot snapshot = apdex.getSnapshot();

        assertEquals(2, snapshot.getSize());
        assertEquals(2, snapshot.getSatisfiedSize());
        assertEquals(0, snapshot.getToleratingSize());
        assertEquals(0, snapshot.getFrustratingSize());
    }

    @Test
    void testFunctionUsage()
    {
        final TestClock clock = new TestClock();
        final ApdexOptions options = ApdexOptions.of(100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        // Function
        apdex.track((ctx) ->
                    {
                        clock.advance(TimeUnit.MILLISECONDS, 10);
                        return 1;
                    });

        // Consumer
        apdex.track((ctx) ->
                    {
                        clock.advance(TimeUnit.MILLISECONDS, 10);
                    });

        final ApdexSnapshot snapshot = apdex.getSnapshot();

        assertEquals(2, snapshot.getSize());
        assertEquals(2, snapshot.getSatisfiedSize());
        assertEquals(0, snapshot.getToleratingSize());
        assertEquals(0, snapshot.getFrustratingSize());
    }

    @Test
    void testDirectUsage()
    {
        final TestClock clock = new TestClock();
        final ApdexOptions options = ApdexOptions.of(100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        apdex.track(20);
        final ApdexSnapshot snapshot = apdex.getSnapshot();

        assertEquals(1, snapshot.getSize());
        assertEquals(1, snapshot.getSatisfiedSize());
        assertEquals(0, snapshot.getToleratingSize());
        assertEquals(0, snapshot.getFrustratingSize());
    }

    @Test
    void testMetricCalculationForTolerating()
    {
        final TestClock clock = new TestClock();
        final ApdexOptions options = ApdexOptions.of(100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        apdex.track(120);

        final ApdexSnapshot snapshot = apdex.getSnapshot();
        assertEquals(1, snapshot.getSize());
        assertEquals(0, snapshot.getSatisfiedSize());
        assertEquals(1, snapshot.getToleratingSize());
        assertEquals(0, snapshot.getFrustratingSize());
    }

    @Test
    void testMetricCalculationForFrustrating()
    {
        final TestClock clock = new TestClock();
        final ApdexOptions options = ApdexOptions.of(100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        // 4 * current request size  + 10
        apdex.track(4 * 100 + 10);
        final ApdexSnapshot snapshot = apdex.getSnapshot();

        assertEquals(1, snapshot.getSize());
        assertEquals(0, snapshot.getSatisfiedSize());
        assertEquals(0, snapshot.getToleratingSize());
        assertEquals(1, snapshot.getFrustratingSize());
    }

    @Test
    void trackAfterExceptionFromAction()
    {
        final TestClock clock = new TestClock();
        final ApdexOptions options = ApdexOptions.of(100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        Throwable throwable = null;
        try
        {
            apdex.track((Consumer<ApdexContext>) (ctx) -> {
                throw new IllegalStateException();
            });
        }
        catch (final Exception ex)
        {
            throwable = ex;
        }

        assertNotNull(throwable);

        final ApdexSnapshot snapshot = apdex.getSnapshot();

        assertEquals(1, snapshot.getSize());
        assertEquals(1, snapshot.getSatisfiedSize());
        assertEquals(0, snapshot.getToleratingSize());
        assertEquals(0, snapshot.getFrustratingSize());
    }

    @Test
    void trackAfterExceptionFromContext()
    {
        final TestClock clock = new TestClock();
        final ApdexOptions options = ApdexOptions.of(100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        Throwable throwable;
        try
        {
            try (final ApdexContext context = apdex.newContext())
            {
                throw new IllegalStateException();
            }
        }
        catch (final Exception ex)
        {
            throwable = ex;
        }

        assertNotNull(throwable);

        final ApdexSnapshot snapshot = apdex.getSnapshot();

        assertEquals(1, snapshot.getSize());
        assertEquals(1, snapshot.getSatisfiedSize());
        assertEquals(0, snapshot.getToleratingSize());
        assertEquals(0, snapshot.getFrustratingSize());
    }

    @Test
    void testReset()
    {
        final TestClock clock = new TestClock();
        final ApdexOptions options = ApdexOptions.of(100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        try (final ApdexContext context = apdex.newContext())
        {
            clock.advance(TimeUnit.MILLISECONDS, 50);
        }

        ApdexSnapshot snapshot = apdex.getSnapshot();

        assertNotNull(snapshot);
        assertEquals(1, snapshot.getSize());
        assertEquals(1, snapshot.getSatisfiedSize());
        assertEquals(0, snapshot.getToleratingSize());
        assertEquals(0, snapshot.getFrustratingSize());

        apdex.reset();
        snapshot = apdex.getSnapshot();

        assertEquals(0, snapshot.getSize());
        assertEquals(0, snapshot.getSatisfiedSize());
        assertEquals(0, snapshot.getToleratingSize());
        assertEquals(0, snapshot.getFrustratingSize());
    }

    @Test
    void apedAcquisitionFromRegistry()
    {
        final MonitorRegistry registry = MonitorRegistry.get("apdex.01");
        final Apdex apdex = registry.apdex("metric.apdex", ApdexOptions.of(100, TimeUnit.MILLISECONDS));

        assertNotNull(apdex);
        assertNotNull(apdex.getSnapshot());
    }

    @Test
    void apedAcquisitionFromRegistryWithScore()
    {
        final MonitorRegistry registry = MonitorRegistry.get("apdex.01");
        final Apdex apdex = registry.apdex("metric.apdex", ApdexOptions.of(100, TimeUnit.MILLISECONDS));

        apdex.track(10);
        apdex.track(10);
        apdex.track(10);
        apdex.track(10);
        apdex.track(10);

        final ApdexSnapshot snapshot = apdex.getSnapshot();
        assertNotNull(snapshot);
        assertEquals(new Double(1.0), apdex.getValue());
    }

    @Test
    void apedAcquisitionFromRegistryWithScore02()
    {
        final MonitorRegistry registry = MonitorRegistry.get("apdex.02");
        final Apdex apdex = registry.apdex("metric.apdex", ApdexOptions.of(100, TimeUnit.MILLISECONDS));

        apdex.track(10);
        apdex.track(10);
        apdex.track(200);
        apdex.track(200);

        final ApdexSnapshot snapshot = apdex.getSnapshot();
        assertNotNull(snapshot);
        assertEquals(new Double(.75), apdex.getValue());
    }
}
