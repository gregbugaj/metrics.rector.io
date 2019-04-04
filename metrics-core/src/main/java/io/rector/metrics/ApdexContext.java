package io.rector.metrics;

public class ApdexContext implements AutoCloseable
{
    private final ApdexProvider provider;

    private final Clock clock;

    private  long start;

    public ApdexContext(final Clock clock, final ApdexProvider provider)
    {
        this.provider = provider;
        this.clock = clock;
        this.start = clock.getTick();
    }

    @Override
    public void close()
    {
        long duration = clock.getTick() - start;
        provider.update(duration);
    }
}
