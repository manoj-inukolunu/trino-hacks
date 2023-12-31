package com.facebook.presto.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

public class MapTransformer<K, V>
{
    private final Map<K, V> map;

    public static <K, V> MapTransformer<K, V> of(Map<K, V> map)
    {
        return new MapTransformer<>(map);
    }

    public MapTransformer(Map<K, V> map)
    {
        this.map = map;
    }

    public <V1> MapTransformer<K, V1> transformValues(Function<V, V1> function)
    {
        return new MapTransformer<>(Maps.transformValues(map, function));
    }

    public MapTransformer<K, V> filterValues(Predicate<? super V> predicate)
    {
        return new MapTransformer<>(Maps.filterValues(map, predicate));
    }

    public <V1 extends V> MapTransformer<K, V1> castValues(Class<V1> clazz)
    {
        return transformValues(MoreFunctions.<V, V1>cast(clazz));
    }

    public Map<K, V> map()
    {
        return map;
    }

    public MapTransformer<V, K> inverse()
    {
        return new MapTransformer<>(ImmutableBiMap.copyOf(map).inverse());
    }

    public BiMap<K, V> biMap()
    {
        return ImmutableBiMap.copyOf(map);
    }

    public Map<K, V> immutableMap()
    {
        return ImmutableMap.copyOf(map);
    }
}
