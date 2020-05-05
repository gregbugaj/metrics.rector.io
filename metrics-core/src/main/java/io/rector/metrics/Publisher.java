package io.rector.metrics;

/**
 * Publisher interface
 */
public interface Publisher
{
    /**
     * Start streaming results to the publisher
     */
    void start();

    /**
     * Get all metrics associated with this registry as {@link Message}
     * @return collection of {@link Message}
     * @param name
     * @param metric
     */
    static Message asMessage(final String name, final Monitor<?> metric)
    {
        final Message msg = new Message();
        msg.setName(name);
        msg.setTime(System.currentTimeMillis());
        msg.setMonitorType(metric.getMonitorType());
        msg.setValue(new Message.ValueObject(metric.getType(), metric.getValue()));
        return msg;
    }
}
