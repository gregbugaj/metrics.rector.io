package io.rector.metrics.test;

import io.rector.metrics.Apdex;
import io.rector.metrics.Reservoir;
import io.rector.metrics.SlidingWindowReservoir;
import io.rector.metrics.Snapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApdexTest
{
    @Test
    void test001()
    {
        try(final Apdex ax = Apdex.track("tag"))
        {
            System.out.println("Some action to track");
        }
    }
}
