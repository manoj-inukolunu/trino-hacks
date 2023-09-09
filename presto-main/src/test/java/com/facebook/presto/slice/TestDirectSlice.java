/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.presto.slice;

import io.airlift.slice.Slice;
import io.airlift.slice.Slices;

import java.nio.ByteBuffer;

public class TestDirectSlice
        extends TestSlice
{
    @Override
    protected Slice allocate(int size)
    {
        if (size == 0) {
            return Slices.EMPTY_SLICE;
        }
        return Slices.allocate(size);
    }
}
