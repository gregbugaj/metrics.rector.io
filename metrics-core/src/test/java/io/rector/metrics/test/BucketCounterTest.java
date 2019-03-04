package io.rector.metrics.test;


import io.rector.metrics.BucketCounter;
import io.rector.metrics.Snapshot;
 import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class BucketCounterTest
{

    @Test
    public void testIncrementalBuckets()
    {
        checkSameBucket(new BucketCounter(0, 100, 200, 300, 400, 500));
    }

    @Test
    public void testCreateSpacedEvenly()
    {
        checkSameBucket(BucketCounter.createSpacedEvenly(0, 500, 6));
    }

    @Test
    public void testCreateSpacedOverInterval()
    {
        checkSameBucket(BucketCounter.createSpacedOverInterval(0, 500, 100));
    }

    @Test
    public void testSnapshot()
    {
        final BucketCounter bc = new BucketCounter(0, 500);

        for (int j = 0; j < 500; ++j)
        {
            bc.add(j);
        }

        final Snapshot sp = bc.getSnapshot(0);

        assertEquals(125.0, sp.getMad(), .000001);
    }


    private void checkSameBucket(final BucketCounter bc)
    {
        assertEquals(0, bc.findBucket(10));
        assertEquals(1, bc.findBucket(100));
        assertEquals(2, bc.findBucket(200));
        assertEquals(3, bc.findBucket(310));
        assertEquals(4, bc.findBucket(400));
        assertEquals(5, bc.findBucket(500));

        // out of bounds
        assertEquals(-1, bc.findBucket(-1000));
        assertEquals(5, bc.findBucket(650));
    }
}
