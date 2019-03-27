package io.rector.metrics.test;

import io.rector.metrics.Reservoir;
import io.rector.metrics.SlidingWindowReservoir;
import io.rector.metrics.Snapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlidingWindowReservoirTest
{
    @Test
    void windowSizeTest()
    {
        final Reservoir reservoir = new SlidingWindowReservoir(2);
        reservoir.update(1);
        reservoir.update(2);
        reservoir.update(3);

        final Snapshot snapshot = reservoir.getSnapshot();

        assertEquals(2, snapshot.size());
        assertArrayEquals(new long[]{2,3}, snapshot.getValues());
    }
}
