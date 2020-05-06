package io.rector.metrics.publisher.csv;

import io.rector.metrics.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Publish statistics to a csv file
 */
public class CsvPublisher extends Publisher
{
    private static final Logger log = LoggerFactory.getLogger(CsvPublisher.class);

    private static final String DEFAULT_CSV_FILE = "./metrics.csv";

    private CSVPrinter printer;

    public CsvPublisher(final MonitorRegistry registry,
                        final Appendable appendable,
                        long time,
                        TimeUnit unit,
                        boolean resetOnReporting)
    {
        super(registry, time, unit, resetOnReporting);
        Objects.requireNonNull(appendable);
        try
        {
            printer = new CSVPrinter(appendable, CSVFormat.RFC4180);
        }
        catch (final IOException e)
        {
            throw new RuntimeException("Unable to initialize printer", e);
        }
    }

    @Override
    protected void publish()
    {
        try
        {
            final String logDate = Long.toString(System.currentTimeMillis());
            forEach((name, metric) -> apply(logDate, name, metric));
            printer.flush();
        }
        catch (final IOException e)
        {
            log.error("Unable tow publish message", e);
        }
    }

    /**
     * Message format
     * <pre>
     *     "logdate", "eventtime", "application", "probe", "probetype", "value", "source", "metrictype"
     * </pre>
     *
     * @param logDate
     * @param name
     * @param metric
     */
    private void apply(final String logDate, final String name, final Monitor<?> metric)
    {
        try
        {
            final Message msg = asMessage(name, metric);
            final String time = "" + msg.getTime();
            final String appId = msg.getAppId();
            final Message.ValueObject value = msg.getValue();
            final String source = msg.getSource();
            final MetricType type = msg.getMonitorType();

            printer.printRecord(logDate,
                                time,
                                appId,
                                name,
                                type,
                                value.getType(),
                                value.getValueAsString(),
                                source,
                                type);
        }
        catch (final IOException ex)
        {
            log.error("Unable to create metric : " + name, ex);
        }
    }

    public static Builder of(final MonitorRegistry registry, Path path)
    {
        return new Builder(registry, path);
    }

    public static Builder of(final MonitorRegistry registry, final Appendable appendable)
    {
        Objects.requireNonNull(registry);
        return new Builder(registry, appendable);
    }

    public static class Builder extends CorePublisherBuilder<Builder>
    {
        private final MonitorRegistry registry;

        private Path path;

        private Appendable appendable;

        private boolean append = true;

        public Builder(final MonitorRegistry registry, final Path path)
        {
            this.registry = Objects.requireNonNull(registry);
            this.path = Objects.requireNonNull(path);
        }

        public Builder(final MonitorRegistry registry, final Appendable appendable)
        {
            this.registry = Objects.requireNonNull(registry);
            this.appendable = Objects.requireNonNull(appendable);
        }

        public Builder withAppend(boolean append)
        {
            this.append = append;
            return this;
        }

        public CsvPublisher build()
        {
            try
            {
                if (appendable == null)
                {
                    if (path == null)
                    {
                        path = Paths.get(DEFAULT_CSV_FILE);
                    }
                    appendable = new PrintWriter(new FileWriter(path.toFile(), append));
                }
                return new CsvPublisher(registry, appendable, time, unit, resetOnReporting);
            }
            catch (final Exception e)
            {
                throw new RuntimeException("Unable to initialize publisher", e);
            }
        }
    }
}
