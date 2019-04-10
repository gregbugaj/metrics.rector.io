package io.rector.metrics.publisher.timescaledb;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.rector.metrics.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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
        executor.scheduleAtFixedRate(this::publisher, time, time, unit);
    }

    private void publisher()
    {
        final String appName = registry.getName();
        final Map<String, Monitor<?>> metrics = registry.getMetrics();
        final List<String> batch = new ArrayList<>();

        metrics.forEach((name, metric)->
        {
            try
            {
                final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                final Message msg = asMessage(name, metric);
                final String time = dateFormat.format(new Date(msg.getTime()));
                final String appId = appName;// msg.getAppId();
                final String probe = msg.getName();

                final Message.ValueObject value = msg.getValue();
                final String valueAsString = value.getValueAsString();
                final Number valueAsNumber = value.getValueAsNumber();

                final String source = msg.getSource();
                final MetricType type = msg.getMonitorType();
                final Map<String, Object> attributes = msg.getAttributes();

                final String format ="INSERT INTO tracked_events(time, event, application, UUID,  probe, probe_type, value_str, value_num, source_address, metric_type, data) \n" +
                        "VALUES (NOW(), '%1$s', '%2$s', '%3$s', '%4$s', '%5$s', '%6$s', %7$s, '%8$s', '%9$s', '%10$s')";

                String query = String.format(format,
                                             time,
                                             appId,
                                             UUID.randomUUID(),
                                             probe,
                                             probe,
                                             valueAsString == null ? "" : valueAsString,
                                             valueAsNumber,
                                             source == null ? "" : source,
                                             type.name(),
                                             "{}"
                );

                batch.add(query);

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
                log.error("Unable to create metric : " + name, ex);
            }
        });

        try (final Connection connection = datasource.getConnection();
                final Statement statement = connection.createStatement())
        {
            for(final String query : batch)
            {
                statement.addBatch(query);
            }

            final int[] ids = statement.executeBatch();
            log.info("Total records inserted : {}", ids.length);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            log.warn("Error running update statement", e);
        }
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
        config.setMinimumIdle(5);
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(60));
        config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");
        config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(10)); // 10000

        return new HikariDataSource(config);
    }

    public static  Builder with(final MonitorRegistry registry, final DatabaseConfig options)
    {
        return new Builder(registry, options);
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
