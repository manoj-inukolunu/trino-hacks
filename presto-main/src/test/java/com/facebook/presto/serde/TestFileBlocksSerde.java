/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.presto.serde;

import com.facebook.presto.block.BlockBuilder;
import com.facebook.presto.block.uncompressed.UncompressedBlock;
import io.airlift.slice.DynamicSliceOutput;
import io.airlift.slice.Slice;
import com.google.common.collect.ImmutableList;
import com.google.common.io.OutputSupplier;
import org.testng.annotations.Test;

import java.util.List;

import static com.facebook.presto.block.BlockAssertions.toValues;
import static com.facebook.presto.serde.BlocksFileReader.readBlocks;
import static com.facebook.presto.serde.BlocksFileWriter.writeBlocks;
import static com.facebook.presto.tuple.TupleInfo.SINGLE_VARBINARY;
import static org.testng.Assert.assertEquals;

public class TestFileBlocksSerde
{
    private final List<ImmutableList<String>> expectedValues = ImmutableList.of(
            ImmutableList.of("alice"),
            ImmutableList.of("bob"),
            ImmutableList.of("charlie"),
            ImmutableList.of("dave"),
            ImmutableList.of("alice"),
            ImmutableList.of("bob"),
            ImmutableList.of("charlie"),
            ImmutableList.of("dave"),
            ImmutableList.of("alice"),
            ImmutableList.of("bob"),
            ImmutableList.of("charlie"),
            ImmutableList.of("dave"));

    private final UncompressedBlock expectedBlock = new BlockBuilder(SINGLE_VARBINARY)
            .append("alice")
            .append("bob")
            .append("charlie")
            .append("dave")
            .build();

    @Test
    public void testRoundTrip()
    {
        for (BlocksFileEncoding encoding : BlocksFileEncoding.values()) {
            testRoundTrip(encoding);
        }
    }

    public void testRoundTrip(BlocksFileEncoding encoding)
    {
        DynamicSliceOutputSupplier sliceOutput = new DynamicSliceOutputSupplier(1024);
        writeBlocks(encoding, sliceOutput, expectedBlock, expectedBlock, expectedBlock);
        Slice slice = sliceOutput.getLastSlice();
        BlocksFileReader actualBlocks = readBlocks(slice);

        List<List<Object>> actualValues = toValues(actualBlocks);

        assertEquals(actualValues, expectedValues);

        BlocksFileStats stats = actualBlocks.getStats();
        assertEquals(stats.getAvgRunLength(), 1);
        assertEquals(stats.getRowCount(), 12);
        assertEquals(stats.getRunsCount(), 12);
        assertEquals(stats.getUniqueCount(), 4);
    }

    private static class DynamicSliceOutputSupplier implements OutputSupplier<DynamicSliceOutput>
    {
        private final int estimatedSize;
        private DynamicSliceOutput lastOutput;

        public DynamicSliceOutputSupplier(int estimatedSize)
        {
            this.estimatedSize = estimatedSize;
        }

        public Slice getLastSlice()
        {
            return lastOutput.slice();
        }

        @Override
        public DynamicSliceOutput getOutput()
        {
            lastOutput = new DynamicSliceOutput(estimatedSize);
            return lastOutput;
        }
    }
}
