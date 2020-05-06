package io.rector.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Published message to the  current logger, messages will not be buffered
 */
public class ConsolePublisher extends Publisher
{
    private static final Logger log = LoggerFactory.getLogger(ConsolePublisher.class);

    public ConsolePublisher(final MonitorRegistry registry,
                            final long time,
                            final TimeUnit unit,
                            boolean resetOnReporting)
    {
        super(registry, time, unit, resetOnReporting);
    }

    public static Builder of(final MonitorRegistry registry)
    {
        return new Builder(registry);
    }

    @Override
    protected void publish()
    {
        final String logDate = Long.toString(System.currentTimeMillis());

        System.out.println(String.format("-------------------------------"));
        forEach((name, metric) -> apply(logDate, name, metric));
    }

    private void apply(final String logDate, final String name, final Monitor<?> metric)
    {
        final Message msg = asMessage(name, metric);
        final String time = "" + msg.getTime();
        final String appId = msg.getAppId();
        final Message.ValueObject value = msg.getValue();
        final String source = msg.getSource();
        final MetricType type = msg.getMonitorType();

        // "logdate", "eventtime", "application", "probe", "probetype", "value", "source", "metrictype"
        final String line = String.format("%-15s %-15s %-10s %-25s %-10s %-10s %-15s %-10s %-10s", logDate,
                                            time,
                                            appId,
                                            name,
                                            type,
                                            value.getType(),
                                            value.getValueAsString(),
                                            source,
                                            type);

        System.out.println(line);
    }

    public static class Builder extends CorePublisherBuilder<Builder>
    {
        private final MonitorRegistry registry;

        public Builder(final MonitorRegistry registry)
        {
            this.registry = Objects.requireNonNull(registry);
        }

        public ConsolePublisher build()
        {
            try
            {
                return new ConsolePublisher(registry, time, unit, resetOnReporting);
            }
            catch (final Exception e)
            {
                throw new RuntimeException("Unable to initialize publisher", e);
            }
        }
    }
}
