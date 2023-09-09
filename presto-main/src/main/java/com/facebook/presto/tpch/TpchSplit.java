package com.facebook.presto.tpch;

import com.facebook.presto.metadata.DataSourceType;
import com.facebook.presto.split.Split;
import com.google.common.base.Preconditions;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

// Right now, splits are just the entire TPCH table
public class TpchSplit
    implements Split
{
    private final TpchTableHandle tableHandle;

    @JsonCreator
    public TpchSplit(@JsonProperty("tableHandle") TpchTableHandle tableHandle)
    {
        this.tableHandle = Preconditions.checkNotNull(tableHandle, "tableHandle is null");
    }

    @Override
    public DataSourceType getDataSourceType()
    {
        throw new UnsupportedOperationException();
    }

    @JsonProperty
    public TpchTableHandle getTableHandle()
    {
        return tableHandle;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TpchSplit)) {
            return false;
        }

        TpchSplit tpchSplit = (TpchSplit) o;

        if (!tableHandle.equals(tpchSplit.tableHandle)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return tableHandle.hashCode();
    }
}
