package com.facebook.presto.tuple;

import io.airlift.slice.Slice;
import io.airlift.slice.SliceOutput;
import com.facebook.presto.tuple.TupleInfo.Type;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import io.airlift.slice.Slice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;

public class Tuple
        implements TupleReadable
{
    private final Slice slice;
    private final TupleInfo tupleInfo;

    public Tuple(Slice slice, TupleInfo tupleInfo)
    {
        this.slice = slice;
        this.tupleInfo = tupleInfo;
    }

    @Override
    public TupleInfo getTupleInfo()
    {
        return tupleInfo;
    }

    @Override
    public Tuple getTuple()
    {
        return this;
    }

    public Slice getTupleSlice()
    {
        return slice;
    }

    @Override
    public long getLong(int index)
    {
        return tupleInfo.getLong(slice, index);
    }

    @Override
    public double getDouble(int index)
    {
        return tupleInfo.getDouble(slice, index);
    }

    @Override
    public Slice getSlice(int index)
    {
        return tupleInfo.getSlice(slice, index);
    }

    @Override
    public boolean isNull(int index)
    {
        return tupleInfo.isNull(slice, index);
    }

    public int size()
    {
        return tupleInfo.size(slice);
    }

    public void writeTo(SliceOutput out)
    {
        out.writeBytes(slice);
    }

    /**
     * Materializes the tuple values as Java Object.
     * This method is mainly for diagnostics and should not be called in normal query processing.
     */
    public List<Object> toValues()
    {
        ArrayList<Object> values = new ArrayList<>();
        int index = 0;
        for (Type type : tupleInfo.getTypes()) {
            if (isNull(index)) {
                values.add(null);
            }
            else {
                switch (type) {
                    case FIXED_INT_64:
                        values.add(getLong(index));
                        break;
                    case DOUBLE:
                        values.add(getDouble(index));
                        break;
                    case VARIABLE_BINARY:
                        Slice slice = getSlice(index);
                        values.add(slice.toString(UTF_8));
                        break;
                }
            }
            index++;
        }
        return Collections.unmodifiableList(values);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tuple tuple = (Tuple) o;

        if (!slice.equals(tuple.slice)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = slice.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        String value = Joiner.on(",").useForNull("NULL").join(toValues()).replace("\n", "\\n");
        return Objects.toStringHelper(this)
                .add("slice", slice)
                .add("tupleInfo", tupleInfo)
                .add("value", "{" + value + "}")
                .toString();
    }
}
