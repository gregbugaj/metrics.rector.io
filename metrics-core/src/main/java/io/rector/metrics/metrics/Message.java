package io.rector.metrics.metrics;

import java.util.Date;
import java.util.Map;

/**
 * Message containing all relevant metrics information that will be published via a {@link Publisher}
 */
public class Message
{
    /**
     * Application Id
     */
    private String appId;

    /**
     * Metric name
     */
    private String name;

    /**
     * The value the metric is holding
     */
    private ValueObject value;

    /**
     * Source of the event
     */
    private String source;

    /**
     * Metric type
     */
    private MetricType monitorType = MetricType.GAUGE;

    /**
     * Time the message have been created
     */
    private long time;

    /**
     * Additional attributes associated witht the message
     */
    private Map<String, Object> attributes;

    /**
     * @return the value
     */
    public ValueObject getValue()
    {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(final ValueObject value)
    {
        this.value = value;
    }

    /**
     * @return the time
     */
    public long getTime()
    {
        return time;
    }

    /**
     * @param time
     *            the time to set
     */
    public void setTime(final long time)
    {
        this.time = time;
    }

    /**
     * @return the appId
     */
    public String getAppId()
    {
        return appId;
    }

    /**
     * @param appId
     *            the appId to set
     */
    public void setAppId(final String appId)
    {
        this.appId = appId;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(final String source)
    {
        this.source = source;
    }

    public MetricType getMonitorType()
    {
        return monitorType;
    }

    public void setMonitorType(final MetricType monitorType)
    {
        this.monitorType = monitorType;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(appId).append(":").append(name).append(":").append("[").append(value).append("]").append(":")
            .append(new Date(time));
        return sb.toString();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static class ValueObject
    {
        private Type type;

        private Object value;

        public ValueObject()
        {
             // required for serialization
        }

        public ValueObject(final Type type, final Object value)
        {
            this.type = type;
            this.value = value;
        }

        /**
         * @return the type
         */
        public Type getType()
        {
            return type;
        }

        /**
         * @param type
         *            the type to set
         */
        public void setType(final Type type)
        {
            this.type = type;
        }

        /**
         * @return the value
         */
        public Object getValue()
        {
            return value;
        }

        /**
         * @param value
         *            the value to set
         */
        public void setValue(final Object value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return type + "," + value;
        }

        public Object getValueAsString()
        {
            if(value == null)
                return "";
            return value.toString();
        }
    }
}
