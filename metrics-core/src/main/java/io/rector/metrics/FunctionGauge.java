package io.rector.metrics;

import java.util.Objects;
import java.util.function.Supplier;

public class FunctionGauge implements Monitor<Double>
{
    private final Supplier<Double> supplier;

    public FunctionGauge(final Supplier<Double> supplier)
    {
        this.supplier = Objects.requireNonNull(supplier);
    }

    @Override
    public Double getValue()
    {
        try
        {
            return supplier.get();
        }
        catch (final Exception e)
        {
            return Double.NaN;
        }
    }

    @Override
    public Type getType()
    {
        return Type.NUMBER;
    }

    @Override
    public MetricType getMonitorType()
    {
        return MetricType.GAUGE;
    }

    @Override
    public String toString()
    {
        return getMonitorType()+ " : " + getValue();
    }
}
