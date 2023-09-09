package com.facebook.presto.sql.planner.plan;

import com.facebook.presto.sql.analyzer.Symbol;
import com.facebook.presto.sql.tree.SortItem;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import java.util.Map;

public class SortNode
        extends PlanNode
{
    private final PlanNode source;
    private final List<Symbol> orderBy;
    private final Map<Symbol, SortItem.Ordering> orderings;

    @JsonCreator
    public SortNode(@JsonProperty("source") PlanNode source,
            @JsonProperty("orderBy") List<Symbol> orderBy,
            @JsonProperty("orderings") Map<Symbol, SortItem.Ordering> orderings)
    {
        Preconditions.checkNotNull(source, "source is null");
        Preconditions.checkNotNull(orderBy, "orderBy is null");
        Preconditions.checkArgument(!orderBy.isEmpty(), "orderBy is empty");
        Preconditions.checkArgument(orderings.size() == orderBy.size(), "orderBy and orderings sizes don't match");

        this.source = source;
        this.orderBy = ImmutableList.copyOf(orderBy);
        this.orderings = ImmutableMap.copyOf(orderings);
    }

    @Override
    public List<PlanNode> getSources()
    {
        return ImmutableList.of(source);
    }

    @JsonProperty("source")
    public PlanNode getSource()
    {
        return source;
    }

    @Override
    public List<Symbol> getOutputSymbols()
    {
        return source.getOutputSymbols();
    }

    @JsonProperty("orderBy")
    public List<Symbol> getOrderBy()
    {
        return orderBy;
    }

    @JsonProperty("orderings")
    public Map<Symbol, SortItem.Ordering> getOrderings()
    {
        return orderings;
    }

    @Override
    public <C, R> R accept(PlanVisitor<C, R> visitor, C context)
    {
        return visitor.visitSort(this, context);
    }
}
