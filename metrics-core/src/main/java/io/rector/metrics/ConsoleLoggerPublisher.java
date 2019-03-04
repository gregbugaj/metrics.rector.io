package io.rector.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Published message to the  current logger, messages will not be buffered
 */
public class ConsoleLoggerPublisher implements Publisher
{
    private static final Logger log = LoggerFactory.getLogger(ConsoleLoggerPublisher.class);

    @Override
    public void start()
    {
        // noop
    }
}
