package io.rector.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Published message to the  current logger, messages will not be buffered
 */
public class ConsolePublisher implements Publisher
{
    private static final Logger log = LoggerFactory.getLogger(ConsolePublisher.class);

    @Override
    public void start()
    {
        // noop
    }
}
