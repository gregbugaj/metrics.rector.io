package io.rector.metrics.publisher.timescaledb;

public class DatabaseConfig
{
    private String url;

    private String username;

    private String password;

    private String driverClassName;

    private int maximumPoolSize;

    private Boolean skipConnectionTest = false;

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl(final String url)
    {
        this.url = url;
    }

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(final String username)
    {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(final String password)
    {
        this.password = password;
    }

    /**
     * @return the driverClassName
     */
    public String getDriverClassName()
    {
        return driverClassName;
    }

    /**
     * @param driverClassName
     *            the driverClassName to set
     */
    public void setDriverClassName(final String driverClassName)
    {
        this.driverClassName = driverClassName;
    }

    public int getMaximumPoolSize()
    {
        return maximumPoolSize;
    }

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
}
