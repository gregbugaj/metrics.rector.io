package io.rector.metrics.test;

import io.rector.metrics.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApdexTest
{

    @Test
    void testContextUsage()
    {
        final TestClock clock = new TestClock();
        final ApdexOptions options = ApdexOptions.of("apdex.test1", 100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        try(final ApdexContext context = apdex.newContext())
        {
            clock.advance(TimeUnit.MILLISECONDS, 20);
        }

        try(final ApdexContext context = apdex.newContext())
        {
            clock.advance(TimeUnit.MILLISECONDS, 20);
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
        final ApdexOptions options = ApdexOptions.of("apdex.test1", 100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        apdex.track(()->
        {
            clock.advance(TimeUnit.MILLISECONDS, 20);
            return 1;
        });

        final ApdexSnapshot snapshot = apdex.getSnapshot();

        assertEquals(1, snapshot.getSize());
        assertEquals(1, snapshot.getSatisfiedSize());
        assertEquals(0, snapshot.getToleratingSize());
        assertEquals(0, snapshot.getFrustratingSize());
    }


    @Test
    void testDirectUsage()
    {
        final TestClock clock = new TestClock();
        final ApdexOptions options = ApdexOptions.of("apdex.test1", 100, TimeUnit.MILLISECONDS);
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
        final ApdexOptions options = ApdexOptions.of("apdex.test1", 100, TimeUnit.MILLISECONDS);
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
        final ApdexOptions options = ApdexOptions.of("apdex.test1", 100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        apdex.track(4 * 100 + 10);

        final ApdexSnapshot snapshot = apdex.getSnapshot();
        assertEquals(1, snapshot.getSize());
        assertEquals(0, snapshot.getSatisfiedSize());
        assertEquals(0, snapshot.getToleratingSize());
        assertEquals(1, snapshot.getFrustratingSize());
    }


    @Test
    void testReset()
    {
        final TestClock clock = new TestClock();
        final ApdexOptions options = ApdexOptions.of("apdex.test1", 100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        try(final ApdexContext context = apdex.newContext())
        {
            clock.advance(TimeUnit.MILLISECONDS, 50);
        }

        ApdexSnapshot snapshot = apdex.getSnapshot();
        System.out.println(snapshot);

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
    void monitorTest()
    {
        final MonitorRegistry registry = MonitorRegistry.get("apdex.01");
    }
}
