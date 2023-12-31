package com.facebook.presto.sql.planner;

import com.facebook.presto.sql.analyzer.Symbol;
import com.facebook.presto.sql.planner.plan.AggregationNode;
import com.facebook.presto.sql.planner.plan.ExchangeNode;
import com.facebook.presto.sql.planner.plan.FilterNode;
import com.facebook.presto.sql.planner.plan.JoinNode;
import com.facebook.presto.sql.planner.plan.LimitNode;
import com.facebook.presto.sql.planner.plan.OutputNode;
import com.facebook.presto.sql.planner.plan.PlanNode;
import com.facebook.presto.sql.planner.plan.PlanVisitor;
import com.facebook.presto.sql.planner.plan.ProjectNode;
import com.facebook.presto.sql.planner.plan.SinkNode;
import com.facebook.presto.sql.planner.plan.SortNode;
import com.facebook.presto.sql.planner.plan.TableScanNode;
import com.facebook.presto.sql.planner.plan.TopNNode;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Computes all symbols declared by a logical plan
 */
public class SymbolExtractor
{
    public static Set<Symbol> extract(PlanNode node)
    {
        ImmutableSet.Builder<Symbol> builder = ImmutableSet.builder();

        node.accept(new Visitor(builder), null);

        return builder.build();
    }

    private static class Visitor
            extends PlanVisitor<Void, Void>
    {
        private final ImmutableSet.Builder<Symbol> builder;

        public Visitor(ImmutableSet.Builder<Symbol> builder)
        {
            this.builder = builder;
        }

        @Override
        public Void visitExchange(ExchangeNode node, Void context)
        {
            builder.addAll(node.getOutputSymbols());

            return null;
        }

        @Override
        public Void visitAggregation(AggregationNode node, Void context)
        {
            // visit child
            node.getSource().accept(this, context);

            builder.addAll(node.getAggregations().keySet());

            return null;
        }

        @Override
        public Void visitFilter(FilterNode node, Void context)
        {
            // visit child
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitProject(ProjectNode node, Void context)
        {
            // visit child
            node.getSource().accept(this, context);

            builder.addAll(node.getOutputSymbols());

            return null;
        }

        @Override
        public Void visitTopN(TopNNode node, Void context)
        {
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitSort(SortNode node, Void context)
        {
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitOutput(OutputNode node, Void context)
        {
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitLimit(LimitNode node, Void context)
        {
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitTableScan(TableScanNode node, Void context)
        {
            builder.addAll(node.getAssignments().keySet());

            return null;
        }

        @Override
        public Void visitJoin(JoinNode node, Void context)
        {
            node.getLeft().accept(this, context);
            node.getRight().accept(this, context);

            return null;
        }

        @Override
        public Void visitSink(SinkNode node, Void context)
        {
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        protected Void visitPlan(PlanNode node, Void context)
        {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }
}
