package io.rector.metrics;

import java.util.Map;

public class MetricSet implements Monitor<Map<String, Monitor<?>>>
{
    @Override
    public Map<String, Monitor<?>> getValue()
    {
        return null;
    }

    @Override
    public Type getType()
    {
        throw new IllegalStateException("Method not supported for this type");
    }

    @Override
    public MetricType getMonitorType()
    {
        return MetricType.COMPOSITE;
    }
}
