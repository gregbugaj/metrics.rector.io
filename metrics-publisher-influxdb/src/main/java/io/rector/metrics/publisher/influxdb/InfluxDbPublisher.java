package io.rector.metrics.publisher.influxdb;

import io.rector.metrics.*;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * InfluxDB Publisher
 * Sample usage :
 * <pre>
 * </pre>
 */
public class InfluxDbPublisher extends Publisher
{
    private static final Logger log = LoggerFactory.getLogger(InfluxDbPublisher.class);

    private InfluxDB influxDB;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public InfluxDbPublisher(final MonitorRegistry registry,
                             final InfluxDbOptions options,
                             long time,
                             TimeUnit unit,
                             boolean resetOnReporting)
    {
        super(registry, time, unit, resetOnReporting);
        this.influxDB = create(options);
        verifyConnection();
    }

    private InfluxDB create(InfluxDbOptions options)
    {
        final String databaseURL = options.getDatabaseURL();
        final String userName = options.getUserName();
        final String password = options.getPassword();

        return InfluxDBFactory.connect(databaseURL, userName, password);
    }

    private void verifyConnection()
    {
        final Pong response = this.influxDB.ping();
        if (response.getVersion().equalsIgnoreCase("unknown"))
        {
            log.error("Error pinging server.");
            return;
        }
    }

    @Override
    protected void publish()
    {
        final Map<String, Monitor<?>> metrics = registry.getMetrics();
        // "logdate", "eventtime", "application", "probe", "probetype", "value", "source", "metrictype"
        final String logDate = "" + System.currentTimeMillis();
        forEach((name, metric) ->
                {
                    try
                    {
                        final Message msg = asMessage(name, metric);
                        final String time = "" + msg.getTime();
                        final String appId = msg.getAppId();
                        final Message.ValueObject value = msg.getValue();
                        final String source = msg.getSource();
                        final MetricType type = msg.getMonitorType();

                        //                        addPoint
                    }
                    catch (final Exception ex)
                    {
                        log.error("Unable to store metric : " + name, ex);
                    }
                });
    }

    /**
     * Add new metric
     *
     * @param metric
     * @param timestamp
     * @param value
     * @param tags
     * @return
     */
    public CompletableFuture<Object> addPoint(final String metric,
                                              final long timestamp,
                                              final long value,
                                              final Map<String, String> tags)
    {
        return null;
    }

    public static class Builder extends CorePublisherBuilder<Builder>
    {
        private final MonitorRegistry registry;

        private final InfluxDbOptions options;

        private long time;

        private TimeUnit unit;

        private boolean resetOnReporting;

        public Builder(final MonitorRegistry registry, final InfluxDbOptions options)
        {
            this.registry = Objects.requireNonNull(registry);
            this.options = Objects.requireNonNull(options);
        }

        public InfluxDbPublisher build()
        {
            return new InfluxDbPublisher(registry, options, time, unit, resetOnReporting);
        }
    }

    public static class InfluxDbOptions
    {
        private String retentionPolicy = "defaultPolicy";

        private String database;

        private String databaseURL;

        private String userName;

        private String password;

        public String getRetentionPolicy()
        {
            return retentionPolicy;
        }

        public void setRetentionPolicy(String retentionPolicy)
        {
            this.retentionPolicy = retentionPolicy;
        }

        public String getDatabase()
        {
            return database;
        }

        public void setDatabase(String database)
        {
            this.database = database;
        }

        public void setDatabaseURL(String databaseURL)
        {
            this.databaseURL = databaseURL;
        }

        public String getUserName()
        {
            return userName;
        }

        public void setUserName(String userName)
        {
            this.userName = userName;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public String getDatabaseURL()
        {
            return databaseURL;
        }
    }
}
