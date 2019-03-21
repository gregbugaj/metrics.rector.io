package io.rector.metrics.publisher.csv;

import io.rector.metrics.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CSV metrics publisher
 */
public class CsvPublisher extends BasePublisher
{
    private static final Logger log = LoggerFactory.getLogger(CsvPublisher.class);

    private static final String DEFAULT_CSV_FILE = "./metrics.csv";

    private final Path path;

    private  boolean resetOnReporting;

    private  MonitorRegistry registry;

    private long time;

    private TimeUnit unit;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private CsvPublisher(final Path path)
    {
        this.path = Objects.requireNonNull(path);
    }

    private CsvPublisher()
    {
        this(Paths.get(DEFAULT_CSV_FILE));
    }

    public CsvPublisher(final MonitorRegistry registry, Path path, long time, TimeUnit unit, boolean resetOnReporting)
    {
        this.registry = registry;
        this.path = path;
        this.time = time;
        this.unit = unit;
        this.resetOnReporting = resetOnReporting;
    }

    private synchronized void publisher()
    {
        final Map<String, Monitor<?>> metrics = registry.getMetrics();
        try (
                final FileWriter writer = new FileWriter(path.toFile(), true);
                final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)
        )
        {
            // message format
            // "logdate", "eventtime", "application", "probe", "probetype", "value", "source", "metrictype"
            final String logDate = "" + System.currentTimeMillis();
            metrics.forEach((name, metric)->{
                try
                {
                    final Message msg = asMessage(name, metric);
                    final String time = "" + msg.getTime();
                    final String appId = msg.getAppId();
                    final Message.ValueObject value = msg.getValue();
                    final String source = msg.getSource();
                    final MetricType type = msg.getMonitorType();

                    csvPrinter.printRecord(logDate, time, appId, name, type, value.getType(), value.getValueAsString(), source, type);

                    if(resetOnReporting)
                    {
                        if(metric instanceof Resettable)
                        {
                            ((Resettable)metric).reset();
                        }
                    }
                }
                catch (IOException ex)
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
        return new Builder(registry, path);
    }

    public void start()
    {
        executor.scheduleAtFixedRate(this::publisher,time, time, unit);
    }

    public static class Builder
    {
        private final MonitorRegistry registry;

        private final Path path;

        private long time;

        private TimeUnit unit;

        private boolean resetOnReporting;

        public Builder(final MonitorRegistry registry, final Path path)
        {
            this.registry = Objects.requireNonNull(registry);
            this.path = Objects.requireNonNull(path);
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
            return new CsvPublisher(registry, path, time, unit, resetOnReporting);
        }
    }
}
