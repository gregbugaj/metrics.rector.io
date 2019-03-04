package io.rector.metrics;

@FunctionalInterface
public interface InfoGauge extends Gauge<String>
{
    @Override
    default Type getType()
    {
        return Type.STRING;
    }
}
