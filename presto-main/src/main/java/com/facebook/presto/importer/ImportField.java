package com.facebook.presto.importer;

import com.facebook.presto.metadata.ImportColumnHandle;
import com.facebook.presto.metadata.NativeColumnHandle;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public class ImportField
{
    private final ImportColumnHandle sourceColumnHandle;
    private final NativeColumnHandle targetColumnHandle;

    @JsonCreator
    public ImportField(
            @JsonProperty("sourceColumnHandle") ImportColumnHandle sourceColumnHandle,
            @JsonProperty("targetColumnHandle") NativeColumnHandle targetColumnHandle)
    {
        this.sourceColumnHandle = checkNotNull(sourceColumnHandle, "sourceColumnHandle is null");
        this.targetColumnHandle = checkNotNull(targetColumnHandle, "targetColumnHandle is null");
    }

    @JsonProperty
    public ImportColumnHandle getSourceColumnHandle()
    {
        return sourceColumnHandle;
    }

    @JsonProperty
    public NativeColumnHandle getTargetColumnHandle()
    {
        return targetColumnHandle;
    }

    public static Function<ImportField, ImportColumnHandle> sourceColumnHandleGetter()
    {
        return new Function<ImportField, ImportColumnHandle>()
        {
            @Override
            public ImportColumnHandle apply(ImportField input)
            {
                return input.getSourceColumnHandle();
            }
        };
    }

    public static Function<ImportField, Long> targetColumnIdGetter()
    {
        return new Function<ImportField, Long>()
        {
            @Override
            public Long apply(ImportField input)
            {
                return input.getTargetColumnHandle().getColumnId();
            }
        };
    }
}
