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
import java.util.concurrent.TimeUnit;

/**
 * TimescaleDB Publisher
 * Sample usage :
 * <pre>
 * </pre>
 */
public class TimescaleDbPublisher extends Publisher
{
    private static final Logger log = LoggerFactory.getLogger(TimescaleDbPublisher.class);

    private final DataSource datasource;

    public TimescaleDbPublisher(final MonitorRegistry registry,
                                final DataSource datasource,
                                long time,
                                TimeUnit unit,
                                boolean resetOnReporting)
    {
        super(registry, time, unit, resetOnReporting);
        this.datasource = datasource;
        verifyConnection();
    }

    private void verifyConnection()
    {
        // noop
    }

    @Override
    protected void publish()
    {
        final String appName = registry.getName();
        final List<String> batch = new ArrayList<>();

        forEach((name, metric) -> apply(appName, name, metric, batch));

        try (final Connection connection = datasource.getConnection();
                final Statement statement = connection.createStatement())
        {
            for (final String query : batch)
            {
                statement.addBatch(query);
            }
            final int[] ids = statement.executeBatch();
            log.info("Total records inserted : {}", ids.length);
        }
        catch (final SQLException e)
        {
            log.warn("Error running update statement", e);
        }
    }

    private void apply(final String appName,
                       final String name,
                       final Monitor<?> metric,
                       final List<String> batch)
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

            final String format =
                    "INSERT INTO tracked_events(time, event, application, UUID,  probe, probe_type, value_str, value_num, source_address, metric_type, data) \n"
                            +
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
        }
        catch (final Exception ex)
        {
            log.error("Unable to create metric : " + name, ex);
        }
    }

    public static Builder with(final MonitorRegistry registry, final DatabaseConfig options)
    {
        return new Builder(registry, options);
    }

    public static class Builder
    {
        private final MonitorRegistry registry;

        private DatabaseConfig options;

        private DataSource datasource;

        private long time;

        private TimeUnit unit;

        private boolean resetOnReporting;

        public Builder(final MonitorRegistry registry, final DatabaseConfig options)
        {
            this.registry = Objects.requireNonNull(registry);
            this.options = Objects.requireNonNull(options);
        }

        public Builder(final MonitorRegistry registry, final DataSource datasource)
        {
            this.registry = Objects.requireNonNull(registry);
            this.datasource = Objects.requireNonNull(datasource);
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
            if (options != null)
            {
                datasource = setup(options);
            }

            if (datasource == null)
                throw new IllegalStateException("Datasource can't be null");

            return new TimescaleDbPublisher(registry, datasource, time, unit, resetOnReporting);
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
    }
}
