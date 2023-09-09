package com.facebook.presto.sql.analyzer;

import com.facebook.presto.tuple.TupleInfo;
import com.google.common.base.Function;

import javax.annotation.Nullable;

public enum Type
{
    LONG(TupleInfo.Type.FIXED_INT_64),
    DOUBLE(TupleInfo.Type.DOUBLE),
    STRING(TupleInfo.Type.VARIABLE_BINARY),
    BOOLEAN(null),
    NULL(null);

    private final TupleInfo.Type rawType;

    Type(@Nullable TupleInfo.Type rawType)
    {
        this.rawType = rawType;
    }

    public TupleInfo.Type getRawType()
    {
        return rawType;
    }

    public static Type fromRaw(TupleInfo.Type raw)
    {
        for (Type type : values()) {
            if (type.getRawType() == raw) {
                return type;
            }
        }

        throw new IllegalArgumentException("Can't map raw type to Type: " + raw);
    }

    public static Function<Type, TupleInfo.Type> toRaw()
    {
        return new Function<Type, TupleInfo.Type>()
        {
            @Override
            public TupleInfo.Type apply(Type input)
            {
                return input.getRawType();
            }
        };
    }

    public static boolean isNumeric(Type type)
    {
        return type == LONG || type == DOUBLE;
    }
}
