package io.rector.metrics;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public abstract class Publisher
{
    protected MonitorRegistry registry;

    private boolean resetOnReporting;

    private long time;

    private TimeUnit unit;

    private Semaphore mutex = new Semaphore(1);

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public Publisher()
    {
        // required for composite
    }

    public Publisher(final MonitorRegistry registry,
                     final long time,
                     final TimeUnit unit,
                     final boolean resetOnReporting)
    {
        this.registry = Objects.requireNonNull(registry);
        this.resetOnReporting = resetOnReporting;
        this.time = time;
        this.unit = unit;
    }

    /**
     * Get all metrics associated with this registry as {@link Message}
     *
     * @param name
     * @param metric
     * @return collection of {@link Message}
     */
    protected Message asMessage(final String name, final Monitor<?> metric)
    {
        final Message msg = new Message();
        msg.setName(name);
        msg.setTime(System.currentTimeMillis());
        msg.setMonitorType(metric.getMonitorType());
        msg.setValue(new Message.ValueObject(metric.getType(), metric.getValue()));
        return msg;
    }

    public void start()
    {
        System.out.println("Publisher.start");
        executor.scheduleAtFixedRate(this::publishToSink, time, time, unit);
    }

    /**
     * Apply action to each metric
     *
     * @param action
     */
    protected void forEach(BiConsumer<String, Monitor<?>> action)
    {
        Objects.requireNonNull(action);
        final Map<String, Monitor<?>> metrics = registry.getMetrics();
        if (metrics != null)
        {
            registry.getMetrics().forEach(action);
        }
    }

    /**
     * Publish messages
     */
    protected abstract void publish();

    /**
     * Publish data to the sink, this method will hold a mutex for duration of the execution
     */
    private void publishToSink()
    {
        System.out.println("Publisher.publishToSink");

        try
        {
            mutex.acquire();
            publish();

            if (resetOnReporting)
            {
                forEach(this::reset);
            }
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        finally
        {
            mutex.release();
        }
    }

    private void reset(final String name, final Monitor<?> metric)
    {
        if (metric instanceof Resettable)
        {
            ((Resettable) metric).reset();
        }
    }
}
