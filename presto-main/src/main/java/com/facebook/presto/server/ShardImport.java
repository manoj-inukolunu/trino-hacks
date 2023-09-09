package com.facebook.presto.server;

import com.facebook.presto.importer.ImportField;
import com.facebook.presto.ingest.SerializedPartitionChunk;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public class ShardImport
{
    private final String sourceName;
    private final SerializedPartitionChunk partitionChunk;
    private final List<ImportField> fields;

    @JsonCreator
    public ShardImport(
            @JsonProperty("sourceName") String sourceName,
            @JsonProperty("partitionChunk") SerializedPartitionChunk partitionChunk,
            @JsonProperty("fields") List<ImportField> fields)
    {
        this.sourceName = checkNotNull(sourceName, "sourceName");
        this.partitionChunk = checkNotNull(partitionChunk, "partitionChunk");
        this.fields = ImmutableList.copyOf(checkNotNull(fields, "fields is null"));
        checkArgument(!fields.isEmpty(), "fields is empty");
    }

    @JsonProperty
    public String getSourceName()
    {
        return sourceName;
    }

    @JsonProperty
    public SerializedPartitionChunk getPartitionChunk()
    {
        return partitionChunk;
    }

    @JsonProperty
    public List<ImportField> getFields()
    {
        return fields;
    }
}
