package com.facebook.presto.sql.planner.plan;

import com.google.common.base.Function;

public final class PlanRewriter<C>
{
    private final PlanNodeRewriter<C> nodeRewriter;
    private final PlanVisitor<Context<C>, PlanNode> visitor;

    public static <C, T extends PlanNode> T rewriteWith(PlanNodeRewriter<C> rewriter, T node)
    {
        return rewriteWith(rewriter, node, null);
    }

    public static <C, T extends PlanNode> T rewriteWith(PlanNodeRewriter<C> rewriter, T node, C context)
    {
        return new PlanRewriter<>(rewriter).rewrite(node, context);
    }

    public PlanRewriter(PlanNodeRewriter<C> nodeRewriter)
    {
        this.nodeRewriter = nodeRewriter;
        this.visitor = new RewritingVisitor();
    }

    public <T extends PlanNode> T rewrite(T node, C context)
    {
        return (T) node.accept(visitor, new Context<>(context, false));
    }

    /**
     * Invoke the default rewrite logic explicitly. Specifically, it skips the invocation of the node rewriter for the provided node.
     */
    public <T extends PlanNode> T defaultRewrite(T node, C context)
    {
        return (T) node.accept(visitor, new Context<>(context, true));
    }

    public static <C, T extends PlanNode> Function<PlanNode, T> rewriteFunction(final PlanNodeRewriter<C> rewriter)
    {
        return new Function<PlanNode, T>()
        {
            @Override
            public T apply(PlanNode node)
            {
                return (T) rewriteWith(rewriter, node);
            }
        };
    }

    private class RewritingVisitor
            extends PlanVisitor<PlanRewriter.Context<C>, PlanNode>
    {
        @Override
        protected PlanNode visitPlan(PlanNode node, Context<C> context)
        {
            if (!context.isDefaultRewrite()) {
                PlanNode result = nodeRewriter.rewriteNode(node, context.get(), PlanRewriter.this);
                if (result != null) {
                    return result;
                }
            }

            throw new UnsupportedOperationException("not yet implemented: " + getClass().getSimpleName() + " for " + node.getClass().getName());
        }

        @Override
        public PlanNode visitExchange(ExchangeNode node, Context<C> context)
        {
            if (!context.isDefaultRewrite()) {
                PlanNode result = nodeRewriter.rewriteExchange(node, context.get(), PlanRewriter.this);
                if (result != null) {
                    return result;
                }
            }

            return node;
        }

        @Override
        public PlanNode visitAggregation(AggregationNode node, Context<C> context)
        {
            if (!context.isDefaultRewrite()) {
                PlanNode result = nodeRewriter.rewriteAggregation(node, context.get(), PlanRewriter.this);
                if (result != null) {
                    return result;
                }
            }

            PlanNode source = rewrite(node.getSource(), context.get());

            if (source != node.getSource()) {
                return new AggregationNode(source, node.getGroupBy(), node.getAggregations(), node.getFunctions(), node.getStep());
            }

            return node;
        }

        @Override
        public PlanNode visitFilter(FilterNode node, Context<C> context)
        {
            if (!context.isDefaultRewrite()) {
                PlanNode result = nodeRewriter.rewriteFilter(node, context.get(), PlanRewriter.this);
                if (result != null) {
                    return result;
                }
            }

            PlanNode source = rewrite(node.getSource(), context.get());

            if (source != node.getSource()) {
                return new FilterNode(source, node.getPredicate());
            }

            return node;
        }

        @Override
        public PlanNode visitProject(ProjectNode node, Context<C> context)
        {
            if (!context.isDefaultRewrite()) {
                PlanNode result = nodeRewriter.rewriteProject(node, context.get(), PlanRewriter.this);
                if (result != null) {
                    return result;
                }
            }

            PlanNode source = rewrite(node.getSource(), context.get());

            if (source != node.getSource()) {
                return new ProjectNode(source, node.getOutputMap());
            }

            return node;
        }

        @Override
        public PlanNode visitTopN(TopNNode node, Context<C> context)
        {
            if (!context.isDefaultRewrite()) {
                PlanNode result = nodeRewriter.rewriteTopN(node, context.get(), PlanRewriter.this);
                if (result != null) {
                    return result;
                }
            }

            PlanNode source = rewrite(node.getSource(), context.get());

            if (source != node.getSource()) {
                return new TopNNode(source, node.getCount(), node.getOrderBy(), node.getOrderings());
            }

            return node;
        }

        @Override
        public PlanNode visitOutput(OutputNode node, Context<C> context)
        {
            if (!context.isDefaultRewrite()) {
                PlanNode result = nodeRewriter.rewriteOutput(node, context.get(), PlanRewriter.this);
                if (result != null) {
                    return result;
                }
            }

            PlanNode source = rewrite(node.getSource(), context.get());

            if (source != node.getSource()) {
                return new OutputNode(source, node.getColumnNames(), node.getAssignments());
            }

            return node;
        }

        @Override
        public PlanNode visitLimit(LimitNode node, Context<C> context)
        {
            if (!context.isDefaultRewrite()) {
                PlanNode result = nodeRewriter.rewriteLimit(node, context.get(), PlanRewriter.this);
                if (result != null) {
                    return result;
                }
            }

            PlanNode source = rewrite(node.getSource(), context.get());

            if (source != node.getSource()) {
                return new LimitNode(source, node.getCount());
            }

            return node;
        }

        @Override
        public PlanNode visitTableScan(TableScanNode node, Context<C> context)
        {
            if (!context.isDefaultRewrite()) {
                PlanNode result = nodeRewriter.rewriteTableScan(node, context.get(), PlanRewriter.this);
                if (result != null) {
                    return result;
                }
            }

            return node;
        }

        @Override
        public PlanNode visitJoin(JoinNode node, Context<C> context)
        {
            if (!context.isDefaultRewrite()) {
                PlanNode result = nodeRewriter.rewriteJoin(node, context.get(), PlanRewriter.this);
                if (result != null) {
                    return result;
                }
            }

            PlanNode left = rewrite(node.getLeft(), context.get());
            PlanNode right = rewrite(node.getRight(), context.get());

            if (left != node.getLeft() || right != node.getRight()) {
                return new JoinNode(left, right, node.getCriteria());
            }

            return node;
        }

        @Override
        public PlanNode visitSort(SortNode node, Context<C> context)
        {
            if (!context.isDefaultRewrite()) {
                PlanNode result = nodeRewriter.rewriteSort(node, context.get(), PlanRewriter.this);
                if (result != null) {
                    return result;
                }
            }

            PlanNode source = rewrite(node.getSource(), context.get());

            if (source != node.getSource()) {
                return new SortNode(source, node.getOrderBy(), node.getOrderings());
            }

            return node;
        }
    }

    public static class Context<C>
    {
        private boolean defaultRewrite;
        private final C context;

        private Context(C context, boolean defaultRewrite)
        {
            this.context = context;
            this.defaultRewrite = defaultRewrite;
        }

        public C get()
        {
            return context;
        }

        public boolean isDefaultRewrite()
        {
            return defaultRewrite;
        }
    }
}
