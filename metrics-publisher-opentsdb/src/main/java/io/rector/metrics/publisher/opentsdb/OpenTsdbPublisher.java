package io.rector.metrics.publisher.opentsdb;

import io.rector.metrics.Publisher;
import com.stumbleupon.async.Deferred;
import net.opentsdb.core.TSDB;
import net.opentsdb.uid.NoSuchUniqueName;
import net.opentsdb.uid.UniqueId;
import net.opentsdb.utils.Config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class OpenTsdbPublisher implements Publisher
{
    private TSDB tsdb;

    private ConcurrentHashMap<String, byte[]> metrics = new ConcurrentHashMap<>();

    public OpenTsdbPublisher() throws IOException {
        // Create a config object with a path to the file for parsing. Or manually
        // override settings.
        // e.g. config.overrideConfig("tsd.storage.hbase.zk_quorum", "localhost");
        // Search for a default config from /etc/opentsdb/opentsdb.conf, etc.

        Config config = new Config(true);
        tsdb = new TSDB(config);
    }


    @Override public void start()
    {

    }



    /**
     * Add new metric
     *
     * @param metric
     * @param timestamp
     * @param value
     * @param tags
     * @return
     */
    public CompletableFuture<Object> addPoint(final String metric, final long timestamp, final long value, final Map<String, String> tags)
    {
        createMetric(metric);

        final Deferred<Object> deferred = tsdb.addPoint(metric, timestamp, value, tags);
        final CompletableFuture<Object> completable = new CompletableFuture<>();

        deferred.addCallbacks(
                result -> {
                    completable.complete(result);
                    return null;
                },
                (Exception ex) -> completable.completeExceptionally(ex)
        );

        return completable;
    }

    private byte[] createMetric(final String metricName)
    {
        byte[] bytes = metrics.get(metricName);
        if(bytes != null)
            return bytes;

        // First check to see it doesn't already exist
        byte[] byteMetricUID; // we don't actually need this for the first
        // .addPoint() call below.
        // TODO: Ideally we could just call a not-yet-implemented tsdb.uIdExists()
        // function.
        // Note, however, that this is optional. If auto metric is enabled
        // (tsd.core.auto_create_metrics), the UID will be assigned in call to
        // addPoint().
        try
        {
            byteMetricUID = tsdb.getUID(UniqueId.UniqueIdType.METRIC, metricName);
        }
        catch (final IllegalArgumentException iae)
        {
            throw new RuntimeException("Invalid metric name", iae);
        }
        catch (final NoSuchUniqueName nsune)
        {
            // If not, great. Create it.
            byteMetricUID = tsdb.assignUid("metric", metricName);
        }

        bytes = metrics.get(metricName);
        if(bytes == null)
            byteMetricUID = metrics.put(metricName, byteMetricUID);

        return byteMetricUID;
    }
}
