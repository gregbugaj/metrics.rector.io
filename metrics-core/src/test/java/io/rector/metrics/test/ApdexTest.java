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
    void test001()
    {
        TestClock clock = new TestClock();

        final ApdexOptions options = ApdexOptions.of("apdex.test1", 100, TimeUnit.MILLISECONDS);
        final Apdex apdex = new Apdex(5, options, clock);

        try(final ApdexContext context = apdex.newContext())
        {
            clock.advance(TimeUnit.MILLISECONDS, 100);
        }

        final ApdexSnapshot snapshot = apdex.getSnapshot();
        System.out.println(snapshot);

        assertEquals(1, snapshot.getSize());
        assertEquals(1, snapshot.getSatisfiedSize());
        assertEquals(0, snapshot.getToleratingSize());
        assertEquals(0, snapshot.getFrustratingSize());
    }


    @Test
    void monitorTest()
    {
        final MonitorRegistry registry = MonitorRegistry.get("apdex.01");
    }
}
