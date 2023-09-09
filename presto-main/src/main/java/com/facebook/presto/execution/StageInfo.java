/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.presto.execution;

import com.facebook.presto.sql.planner.PlanFragment;
import com.facebook.presto.tuple.TupleInfo;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.net.URI;
import java.util.List;

@Immutable
public class StageInfo
{
    private final String queryId;
    private final String stageId;
    private final StageState state;
    private final URI self;
    private final PlanFragment plan;
    private final List<TupleInfo> tupleInfos;
    private final List<TaskInfo> tasks;
    private final List<StageInfo> subStages;
    private final List<FailureInfo> failures;

    @JsonCreator
    public StageInfo(@JsonProperty("queryId") String queryId,
            @JsonProperty("stageId") String stageId,
            @JsonProperty("state") StageState state,
            @JsonProperty("self") URI self,
            @JsonProperty("plan") @Nullable PlanFragment plan,
            @JsonProperty("tupleInfos") List<TupleInfo> tupleInfos,
            @JsonProperty("tasks") List<TaskInfo> tasks,
            @JsonProperty("subStages") List<StageInfo> subStages,
            @JsonProperty("failures") List<FailureInfo> failures)
    {
        Preconditions.checkNotNull(queryId, "queryId is null");
        Preconditions.checkNotNull(stageId, "stageId is null");
        Preconditions.checkNotNull(state, "state is null");
        Preconditions.checkNotNull(self, "self is null");
        Preconditions.checkNotNull(tasks, "tasks is null");
        Preconditions.checkNotNull(subStages, "subStages is null");
        Preconditions.checkNotNull(failures, "failures is null");

        this.queryId = queryId;
        this.stageId = stageId;
        this.state = state;
        this.self = self;
        this.plan = plan;
        this.tupleInfos = tupleInfos;
        this.tasks = ImmutableList.copyOf(tasks);
        this.subStages = subStages;
        this.failures = failures;
    }

    @JsonProperty
    public String getQueryId()
    {
        return queryId;
    }

    @JsonProperty
    public String getStageId()
    {
        return stageId;
    }

    @JsonProperty
    public StageState getState()
    {
        return state;
    }

    @JsonProperty
    public URI getSelf()
    {
        return self;
    }

    @JsonProperty
    @Nullable
    public PlanFragment getPlan()
    {
        return plan;
    }

    @JsonProperty
    public List<TupleInfo> getTupleInfos()
    {
        return tupleInfos;
    }

    @JsonProperty
    public List<TaskInfo> getTasks()
    {
        return tasks;
    }

    @JsonProperty
    public List<StageInfo> getSubStages()
    {
        return subStages;
    }

    @JsonProperty
    public List<FailureInfo> getFailures()
    {
        return failures;
    }

    public static ExecutionStats globalExecutionStats(@Nullable StageInfo stageInfo)
    {
        ExecutionStats executionStats = new ExecutionStats();
        sumStats(stageInfo, executionStats, false);
        return executionStats;
    }

    public static ExecutionStats leafExecutionStats(@Nullable StageInfo stageInfo)
    {
        ExecutionStats executionStats = new ExecutionStats();
        sumStats(stageInfo, executionStats, true);
        return executionStats;
    }

    public static ExecutionStats stageOnlyExecutionStats(@Nullable StageInfo stageInfo)
    {
        ExecutionStats executionStats = new ExecutionStats();
        sumTaskStats(stageInfo, executionStats);
        return executionStats;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("stageId", stageId)
                .add("state", state)
                .toString();
    }


    public static List<StageInfo> getAllStages(StageInfo stageInfo)
    {
        ImmutableList.Builder<StageInfo> collector = ImmutableList.builder();
        addAllStages(stageInfo, collector);
        return collector.build();
    }

    private static void addAllStages(StageInfo stageInfo, ImmutableList.Builder<StageInfo> collector)
    {
        collector.add(stageInfo);
        for (StageInfo subStage : stageInfo.getSubStages()) {
            addAllStages(subStage, collector);
        }
    }

    public static Function<StageInfo, StageState> stageStateGetter()
    {
        return new Function<StageInfo, StageState>()
        {
            @Override
            public StageState apply(StageInfo stageInfo)
            {
                return stageInfo.getState();
            }
        };
    }

    private static void sumStats(@Nullable StageInfo stageInfo, ExecutionStats executionStats, boolean sumLeafOnly)
    {
        if (stageInfo == null) {
            return;
        }
        if (!sumLeafOnly || stageInfo.getSubStages().isEmpty()) {
            sumTaskStats(stageInfo, executionStats);
        }
        for (StageInfo subStage : stageInfo.getSubStages()) {
            sumStats(subStage, executionStats, sumLeafOnly);
        }
    }

    private static void sumTaskStats(@Nullable StageInfo stageInfo, ExecutionStats executionStats)
    {
        if (stageInfo == null) {
            return;
        }
        for (TaskInfo task : stageInfo.getTasks()) {
            executionStats.add(task.getStats());
        }
    }
}
