package io.rector.metrics.publisher.csv;

import io.rector.metrics.Message;
import io.rector.metrics.Monitor;
import io.rector.metrics.Publisher;

public abstract class BasePublisher implements Publisher
{

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

}
