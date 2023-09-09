package com.facebook.presto.execution;

import com.google.common.collect.ImmutableMap;
import io.airlift.configuration.testing.ConfigAssertions;
import io.airlift.units.DataSize;
import io.airlift.units.DataSize.Unit;
import io.airlift.units.Duration;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestQueryManagerConfig
{
    @Test
    public void testDefaults()
    {
        ConfigAssertions.assertRecordedDefaults(ConfigAssertions.recordDefaults(QueryManagerConfig.class)
                .setCoordinator(true)
                .setImportsEnabled(true)
                .setMaxShardProcessorThreads(Runtime.getRuntime().availableProcessors() * 4)
                .setMaxQueryAge(new Duration(15, TimeUnit.MINUTES))
                .setClientTimeout(new Duration(1, TimeUnit.MINUTES))
                .setMaxOperatorMemoryUsage(new DataSize(256, Unit.MEGABYTE))
                .setMaxSplitCount(100_000));
    }

    @Test
    public void testExplicitPropertyMappings()
    {
        Map<String, String> properties = new ImmutableMap.Builder<String, String>()
                .put("coordinator", "false")
                .put("import.enabled", "false")
                .put("query.operator.max-memory", "1GB")
                .put("query.shard.max-threads", "3")
                .put("query.client.timeout", "10s")
                .put("query.max-age", "30s")
                .put("query.max-splits", "100")
                .build();

        QueryManagerConfig expected = new QueryManagerConfig()
                .setCoordinator(false)
                .setMaxOperatorMemoryUsage(new DataSize(1, Unit.GIGABYTE))
                .setMaxShardProcessorThreads(3)
                .setMaxQueryAge(new Duration(30, TimeUnit.SECONDS))
                .setClientTimeout(new Duration(10, TimeUnit.SECONDS))
                .setImportsEnabled(false)
                .setMaxSplitCount(100);

        ConfigAssertions.assertFullMapping(properties, expected);
    }
}
