package com.facebook.presto.split;

import com.facebook.presto.metadata.ColumnHandle;
import com.facebook.presto.operator.Operator;

import java.util.List;

public interface DataStreamProvider
{
    Operator createDataStream(Split split, List<ColumnHandle> columns);
}
