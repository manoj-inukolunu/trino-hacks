package com.facebook.presto.split;

import com.facebook.presto.ingest.SerializedPartitionChunk;
import com.facebook.presto.metadata.DataSourceType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImportSplit
        implements Split
{
    private String sourceName;
    private final SerializedPartitionChunk serializedChunk;

    @JsonCreator
    public ImportSplit(@JsonProperty("sourceName") String sourceName, @JsonProperty("serializedChunk") SerializedPartitionChunk serializedChunk)
    {
        this.sourceName = checkNotNull(sourceName, "sourceName is null");
        this.serializedChunk = checkNotNull(serializedChunk, "serializedChunk is null");
    }

    @Override
    public DataSourceType getDataSourceType()
    {
        return DataSourceType.IMPORT;
    }

    @JsonProperty
    public String getSourceName()
    {
        return sourceName;
    }

    @JsonProperty
    public SerializedPartitionChunk getSerializedChunk()
    {
        return serializedChunk;
    }
}
