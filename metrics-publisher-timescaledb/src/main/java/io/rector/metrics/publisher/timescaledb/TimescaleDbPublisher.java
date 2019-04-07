package io.rector.metrics.publisher.timescaledb;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.rector.metrics.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TimescaleDB Publisher
 *
 * Sample usage :
 * <pre>
 *
 * </pre>
 */
public class TimescaleDbPublisher implements Publisher
{
    private static final Logger log = LoggerFactory.getLogger(TimescaleDbPublisher.class);

    private final DataSource datasource;

    private  boolean resetOnReporting;

    private  MonitorRegistry registry;

    private long time;

    private TimeUnit unit;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TimescaleDbPublisher(final MonitorRegistry registry, final DatabaseConfig options, long time, TimeUnit unit, boolean resetOnReporting)
    {
        this.registry = registry;
        this.time = time;
        this.unit = unit;
        this.resetOnReporting = resetOnReporting;

        this.datasource = setup(options);
        verifyConnection();
    }

    private void verifyConnection()
    {
        // noop
    }

    @Override
    public void start()
    {

    }

    private synchronized void publisher()
    {
        final Map<String, Monitor<?>> metrics = registry.getMetrics();
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

    public DataSource setup(final DatabaseConfig options)
    {
        final String driverClassName = options.getDriverClassName();
        final int maximumPoolSize = options.getMaximumPoolSize();
        final String password = options.getPassword();
        final String url = options.getUrl();
        final String username = options.getUsername();

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setDriverClassName(driverClassName);
        config.setMinimumIdle(0);
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(60));
        config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
        config.setLeakDetectionThreshold(60000);

        return new HikariDataSource(config);
    }

    public static class Builder
    {
        private final MonitorRegistry registry;

        private final DatabaseConfig options;

        private long time;

        private TimeUnit unit;

        private boolean resetOnReporting;

        public Builder(final MonitorRegistry registry, final DatabaseConfig options)
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

        public TimescaleDbPublisher build()
        {
            return new TimescaleDbPublisher(registry, options, time, unit, resetOnReporting);
        }
    }

}
