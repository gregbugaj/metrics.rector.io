package io.rector.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Composite publisher that allows us to chain multiple publishers in a pipeline
 */
public class CompositePublisher extends Publisher
{
    private static final Logger log = LoggerFactory.getLogger(CompositePublisher.class);

    private List<Publisher> publishers;

    public CompositePublisher(final Publisher... publishers)
    {
        super(); // BORKED
        Objects.requireNonNull(publishers);
        this.publishers = Arrays.asList(publishers);
    }

    @Override
    public void start()
    {
        // NOOP
        // We don't want the start method of the P
    }

    @Override
    protected void publish()
    {

    }
}
