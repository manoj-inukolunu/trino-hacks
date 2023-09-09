package com.facebook.presto.split;

import com.facebook.presto.block.BlockIterable;
import com.facebook.presto.metadata.ColumnHandle;
import com.facebook.presto.metadata.NativeColumnHandle;
import com.facebook.presto.metadata.StorageManager;
import com.facebook.presto.operator.AlignmentOperator;
import com.facebook.presto.operator.Operator;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class NativeDataStreamProvider
        implements DataStreamProvider
{
    private final StorageManager storageManager;

    @Inject
    public NativeDataStreamProvider(StorageManager storageManager)
    {
        this.storageManager = checkNotNull(storageManager, "storageManager is null");
    }

    @Override
    public Operator createDataStream(Split split, List<ColumnHandle> columns)
    {
        checkNotNull(split, "split is null");
        checkArgument(split instanceof NativeSplit, "Split must be of type NativeType, not %s", split.getClass().getName());
        assert split instanceof NativeSplit; // // IDEA-60343
        checkNotNull(columns, "columns is null");
        checkArgument(!columns.isEmpty(), "must provide at least one column");

        NativeSplit nativeSplit = (NativeSplit) split;

        ImmutableList.Builder<BlockIterable> builder = ImmutableList.builder();
        for (ColumnHandle column : columns) {
            checkArgument(column instanceof NativeColumnHandle, "column must be of type NativeColumnHandle, not %s", column.getClass().getName());
            assert column instanceof NativeColumnHandle; // // IDEA-60343
            NativeColumnHandle nativeColumn = (NativeColumnHandle) column;
            builder.add(storageManager.getBlocks(nativeSplit.getShardId(), nativeColumn.getColumnId()));
        }
        return new AlignmentOperator(builder.build());
    }
}
