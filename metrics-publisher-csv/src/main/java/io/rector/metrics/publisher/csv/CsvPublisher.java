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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Publish statistics to a csv file
 */
public class CsvPublisher extends BasePublisher
{
    private static final Logger log = LoggerFactory.getLogger(CsvPublisher.class);

    private static final String DEFAULT_CSV_FILE = "./metrics.csv";

    private final Appendable appendable;

    private boolean resetOnReporting;

    private MonitorRegistry registry;

    private long time;

    private TimeUnit unit;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private CsvPublisher(final Path path) throws IOException
    {
        Objects.requireNonNull(path);
        appendable = new PrintWriter(new FileWriter(path.toFile(), true));
    }

    private CsvPublisher() throws IOException
    {
        this(Paths.get(DEFAULT_CSV_FILE));
    }

    public CsvPublisher(final MonitorRegistry registry,
                        Appendable appendable,
                        long time,
                        TimeUnit unit,
                        boolean resetOnReporting)
    {
        this.registry = Objects.requireNonNull(registry);
        this.appendable = Objects.requireNonNull(appendable);
        this.time = time;
        this.unit = unit;
        this.resetOnReporting = resetOnReporting;
    }

    private synchronized void publisher()
    {
        final Map<String, Monitor<?>> metrics = registry.getMetrics();
        try (final CSVPrinter csvPrinter = new CSVPrinter(appendable, CSVFormat.RFC4180))
        {
            System.out.println("Publishing");
            // message format
            // "logdate", "eventtime", "application", "probe", "probetype", "value", "source", "metrictype"
            final String logDate = "" + System.currentTimeMillis();
            metrics.forEach((name, metric) ->
                            {
                                System.out.println(" -- " + name);

                                try
                                {
                                    final Message msg = Publisher.asMessage(name, metric);
                                    final String time = "" + msg.getTime();
                                    final String appId = msg.getAppId();
                                    final Message.ValueObject value = msg.getValue();
                                    final String source = msg.getSource();
                                    final MetricType type = msg.getMonitorType();

                                    csvPrinter.printRecord(logDate,
                                                           time,
                                                           appId,
                                                           name,
                                                           type,
                                                           value.getType(),
                                                           value.getValueAsString(),
                                                           source,
                                                           type);

                                    if (resetOnReporting)
                                    {
                                        if (metric instanceof Resettable)
                                        {
                                            ((Resettable) metric).reset();
                                        }
                                    }
                                }
                                catch (final IOException ex)
                                {
                                    log.error("Unable to create metric : " + name, ex);
                                }
                            });

            csvPrinter.flush();
        }
        catch (final IOException e)
        {
            log.error("Unable tow publish message", e);
        }
    }

    public static Builder of(final MonitorRegistry registry, Path path)
    {
        Objects.requireNonNull(registry);
        Objects.requireNonNull(path);
        return new Builder(registry, path);
    }

    public static Builder of(final MonitorRegistry registry, final Appendable appendable)
    {
        Objects.requireNonNull(registry);
        return new Builder(registry, appendable);
    }

    public void start()
    {
        executor.scheduleAtFixedRate(this::publisher, time, time, unit);
    }

    public static class Builder
    {
        private final MonitorRegistry registry;

        private Path path;

        private Appendable appendable;

        private long time;

        private TimeUnit unit;

        private boolean resetOnReporting;

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

        public Builder withInterval(long time, TimeUnit unit)
        {
            this.time = time;
            this.unit = unit;
            return this;
        }

        public Builder withResetOnReporting(boolean reset)
        {
            this.resetOnReporting = reset;
            return this;
        }

        public CsvPublisher build()
        {
            try
            {
                if (path != null)
                {
                    appendable = new PrintWriter(new FileWriter(path.toFile(), true));
                }
                if (appendable == null)
                {
                    throw new IllegalStateException("appendable should not be null");
                }

                return new CsvPublisher(registry, appendable, time, unit, resetOnReporting);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Unable to initialize publisher", e);
            }
        }
    }
}
