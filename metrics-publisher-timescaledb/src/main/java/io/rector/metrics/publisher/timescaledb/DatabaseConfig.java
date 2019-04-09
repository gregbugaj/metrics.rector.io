package io.rector.metrics.publisher.timescaledb;

public class DatabaseConfig
{
    private String url;

    private String username;

    private String password;

    private String driverClassName;

    private int maximumPoolSize;

    private Boolean skipConnectionTest = false;

    public DatabaseConfig setMaximumPoolSize(final int maximumPoolSize)
    {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    public Boolean getSkipConnectionTest()
    {
        return skipConnectionTest;
    }

    public DatabaseConfig setSkipConnectionTest(final Boolean skipConnectionTest)
    {
        this.skipConnectionTest = skipConnectionTest;
        return this;
    }

    public String getUrl()
    {
        return url;
    }

    public DatabaseConfig setUrl(final String url)
    {
        this.url = url;
        return this;
    }

    public String getUsername()
    {
        return username;
    }

    public DatabaseConfig setUsername(final String username)
    {
        this.username = username;
        return this;
    }

    public String getPassword()
    {
        return password;
    }

    public DatabaseConfig setPassword(final String password)
    {
        this.password = password;
        return this;
    }

    public String getDriverClassName()
    {
        return driverClassName;
    }

    public DatabaseConfig setDriverClassName(final String driverClassName)
    {
        this.driverClassName = driverClassName;
        return this;
    }

    public int getMaximumPoolSize()
    {
        return maximumPoolSize;
    }
}
