package com.facebook.presto.block.uncompressed;

import com.facebook.presto.block.Block;
import com.facebook.presto.block.BlockBuilder;
import com.facebook.presto.block.BlockCursor;
import io.airlift.slice.Slice;
import com.facebook.presto.tuple.Tuple;
import com.facebook.presto.tuple.TupleInfo;
import com.google.common.base.Preconditions;


import static com.facebook.presto.tuple.TupleInfo.SINGLE_DOUBLE;
import static io.airlift.slice.SizeOf.SIZE_OF_BYTE;
import static io.airlift.slice.SizeOf.SIZE_OF_DOUBLE;

public class UncompressedDoubleBlockCursor
        implements BlockCursor
{
    private static final int ENTRY_SIZE = SIZE_OF_DOUBLE + SIZE_OF_BYTE;
    private final Slice slice;
    private final int positionCount;

    private int position;
    private int offset;

    public UncompressedDoubleBlockCursor(int positionCount, Slice slice, int sliceOffset)
    {
        Preconditions.checkArgument(positionCount >= 0, "positionCount is negative");
        Preconditions.checkNotNull(positionCount, "positionCount is null");
        Preconditions.checkPositionIndex(sliceOffset, slice.length(), "sliceOffset");

        this.positionCount = positionCount;

        this.slice = slice;

        // start one position before the start
        position = -1;
        offset = sliceOffset - ENTRY_SIZE;
    }

    @Override
    public TupleInfo getTupleInfo()
    {
        return TupleInfo.SINGLE_DOUBLE;
    }

    @Override
    public int getRemainingPositions()
    {
        return positionCount - (position + 1);
    }

    @Override
    public boolean isValid()
    {
        return 0 <= position && position < positionCount;
    }

    @Override
    public boolean isFinished()
    {
        return position >= positionCount;
    }

    private void checkReadablePosition()
    {
        Preconditions.checkState(isValid(), "cursor is not valid");
    }

    @Override
    public boolean advanceNextPosition()
    {
        if (position >= positionCount -1) {
            position = positionCount;
            return false;
        }

        position++;
        offset += ENTRY_SIZE;
        return true;
    }

    @Override
    public boolean advanceToPosition(int newPosition)
    {
        // if new position is out of range, return false
        if (newPosition >= positionCount) {
            position = positionCount;
            return false;
        }

        Preconditions.checkArgument(newPosition >= this.position, "Can't advance backwards");

        offset += (newPosition - position) * ENTRY_SIZE;
        position = newPosition;

        return true;
    }

    @Override
    public Block getRegionAndAdvance(int length)
    {
        // view port starts at next position
        int startOffset = offset + ENTRY_SIZE;
        length = Math.min(length, getRemainingPositions());

        // advance to end of view port
        offset += length * ENTRY_SIZE;
        position += length;

        return new UncompressedBlock(length, SINGLE_DOUBLE, slice, startOffset);
    }

    @Override
    public int getPosition()
    {
        checkReadablePosition();
        return position;
    }

    @Override
    public Tuple getTuple()
    {
        checkReadablePosition();
        return new Tuple(slice.slice(offset, ENTRY_SIZE), TupleInfo.SINGLE_DOUBLE);
    }

    @Override
    public long getLong(int field)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(int field)
    {
        checkReadablePosition();
        Preconditions.checkElementIndex(0, 1, "field");
        return slice.getDouble(offset + SIZE_OF_BYTE);
    }

    @Override
    public Slice getSlice(int field)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNull(int field)
    {
        checkReadablePosition();
        Preconditions.checkElementIndex(0, 1, "field");
        return slice.getByte(offset) != 0;
    }

    @Override
    public boolean currentTupleEquals(Tuple value)
    {
        checkReadablePosition();
        Slice tupleSlice = value.getTupleSlice();
        return tupleSlice.length() == ENTRY_SIZE && slice.getDouble(offset + SIZE_OF_BYTE) == tupleSlice.getDouble(SIZE_OF_BYTE);
    }

    @Override
    public int getRawOffset()
    {
        return offset;
    }

    @Override
    public Slice getRawSlice()
    {
        return slice;
    }

    @Override
    public void appendTupleTo(BlockBuilder blockBuilder)
    {
        blockBuilder.appendTuple(slice, offset, ENTRY_SIZE);
    }
}
