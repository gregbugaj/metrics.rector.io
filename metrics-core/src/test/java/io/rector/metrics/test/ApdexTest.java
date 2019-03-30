package io.rector.metrics.test;

import io.rector.metrics.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApdexTest
{
    @Test
    void test001()
    {
        try(final ApdexContext apex = Apdex.track("tag"))
        {
            System.out.println("Some action to track");
        }
    }
}
