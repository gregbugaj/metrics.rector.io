package io.rector.metrics.publisher.csv;

import io.rector.metrics.metrics.Message;
import io.rector.metrics.metrics.MetricType;
import io.rector.metrics.metrics.Publisher;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

    private List<Message> messages = new ArrayList<>();

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public CsvPublisher(final Path path)
    {
        this.path = Objects.requireNonNull(path);
        executor.schedule(this::publisher,15, TimeUnit.SECONDS);
    }

    public CsvPublisher()
    {
        this(Paths.get(DEFAULT_CSV_FILE));
    }

    private void publisher()
    {
        final List<Message> clone = new ArrayList<>(messages);
        messages = new ArrayList<>();

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

    @Override
    public void publish(final Message message)
    {
        messages.add(message);
    }
}
