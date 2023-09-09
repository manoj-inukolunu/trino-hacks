package com.facebook.presto.jdbc;

import com.facebook.presto.cli.ClientSession;
import com.facebook.presto.execution.QueryInfo;
import com.facebook.presto.execution.TaskInfo;
import com.facebook.presto.server.HttpQueryClient;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.FunctionCall;
import com.facebook.presto.sql.tree.Serialization;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.airlift.http.client.ApacheHttpClient;
import io.airlift.http.client.HttpClientConfig;
import io.airlift.http.client.HttpRequestFilter;
import io.airlift.json.JsonCodec;
import io.airlift.json.JsonCodecFactory;
import io.airlift.json.ObjectMapperProvider;
import io.airlift.units.Duration;
import org.codehaus.jackson.map.JsonDeserializer;


import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class QueryExecutor
        implements Closeable
{
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final JsonCodec<QueryInfo> queryInfoCodec;
    private final JsonCodec<TaskInfo> taskInfoCodec;
    private final ApacheHttpClient httpClient;

    public QueryExecutor(String userAgent,
            JsonCodec<QueryInfo> queryInfoCodec,
            JsonCodec<TaskInfo> taskInfoCodec)
    {
        checkNotNull(userAgent, "userAgent is null");
        this.queryInfoCodec = checkNotNull(queryInfoCodec, "queryInfoCodec is null");
        this.taskInfoCodec = checkNotNull(taskInfoCodec, "taskInfoCodec is null");
        this.httpClient = new ApacheHttpClient(new HttpClientConfig()
                .setConnectTimeout(new Duration(1, TimeUnit.DAYS))
                .setReadTimeout(new Duration(10, TimeUnit.DAYS)),
                ImmutableSet.<HttpRequestFilter>of(new UserAgentRequestFilter(userAgent)));
    }

    public HttpQueryClient startQuery(ClientSession session, String query)
    {
        return new HttpQueryClient(session, query, httpClient, executor, queryInfoCodec, taskInfoCodec);
    }

    
    @Override
    public void close()
    {
        executor.shutdownNow();
        httpClient.close();
    }

    public static QueryExecutor create(String userAgent)
    {
        JsonCodecFactory codecs = createCodecFactory();
        JsonCodec<QueryInfo> queryInfoCodec = codecs.jsonCodec(QueryInfo.class);
        JsonCodec<TaskInfo> taskInfoCodec = codecs.jsonCodec(TaskInfo.class);
        return new QueryExecutor(userAgent, queryInfoCodec, taskInfoCodec);
    }

    private static JsonCodecFactory createCodecFactory()
    {
        ObjectMapperProvider objectMapperProvider = new ObjectMapperProvider();
        ImmutableMap.Builder<Class<?>, JsonDeserializer<?>> deserializers = ImmutableMap.builder();
        deserializers.put(Expression.class, new Serialization.ExpressionDeserializer());
        deserializers.put(FunctionCall.class, new Serialization.FunctionCallDeserializer());
        objectMapperProvider.setJsonDeserializers(deserializers.build());
        return new JsonCodecFactory(objectMapperProvider);
    }
}
