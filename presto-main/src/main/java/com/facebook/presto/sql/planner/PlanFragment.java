package com.facebook.presto.sql.planner;

import com.facebook.presto.sql.analyzer.Symbol;
import com.facebook.presto.sql.analyzer.Type;
import com.facebook.presto.sql.planner.plan.PlanNode;
import com.facebook.presto.tuple.TupleInfo;
import com.facebook.presto.util.IterableTransformer;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Map;

@Immutable
public class PlanFragment
{
    private final int id;
    private final PlanNode root;
    private final boolean partitioned;
    private final Map<Symbol, Type> symbols;

    @JsonCreator
    public PlanFragment(@JsonProperty("id") int id, @JsonProperty("partitioned") boolean isPartitioned, @JsonProperty("symbols") Map<Symbol, Type> symbols, @JsonProperty("root") PlanNode root)
    {
        Preconditions.checkArgument(id >= 0, "id must be positive");
        Preconditions.checkNotNull(symbols, "symbols is null");
        Preconditions.checkNotNull(root, "root is null");

        this.id = id;
        this.root = root;
        partitioned = isPartitioned;
        this.symbols = symbols;
    }

    @JsonProperty("id")
    public int getId()
    {
        return id;
    }

    @JsonProperty("partitioned")
    public boolean isPartitioned()
    {
        return partitioned;
    }

    @JsonProperty("root")
    public PlanNode getRoot()
    {
        return root;
    }

    @JsonProperty("symbols")
    public Map<Symbol, Type> getSymbols()
    {
        return symbols;
    }

    public List<TupleInfo> getTupleInfos()
    {
        return ImmutableList.copyOf(IterableTransformer.on(getRoot().getOutputSymbols())
                .transform(Functions.forMap(getSymbols()))
                .transform(com.facebook.presto.sql.analyzer.Type.toRaw())
                .transform(new Function<TupleInfo.Type, TupleInfo>()
                {
                    @Override
                    public TupleInfo apply(TupleInfo.Type input)
                    {
                        return new TupleInfo(input);
                    }
                })
                .list());
    }

    public List<PlanNode> getSources()
    {
        ImmutableList.Builder<PlanNode> sources = ImmutableList.builder();
        findSources(root, sources);
        return sources.build();
    }

    private void findSources(PlanNode node, ImmutableList.Builder<PlanNode> builder)
    {
        for (PlanNode source : node.getSources()) {
            findSources(source, builder);
        }

        if (node.getSources().isEmpty()) {
            builder.add(node);
        }
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("partitioned", partitioned)
                .toString();
    }

    public static Function<PlanFragment, Integer> idGetter()
    {
        return new Function<PlanFragment, Integer>()
        {
            @Override
            public Integer apply(PlanFragment input)
            {
                return input.getId();
            }
        };
    }
}
