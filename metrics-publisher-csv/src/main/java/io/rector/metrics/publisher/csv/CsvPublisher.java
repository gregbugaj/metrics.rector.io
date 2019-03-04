package io.rector.metrics.publisher.csv;

import io.rector.metrics.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.omg.CORBA.TIMEOUT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CSV metrics publisher
 */
public class CsvPublisher implements Publisher
{
    private static final Logger log = LoggerFactory.getLogger(CsvPublisher.class);

    private static final String DEFAULT_CSV_FILE = "./metrics.csv";

    private final Path path;

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

    public CsvPublisher(final MonitorRegistry registry, final Path path, final long time, final TimeUnit unit)
    {
        this.registry = registry;
        this.path = path;
        this.time = time;
        this.unit = unit;
    }

    private void publisher()
    {
        final List<Message> messages = registry.getMessages();
        System.out.println("Publishing messages : " + messages.size());
        try (
                final BufferedWriter writer = Files.newBufferedWriter(path);
                final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader(
                                "logdate",
                                "eventtime",
                                "application",
                                "probe",
                                "probetype",
                                "value",
                                "source",
                                "metrictype"
                        ))
        )
        {
            final String logdate = "" + System.currentTimeMillis();

            for(final Message msg : messages)
            {
                final String time = "" + msg.getTime();
                final String appId = msg.getAppId();
                final String name = msg.getName();
                final Message.ValueObject value = msg.getValue();
                final String source = msg.getSource();
                final MetricType type = msg.getMonitorType();

                csvPrinter.printRecord(logdate, time, appId, name, type,value.getType(), value.getValueAsString(), source, type);
            }

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
        System.out.println("Starting CSV publisher");
        executor.schedule(this::publisher,time, unit);
    }

    public static class Builder
    {
        private final MonitorRegistry registry;

        private final Path path;

        private long time;

        private TimeUnit unit;

        public Builder(final MonitorRegistry registry, final Path path)
        {
            this.registry = Objects.requireNonNull(registry);
            this.path = Objects.requireNonNull(path);
        }

        public Builder interval(long time, TimeUnit unit)
        {
            this.time = time;
            this.unit = unit;
            return this;
        }

        public CsvPublisher build()
        {
            return new CsvPublisher(registry, path, time, unit);
        }
    }
}
