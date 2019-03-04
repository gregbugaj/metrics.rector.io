package io.rector.metrics.metrics;

public class MonitoringException extends RuntimeException
{
    public MonitoringException(final String msg)
    {
        super(msg);
    }

    public MonitoringException(final Throwable t)
    {
        super(t);
    }

    public MonitoringException(final String msg, final Throwable t)
    {
        super(msg, t);
    }
}
