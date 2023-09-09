package com.facebook.presto.benchmark;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import io.airlift.json.JsonCodec;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class JsonAvgBenchmarkResultWriter
        implements BenchmarkResultHook
{
    private static final JsonCodec<BuildResult> JSON_CODEC = JsonCodec.jsonCodec(BuildResult.class);
    private final OutputStream outputStream;

    private int sampleCount;
    public long totalElapsedMillis;
    public long totalInputRows;
    public long totalInputRowsPerSecond;
    public long totalOutputRows;
    public long totalOutputRowsPerSecond;
    public long totalInputMegabytes;
    public long totalInputMegabytesPerSecond;

    public JsonAvgBenchmarkResultWriter(OutputStream outputStream)
    {
        Preconditions.checkNotNull(outputStream, "outputStream is null");
        this.outputStream = outputStream;
    }

    @Override
    public BenchmarkResultHook addResults(Map<String, Long> results)
    {
        Preconditions.checkNotNull(results, "results is null");
        sampleCount++;
        totalElapsedMillis += getValue(results, "elapsed_millis");
        totalInputRows += getValue(results, "input_rows;");
        totalInputRowsPerSecond += getValue(results, "input_rows_per_second");
        totalOutputRows += getValue(results, "output_rows");
        totalOutputRowsPerSecond += getValue(results, "output_rows_per_second");
        totalInputMegabytes += getValue(results, "input_megabytes");
        totalInputMegabytesPerSecond += getValue(results, "input_megabytes_per_second");
        return this;
    }

    private long getValue(Map<String, Long> results, String name)
    {
        Long value = results.get(name);
        if (value == null) {
            return 0;
        }
        return value;
    }

    @Override
    public void finished()
    {
        BuildResult average = new BuildResult();
        average.elapsedMillis += totalElapsedMillis / sampleCount;
        average.inputRows += totalInputRows / sampleCount;
        average.inputRowsPerSecond += totalInputRowsPerSecond / sampleCount;
        average.outputRows += totalOutputRows / sampleCount;
        average.outputRowsPerSecond += totalOutputRowsPerSecond / sampleCount;
        average.inputMegabytes += totalInputRows / sampleCount;
        average.inputMegabytesPerSecond += totalInputMegabytesPerSecond / sampleCount;

        String json = JSON_CODEC.toJson(average);
        try {
            outputStream.write(json.getBytes(Charsets.UTF_8));
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static class BuildResult
    {
        @JsonProperty
        public long elapsedMillis;
        @JsonProperty
        public long inputRows;
        @JsonProperty
        public long inputRowsPerSecond;
        @JsonProperty
        public long outputRows;
        @JsonProperty
        public long outputRowsPerSecond;
        @JsonProperty
        public long inputMegabytes;
        @JsonProperty
        public long inputMegabytesPerSecond;
    }
}
