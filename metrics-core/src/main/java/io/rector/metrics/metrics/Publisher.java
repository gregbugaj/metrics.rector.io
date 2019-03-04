package io.rector.metrics.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publisher interface
 */
public interface Publisher
{
    /**
     * Publish new message, {@link Message} itself is guaranteed to be non null,
     * but it could contain null fields;
     * 
     * @param message the message to publish
     */
    void publish(final Message message);

    /**
     * Set current publisher
     * @param publisher the publisher to set
     */
    static void set(final Publisher publisher)
    {
        Factory.set(publisher);
    }

    /**
     * Set Composite publishers
     * @param publishers
     */
    static void set(final Publisher... publishers)
    {
        Factory.set(publishers);
    }


    class Factory
    {
        private static Publisher defaultPublisher = new ConsoleLoggerPublisher();

        /**
         * Obtain default publisher
         * 
         * @return current instance of the publisher
         */
        public static Publisher getInstance()
        {
            return defaultPublisher;
        }

        /**
         * Set current publisher
         * @param publisher the publisher to set
         */
        public static void set(final Publisher publisher)
        {
            Objects.requireNonNull(publisher);
            defaultPublisher = publisher;
        }

        public static void set(final Publisher... publishers)
        {
            Objects.requireNonNull(publishers);
            defaultPublisher = new CompositePublisher(publishers);
        }
        
    }

    /**
     * Published message to the  current logger
     */
    class ConsoleLoggerPublisher implements Publisher
    {
        private static final Logger log = LoggerFactory.getLogger(ConsoleLoggerPublisher.class);

        @Override
        public void publish(final Message message)
        {
            log.info("Message : {}", message);
        }
    }

    /**
     * Composite publisher
     */
    class CompositePublisher implements  Publisher
    {
        private static final Logger log = LoggerFactory.getLogger(ConsoleLoggerPublisher.class);

        private List<Publisher> publishers = new ArrayList();

        public CompositePublisher(final Publisher... publishers)
        {
            Objects.requireNonNull(publishers);
            this.publishers = Arrays.asList(publishers);
        }

        @Override
        public void publish(final Message message)
        {
            if(message == null)
                return;

            for(final Publisher publisher : publishers)
            {
                try
                {
                    publisher.publish(message);
                }
                catch (Exception e)
                {
                    log.warn("Unable to publish to message : {}, {} ", publisher, message);
                }
            }
        }
    }
}
