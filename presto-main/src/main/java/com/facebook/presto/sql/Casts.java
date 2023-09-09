package com.facebook.presto.sql;

import com.facebook.presto.operator.scalar.MathFunctions;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;

import static com.google.common.base.Charsets.US_ASCII;
import static com.google.common.base.Charsets.UTF_8;

public class Casts
{
    private static final Slice TRUE = Slices.copiedBuffer("true", US_ASCII);
    private static final Slice FALSE = Slices.copiedBuffer("false", US_ASCII);

    public static Object toBoolean(Object value)
    {
        if (value instanceof Boolean) {
            return value;
        }
        else if (value instanceof Long) {
            return ((Long) value).longValue() != 0;
        }
        else if (value instanceof Slice) {
            String stringValue = ((Slice) value).toString(UTF_8).toLowerCase();
            if (stringValue.equals("t") || stringValue.equals("true") || stringValue.equals("1")) {
                return true;
            }
            if (stringValue.equals("f") || stringValue.equals("false") || stringValue.equals("0")) {
                return false;
            }
        }

        throw new IllegalArgumentException(String.format("Cannot cast %s to BOOLEAN", value.getClass().getSimpleName()));
    }

    public static Object toLong(Object value)
    {
        if (value instanceof Long) {
            return value;
        }
        else if (value instanceof Boolean) {
            return (Boolean) value ? 1L : 0L;
        }
        else if (value instanceof Slice) {
            String stringValue = ((Slice) value).toString(UTF_8);
            return Long.parseLong(stringValue);
        }
        else if (value instanceof Double) {
            return (long) MathFunctions.round(((Double) value).doubleValue());
        }

        throw new IllegalArgumentException(String.format("Cannot cast %s to BIGINT", value.getClass().getSimpleName()));
    }

    public static Object toSlice(Object value)
    {
        if (value instanceof Slice) {
            return value;
        }
        else if (value instanceof Long || value instanceof Double) {
            return Slices.copiedBuffer(value.toString(), US_ASCII);
        }
        else if (value instanceof Boolean) {
            return (Boolean) value ? TRUE : FALSE;
        }

        throw new IllegalArgumentException(String.format("Cannot cast %s to VARCHAR", value.getClass().getSimpleName()));
    }

    public static Object toDouble(Object value)
    {
        if (value instanceof Double) {
            return value;
        }
        else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        else if (value instanceof Slice) {
            String stringValue = ((Slice) value).toString(UTF_8);
            return Double.parseDouble(stringValue);
        }

        throw new IllegalArgumentException(String.format("Cannot cast %s to DOUBLE", value.getClass().getSimpleName()));
    }
}
