package io.rector.metrics.test;

import io.rector.metrics.SlidingWindowReservoir;
import org.junit.jupiter.api.Test;

public class SlidingWindowReservoirTest
{
    @Test
    void catBeReset()
    {
        SlidingWindowReservoir reservoir = new SlidingWindowReservoir(2);
        reservoir.update(1);
        reservoir.update(2);
        reservoir.update(3);

        System.out.println(reservoir.getSnapshot());

    }
}
