package io.rector.metrics.publisher.opentsdb;

import io.rector.metrics.*;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * InfluxDB Publisher
 *
 * Sample usage :
 * <pre>
 *
 * </pre>
 */
public class InfluxDbPublisher implements Publisher
{
    private static final Logger log = LoggerFactory.getLogger(InfluxDbPublisher.class);

    private InfluxDB influxDB;

    private  boolean resetOnReporting;

    private  MonitorRegistry registry;

    private long time;

    private TimeUnit unit;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public InfluxDbPublisher(final MonitorRegistry registry, final InfluxDbOptions options, long time, TimeUnit unit, boolean resetOnReporting)
    {
        this.registry = registry;
        this.time = time;
        this.unit = unit;
        this.resetOnReporting = resetOnReporting;
        this.influxDB = create(options);

        verifyConnection();
    }

    private InfluxDB create(InfluxDbOptions options)
    {
        final String databaseURL = options.getDatabaseURL();
        final String userName = options.getUserName();
        final String password = options.getPassword();

        final InfluxDB influxDB = InfluxDBFactory.connect(databaseURL, userName, password);

        return influxDB;
    }

    private void verifyConnection()
    {
        Pong response = this.influxDB.ping();
        if (response.getVersion().equalsIgnoreCase("unknown"))
        {
            log.error("Error pinging server.");
            return;
        }
    }

    @Override
    public void start()
    {

    }

    private synchronized void publisher()
    {
        final Map<String, Monitor<?>> metrics = registry.getMetrics();
        // "logdate", "eventtime", "application", "probe", "probetype", "value", "source", "metrictype"
        final String logDate = "" + System.currentTimeMillis();
        metrics.forEach((name, metric)->
        {
            try
            {
                final Message msg = asMessage(name, metric);
                final String time = "" + msg.getTime();
                final String appId = msg.getAppId();
                final Message.ValueObject value = msg.getValue();
                final String source = msg.getSource();
                final MetricType type = msg.getMonitorType();

                if(resetOnReporting)
                {
                    if(metric instanceof Resettable)
                    {
                        ((Resettable)metric).reset();
                    }
                }
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
    public CompletableFuture<Object> addPoint(final String metric, final long timestamp, final long value, final Map<String, String> tags)
    {
        return null;
    }

    /**
     * Get all metrics associated with this registry as {@link Message}
     * @return collection of {@link Message}
     * @param name
     * @param metric
     */
    public Message asMessage(final String name, final Monitor<?> metric)
    {
        final Message msg = new Message();

        msg.setName(name);
        msg.setTime(System.currentTimeMillis());
        msg.setMonitorType(metric.getMonitorType());
        msg.setValue(new Message.ValueObject(metric.getType(), metric.getValue()));

        return msg;
    }

    public static class Builder
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

        private String userName ;

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

        public String getDatabaseURL() {
            return databaseURL;
        }
    }
}
