package com.facebook.presto.metadata;

import com.facebook.presto.operator.AggregationFunctionDefinition;
import com.facebook.presto.operator.aggregation.AggregationFunction;
import com.facebook.presto.operator.Input;
import com.facebook.presto.sql.tree.QualifiedName;
import com.facebook.presto.tuple.TupleInfo;
import com.facebook.presto.tuple.TupleInfo.Type;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import java.lang.invoke.MethodHandle;
import java.util.List;

import static com.facebook.presto.operator.AggregationFunctionDefinition.aggregation;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class FunctionInfo implements Comparable<FunctionInfo>
{
    private final int id;

    private final QualifiedName name;
    private final TupleInfo.Type returnType;
    private final List<TupleInfo.Type> argumentTypes;

    private final boolean isAggregate;
    private final TupleInfo.Type intermediateType;
    private final AggregationFunction aggregationFunction;

    private final MethodHandle scalarFunction;
    private final boolean deterministic;

    public FunctionInfo(int id, QualifiedName name, TupleInfo.Type returnType, List<TupleInfo.Type> argumentTypes, TupleInfo.Type intermediateType, AggregationFunction function)
    {
        this.id = id;
        this.name = name;
        this.returnType = returnType;
        this.argumentTypes = argumentTypes;
        this.intermediateType = intermediateType;
        this.aggregationFunction = function;
        this.isAggregate = true;
        this.scalarFunction = null;
        this.deterministic = true;
    }

    public FunctionInfo(int id, QualifiedName name, Type returnType, List<Type> argumentTypes, MethodHandle function, boolean deterministic)
    {
        this.id = id;
        this.name = name;
        this.returnType = returnType;
        this.argumentTypes = argumentTypes;
        this.deterministic = deterministic;

        this.isAggregate = false;
        this.intermediateType = null;
        this.aggregationFunction = null;

        this.scalarFunction = checkNotNull(function, "function is null");
    }

    public FunctionHandle getHandle()
    {
        return new FunctionHandle(id, name.toString());
    }

    public QualifiedName getName()
    {
        return name;
    }

    public boolean isAggregate()
    {
        return isAggregate;
    }

    public TupleInfo.Type getReturnType()
    {
        return returnType;
    }

    public List<TupleInfo.Type> getArgumentTypes()
    {
        return argumentTypes;
    }

    public TupleInfo.Type getIntermediateType()
    {
        return intermediateType;
    }

    public AggregationFunctionDefinition bind(List<Input> inputs)
    {
        checkState(isAggregate, "function is not an aggregate");
        if (inputs.isEmpty()) {
            return aggregation(aggregationFunction, null);
        }
        else {
            Preconditions.checkArgument(inputs.size() == 1, "expected at most one input");
            return aggregation(aggregationFunction, Iterables.getOnlyElement(inputs));
        }
    }

    public MethodHandle getScalarFunction()
    {
        checkState(scalarFunction != null, "not a scalar function");
        return scalarFunction;
    }

    public boolean isDeterministic()
    {
        return deterministic;
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

        FunctionInfo that = (FunctionInfo) o;

        if (isAggregate != that.isAggregate) {
            return false;
        }
        if (!argumentTypes.equals(that.argumentTypes)) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        if (returnType != that.returnType) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + (isAggregate ? 1 : 0);
        result = 31 * result + returnType.hashCode();
        result = 31 * result + argumentTypes.hashCode();
        return result;
    }

    @Override
    public int compareTo(FunctionInfo o)
    {
        return ComparisonChain.start()
                .compareTrueFirst(isAggregate, o.isAggregate)
                .compare(name.toString(), o.name.toString())
                .compare(argumentTypes, o.argumentTypes, Ordering.<Type>natural().lexicographical())
                .compare(returnType, o.returnType)
                .result();
    }

    public static Function<FunctionInfo, QualifiedName> nameGetter()
    {
        return new Function<FunctionInfo, QualifiedName>()
        {
            @Override
            public QualifiedName apply(FunctionInfo input)
            {
                return input.getName();
            }
        };
    }

    public static Function<FunctionInfo, FunctionHandle> handleGetter()
    {
        return new Function<FunctionInfo, FunctionHandle>()
        {
            @Override
            public FunctionHandle apply(FunctionInfo input)
            {
                return input.getHandle();
            }
        };
    }
}
