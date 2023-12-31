package com.facebook.presto.operator.aggregation;

import com.facebook.presto.block.Block;
import com.facebook.presto.block.BlockBuilder;
import com.facebook.presto.block.BlockCursor;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import com.facebook.presto.tuple.Tuple;
import com.facebook.presto.tuple.TupleInfo;
import com.facebook.presto.tuple.Tuples;
import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.*;

/**
 * A wrapper that inserts a null in every other position
 */
public class AlternatingNullsBlockCursor
        implements BlockCursor
{
    private final BlockCursor delegate;
    private final Tuple nullTuple;
    private int index = -1;

    public AlternatingNullsBlockCursor(BlockCursor delegate)
    {
        this.delegate = delegate;
        nullTuple = Tuples.nullTuple(this.delegate.getTupleInfo());
    }

    @Override
    public TupleInfo getTupleInfo()
    {
        return delegate.getTupleInfo();
    }

    @Override
    public int getRemainingPositions()
    {
        return delegate.getRemainingPositions() * 2 + (isNullPosition() ? 1 : 0);
    }

    @Override
    public boolean isValid()
    {
        return index > 0 && delegate.isValid();
    }

    @Override
    public boolean isFinished()
    {
        return delegate.isFinished();
    }

    @Override
    public boolean advanceNextPosition()
    {
        index++;
        return isNullPosition() || delegate.advanceNextPosition();
    }

    private boolean isNullPosition()
    {
        return index % 2 == 0;
    }

    @Override
    public boolean advanceToPosition(int position)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Block getRegionAndAdvance(int length)
    {
        throw new UnsupportedOperationException("No block form for " + getClass().getSimpleName());
    }

    @Override
    public Tuple getTuple()
    {
        if (isNullPosition()) {
            return nullTuple;
        }
        return delegate.getTuple();
    }

    @Override
    public long getLong(int field)
    {
        if (isNullPosition()) {
            return 0;
        }
        return delegate.getLong(field);
    }

    @Override
    public double getDouble(int field)
    {
        if (isNullPosition()) {
            return 0;
        }
        return delegate.getDouble(field);
    }

    @Override
    public Slice getSlice(int field)
    {
        if (isNullPosition()) {
            return Slices.EMPTY_SLICE;
        }
        return delegate.getSlice(field);
    }

    @Override
    public boolean isNull(int field)
    {
        return isNullPosition() || delegate.isNull(field);
    }

    @Override
    public int getPosition()
    {
        return index;
    }

    @Override
    public boolean currentTupleEquals(Tuple value)
    {
        if (isNullPosition()) {
            return nullTuple.equals(value);
        }
        return delegate.currentTupleEquals(value);
    }

    @Override
    public int getRawOffset()
    {
        checkState(!isNullPosition(), "should not be called on a null position");
        return delegate.getRawOffset();
    }

    @Override
    public Slice getRawSlice()
    {
        checkState(!isNullPosition(), "should not be called on a null position");
        return delegate.getRawSlice();
    }

    @Override
    public void appendTupleTo(BlockBuilder blockBuilder)
    {
        if (isNullPosition()) {
            blockBuilder.append(nullTuple);
        }
        delegate.appendTupleTo(blockBuilder);
    }
}
