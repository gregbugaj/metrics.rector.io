package io.rector.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.rector.metrics.MonitorRegistry.Factory.getInstance;

public interface MonitorRegistry
{
    @FunctionalInterface
    interface MetricSupplier<T extends Monitor>
    {
        T newMetric();
    }

    /**
     * Get underlying metrics
     *
     * @return
     */
    Map<String, Monitor<?>> getMetrics();

    /**
     * Name that this registry is associated with
     *
     * @return
     */
    String getName();

    /**
     * Register new Monitor
     * @param name of the monitor
     * @param monitor to register
     * @throws IllegalArgumentException when monitor is already registered with given name
     */
    <T extends Monitor> T register(final String name, final T monitor) throws IllegalArgumentException;

    /**
     * Register new {@link MetricSet}
     * @param metrics to register
     * @throws IllegalArgumentException
     */
    void registerAll(final MetricSet metrics) throws IllegalArgumentException;

    void registerAll(final String prefix, final Monitor<?> m1, final Monitor<?> m2)throws IllegalArgumentException;

    void registerAll(final String prefix, final Monitor<?> m1, final Monitor<?> m2, final Monitor<?> m3)throws IllegalArgumentException;

    void registerAll(final String prefix, final Monitor<?> m1, final Monitor<?> m2, final Monitor<?> m3, final Monitor<?>... m4) throws IllegalArgumentException;

    /**
     * Create or retrieve gauge, {@link Gauge} will be created on first access
     * @param name of the gage to get
     * @return new instance of Gauge or existing one if the gage already exist with given name
     */
    Gauge gauge(final String name);

    Gauge gauge(final String name, final MetricSupplier<Gauge> supplier);

    /**
     * Create or retrieve apdex monitor, {@link Apdex} will be create on first access
     *
     * @param name the name of the apdex monitor
     * @return new instance of Apdex or existing one if the apdex already exist with given name
     */
    Apdex apdex(final String name);

    /**
     * Create or retrieve apdex monitor, {@link Apdex} will be create on first access
     *
     * @param name the name of the apdex monitor
     * @param options the apdex options
     * @return new instance of Apdex or existing one if the apdex already exist with given name
     */
    Apdex apdex(final String name, final ApdexOptions options);


    /**
     * Create or retrieve counter, {@link Counter} will be created on first access
     *
     * @param name of the counter to create and register
     * @return new instance of Counter or existing one if the gage already exist with given name
     */
    Counter counter(final String name);

    Counter counter(final String name, final MetricSupplier<Counter> supplier);

    /**
     * Remove metric with given name
     *
     * @param name of the metric to remove
     * @return true when the metric have been removed successfully, false otherwise
     */
    boolean remove(final String name);

    static MonitorRegistry get()
    {
        return get("DEFAULT");
    }

    /**
     * Get {@link MonitorRegistry} associated with this name
     * @param name the name of the registry to get
     * @return
     */
    static MonitorRegistry get(String name)
    {
        return getInstance(name);
    }

    class Factory
    {
        private static Map<String, MonitorRegistry> registry = new ConcurrentHashMap<>();

        static MonitorRegistry getInstance(final String name)
        {
            Objects.requireNonNull(name);
            final MonitorRegistry reg = registry.get(name);

            if (reg != null)
                return reg;

            // should hit only once
            final MonitorRegistry value = registry.putIfAbsent(name, new MonitorRegistryDefault(name));

            // race condition between Thred1 and Thread2, so we simply retrieve results from the cache
            if (value == null)
                return registry.get(name);

            return value;
        }
    }

    class MonitorRegistryDefault implements MonitorRegistry
    {
        private static final Logger log = LoggerFactory.getLogger(MonitorRegistryDefault.class);

        private final String name;

        private ConcurrentHashMap<String, Monitor<?>> metrics;

        private MonitorRegistryDefault(String name)
        {
            this.name = name;
            metrics = new ConcurrentHashMap<>();
        }

        public Map<String, Monitor<?>> getMetrics()
        {
            return Collections.unmodifiableMap(metrics);
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public <T extends Monitor> T register(final String name, final T monitor) throws IllegalArgumentException
        {
            Objects.requireNonNull(name);
            Objects.requireNonNull(monitor);

            if(monitor.getMonitorType() == MetricType.COMPOSITE)
            {
                registerAll(name, (MetricSet)monitor);
            }
            else
            {
                Monitor<?> existing = metrics.putIfAbsent(name, monitor);

                if(existing != null)
                {
                    throw new IllegalStateException("Metric named '" +name+"' already exists");
                }
            }

            return monitor;
        }

        private <T extends Monitor> T registerNoThrow(final String name, final T monitor)
        {
            try
            {
                return register(name, monitor);
            }
            catch(IllegalArgumentException ex)
            {
                log.error("Unable to register metric: " + name, ex);
            }

            return null;
        }

        @Override
        public  void registerAll(final String prefix, final Monitor<?> m1, final Monitor<?> m2)
        {
            registerNoThrow(name(prefix, name(m1.getClass())), m1);
            registerNoThrow(name(prefix, name(m2.getClass())), m2);
        }

        @Override
        public  void registerAll(final String prefix, final Monitor<?> m1, final Monitor<?> m2, final Monitor<?> m3)
        {
            registerNoThrow(name(prefix, name(m1.getClass())), m1);
            registerNoThrow(name(prefix, name(m2.getClass())), m2);
            registerNoThrow(name(prefix, name(m3.getClass())), m3);
        }

        @Override
        public void registerAll(final String prefix,final  Monitor<?> m1,final  Monitor<?> m2,final  Monitor<?> m3,final  Monitor<?>... m4)
        {
            MetricSet metrics = new MetricSet()
            {
                @Override
                public Map<String, Monitor<?>> getValue()
                {
                    List<Monitor<?>> monitor = new ArrayList<>();
                    Map<String, Monitor<?>> monitors = new HashMap<>();

                    monitor.addAll(Arrays.asList(m1, m2, m3));
                    monitor.addAll(Arrays.asList(m4));

                    monitor.forEach(m->monitors.put(name(m.getClass()), m));

                    return monitors;
                }
            } ;

            registerAll(prefix, metrics);
        }

        @Override
        public Gauge gauge(String name)
        {
          return getOrAdd(name, MetricBuilder.GAUGES);
        }

        @Override
        public Gauge gauge(String name, MetricSupplier<Gauge> supplier)
        {
         return getOrAdd(name, new MetricBuilder<Gauge>() {

             @Override
             public Gauge newMetric()
             {
                 return supplier.newMetric();
             }

             @Override
             public boolean isInstance(Monitor monitor)
             {
                 return Gauge.class.isInstance(monitor);
             }
         });
        }

        @Override
        public Apdex apdex(final String name)
        {
            final ApdexOptions options  = ApdexOptions.of(1, TimeUnit.SECONDS);
            return apdex(name, options);
        }

        @Override
        public Apdex apdex(final String name, final ApdexOptions options)
        {
            return getOrAdd(name, new MetricBuilder<Apdex>()
            {
                @Override
                public Apdex newMetric()
                {
                    return new Apdex(10, options);
                }

                @Override
                public boolean isInstance(final Monitor<?> monitor)
                {
                    return Apdex.class.isInstance(monitor);
                }
            });
        }

        @Override
        public Counter counter(final String name)
        {
            return getOrAdd(name, MetricBuilder.COUTNERS);
        }

        @Override
        public Counter counter(String name, MetricSupplier<Counter> supplier)
        {
            return getOrAdd(name, new MetricBuilder<Counter>(){

                @Override
                public Counter newMetric()
                {
                    return supplier.newMetric();
                }

                @Override
                public boolean isInstance(final Monitor<?> monitor)
                {
                    return Counter.class.isInstance(monitor);
                }
            });
        }

        @SuppressWarnings("unchecked")
        private <T extends Monitor> T getOrAdd(final String name, final MetricBuilder<T> builder)
        {
            final Monitor<?> metric = metrics.get(name);

            if(builder.isInstance(metric))
            {
                return (T) metric;
            }
            else if (metric == null)
            {
                try
                {
                    return register(name, builder.newMetric());
                }
                catch(final IllegalArgumentException e)
                {
                    final Monitor<?> added = metrics.get(name);
                    if(builder.isInstance(added))
                        return (T) added;
                }
            }

            throw new IllegalArgumentException(name +" is already registered for a different type");
        }

        @Override
        public void registerAll(MetricSet metrics) throws IllegalArgumentException
        {
            registerAll(null, metrics);
        }

        private  void registerAll(final String prefix, final MetricSet set) throws IllegalArgumentException
        {
            for(Map.Entry<String, Monitor<?>> entry : set.getValue().entrySet())
            {
                Monitor<?> metric = entry.getValue();
                if(metric.getMonitorType() == MetricType.COMPOSITE)
                {
                    registerAll(name(prefix, entry.getKey()), (MetricSet)entry.getValue());
                }
                else
                {
                    register(name(prefix, entry.getKey()), entry.getValue());
                }
            }
        }

        @Override
        public boolean remove(final String name)
        {
            final Monitor<?> monitor = metrics.remove(name);
            return monitor != null;
        }

        /**
         * Concatenates elements to form a dotted name, eliding any null values or empty strings.
         *
         * @param name  the first element of the name
         * @param names the remaining elements of the name
         * @return {@code name} and {@code names} concatenated by periods
         */
        public static String name(final String name,final String... names)
        {
            final StringBuilder builder = new StringBuilder();
            append(builder, name);

            if (names != null)
            {
                for (String s : names)
                {
                    append(builder, s);
                }
            }
            return builder.toString();
        }

        /**
         * Concatenates a class name and elements to form a dotted name, eliding any null values or
         * empty strings.
         *
         * @param klass the first element of the name
         * @param names the remaining elements of the name
         * @return {@code klass} and {@code names} concatenated by periods
         */
        public static String name(final Class<?> klass, String... names)
        {
            return name(klass.getName(), names);
        }

        private static void append(final StringBuilder builder, final String part)
        {
            if (part != null && !part.isEmpty())
            {
                if (builder.length() > 0)
                {
                    builder.append('.');
                }
                builder.append(part);
            }
        }
    }

    interface MetricBuilder <T extends Monitor>
    {
        MetricBuilder<Counter> COUTNERS = new MetricBuilder<Counter>()
        {

            @Override
            public Counter newMetric()
            {
                return new Counter();
            }

            @Override
            public boolean isInstance(Monitor monitor)
            {
                return Counter.class.isInstance(monitor);
            }
        };

        MetricBuilder<Gauge> GAUGES = new MetricBuilder<Gauge>()
        {

            @Override
            public Gauge newMetric()
            {
                return new Gauge();
            }

            @Override
            public boolean isInstance(final Monitor monitor)
            {
                return Gauge.class.isInstance(monitor);
            }
        };

        /**
         * Create new metric
         * @return
         */
        T newMetric();

        /**
         * Check if the monitor
         * @param monitor
         * @return
         */
        boolean isInstance(final Monitor<?>  monitor);
    }
}
