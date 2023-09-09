/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.presto.sql.planner;

import com.facebook.presto.sql.planner.plan.OutputNode;
import com.facebook.presto.tuple.TupleInfo;
import com.facebook.presto.tuple.TupleInfo.Type;
import com.facebook.presto.util.IterableTransformer;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class StageExecutionPlan
{
    private final PlanFragment fragment;
    private final List<Partition> partitions;
    private final List<StageExecutionPlan> subStages;
    private final List<TupleInfo> tupleInfos;
    private final Optional<List<String>> fieldNames;

    public StageExecutionPlan(PlanFragment fragment, List<Partition> partitions, List<StageExecutionPlan> subStages)
    {
        this.fragment = checkNotNull(fragment, "fragment is null");
        this.partitions = ImmutableList.copyOf(checkNotNull(partitions, "partitions is null"));
        this.subStages = ImmutableList.copyOf(checkNotNull(subStages, "dependencies is null"));

        tupleInfos = ImmutableList.copyOf(IterableTransformer.on(fragment.getRoot().getOutputSymbols())
                .transform(Functions.forMap(fragment.getSymbols()))
                .transform(com.facebook.presto.sql.analyzer.Type.toRaw())
                .transform(new Function<Type, TupleInfo>()
                {
                    @Override
                    public TupleInfo apply(Type input)
                    {
                        return new TupleInfo(input);
                    }
                })
                .list());
        fieldNames = (fragment.getRoot() instanceof OutputNode) ?
                Optional.<List<String>>of(ImmutableList.copyOf(((OutputNode) fragment.getRoot()).getColumnNames())) :
                Optional.<List<String>>absent();
    }

    public List<TupleInfo> getTupleInfos()
    {
        return tupleInfos;
    }

    public List<String> getFieldNames()
    {
        checkState(fieldNames.isPresent(), "cannot get field names from non-output stage");
        return fieldNames.get();
    }

    public PlanFragment getFragment()
    {
        return fragment;
    }

    public List<Partition> getPartitions()
    {
        return partitions;
    }

    public List<StageExecutionPlan> getSubStages()
    {
        return subStages;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("fragment", fragment)
                .add("partitions", partitions)
                .add("subStages", subStages)
                .toString();
    }
}
