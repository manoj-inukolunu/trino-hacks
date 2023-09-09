package com.facebook.presto.operator;

import com.facebook.presto.block.Block;
import com.facebook.presto.block.BlockBuilder;
import com.facebook.presto.block.BlockCursor;
import com.facebook.presto.operator.aggregation.AggregationFunction;
import com.facebook.presto.operator.aggregation.FixedWidthAggregationFunction;
import com.facebook.presto.operator.aggregation.VariableWidthAggregationFunction;
import io.airlift.slice.Slice;
import com.facebook.presto.sql.planner.plan.AggregationNode.Step;
import com.facebook.presto.tuple.TupleInfo;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.airlift.slice.Slices;

import java.util.List;


/**
 * Group input data and produce a single block for each sequence of identical values.
 */
public class AggregationOperator
        implements Operator
{
    private final Operator source;
    private final Step step;
    private final List<AggregationFunctionDefinition> functionDefinitions;
    private final List<TupleInfo> tupleInfos;

    public AggregationOperator(Operator source,
            Step step,
            List<AggregationFunctionDefinition> functionDefinitions)
    {
        Preconditions.checkNotNull(source, "source is null");
        Preconditions.checkNotNull(step, "step is null");
        Preconditions.checkNotNull(functionDefinitions, "functionDefinitions is null");

        this.source = source;
        this.step = step;
        this.functionDefinitions = ImmutableList.copyOf(functionDefinitions);

        ImmutableList.Builder<TupleInfo> tupleInfos = ImmutableList.builder();
        for (AggregationFunctionDefinition functionDefinition : functionDefinitions) {
            if (step != Step.PARTIAL) {
                tupleInfos.add(functionDefinition.getFunction().getFinalTupleInfo());
            }
            else {
                tupleInfos.add(functionDefinition.getFunction().getIntermediateTupleInfo());
            }
        }
        this.tupleInfos = tupleInfos.build();
    }

    @Override
    public int getChannelCount()
    {
        return tupleInfos.size();
    }

    @Override
    public List<TupleInfo> getTupleInfos()
    {
        return tupleInfos;
    }

    @Override
    public PageIterator iterator(OperatorStats operatorStats)
    {
        return new AggregationPageIterator(tupleInfos, source, operatorStats, step, functionDefinitions);
    }

    private static class AggregationPageIterator
            extends AbstractPageIterator
    {
        private final PageIterator source;
        private final List<Aggregator> aggregates;
        private boolean done;

        private AggregationPageIterator(List<TupleInfo> tupleInfos,
                Operator source,
                OperatorStats operatorStats,
                Step step,
                List<AggregationFunctionDefinition> functionDefinitions)
        {
            super(tupleInfos);

            // wrapper each function with an aggregator
            ImmutableList.Builder<Aggregator> builder = ImmutableList.builder();
            for (AggregationFunctionDefinition functionDefinition : functionDefinitions) {
                builder.add(createAggregator(functionDefinition, step));
            }
            aggregates = builder.build();

            this.source = source.iterator(operatorStats);
        }

        @Override
        protected Page computeNext()
        {
            if (done) {
                return endOfData();
            }
            while (source.hasNext()) {
                Page page = source.next();

                // process the row
                for (Aggregator aggregate : aggregates) {
                    aggregate.addValue(page);
                }
            }

            // project results into output blocks
            Block[] blocks = new Block[aggregates.size()];
            for (int i = 0; i < blocks.length; i++) {
                blocks[i] = aggregates.get(i).getResult();
            }
            done = true;
            return new Page(blocks);
        }

        @Override
        protected void doClose()
        {
            source.close();
        }
    }

    @VisibleForTesting
    public static Aggregator createAggregator(AggregationFunctionDefinition functionDefinition, Step step)
    {
        AggregationFunction function = functionDefinition.getFunction();
        if (function instanceof VariableWidthAggregationFunction) {
            return new VariableWidthAggregator<>((VariableWidthAggregationFunction<Object>) functionDefinition.getFunction(), functionDefinition.getInput(), step);
        }
        else {
            return new FixedWidthAggregator((FixedWidthAggregationFunction) functionDefinition.getFunction(), functionDefinition.getInput(), step);
        }
    }

    @VisibleForTesting
    public interface Aggregator
    {
        void addValue(Page page);

        void addValue(BlockCursor... cursors);

        Block getResult();
    }

    private static class FixedWidthAggregator
            implements Aggregator
    {
        private final FixedWidthAggregationFunction function;
        private final Input input;
        private final Step step;
        private final Slice intermediateValue;

        private FixedWidthAggregator(FixedWidthAggregationFunction function, Input input, Step step)
        {
            Preconditions.checkNotNull(function, "function is null");
            Preconditions.checkNotNull(step, "step is null");
            this.function = function;
            this.input = input;
            this.step = step;
            this.intermediateValue = Slices.allocate(function.getFixedSize());
            function.initialize(intermediateValue, 0);
        }

        @Override
        public void addValue(BlockCursor... cursors)
        {
            BlockCursor cursor = cursors[input.getChannel()];

            // if this is a final aggregation, the input is an intermediate value
            if (step == Step.FINAL) {
                function.addIntermediate(cursor, input.getField(), intermediateValue, 0);
            }
            else {
                function.addInput(cursor, input.getField(), intermediateValue, 0);
            }
        }

        @Override
        public void addValue(Page page)
        {
            // if this is a final aggregation, the input is an intermediate value
            if (step == Step.FINAL) {
                BlockCursor cursor = page.getBlock(input.getChannel()).cursor();
                while (cursor.advanceNextPosition()) {
                    function.addIntermediate(cursor, input.getField(), intermediateValue, 0);
                }
            }
            else {
                Block block;
                int field = -1;
                if (input != null) {
                    block = page.getBlock(input.getChannel());
                    field = input.getField();
                }
                else {
                    block = null;
                }
                function.addInput(page.getPositionCount(), block, field, intermediateValue, 0);
            }
        }

        @Override
        public Block getResult()
        {
            // if this is a partial, the output is an intermediate value
            if (step == Step.PARTIAL) {
                BlockBuilder output = new BlockBuilder(function.getIntermediateTupleInfo());
                function.evaluateIntermediate(intermediateValue, 0, output);
                return output.build();
            }
            else {
                BlockBuilder output = new BlockBuilder(function.getFinalTupleInfo());
                function.evaluateFinal(intermediateValue, 0, output);
                return output.build();
            }
        }
    }

    private static class VariableWidthAggregator<T>
            implements Aggregator
    {
        private final VariableWidthAggregationFunction<T> function;
        private final Input input;
        private final Step step;
        private T intermediateValue;

        private VariableWidthAggregator(VariableWidthAggregationFunction<T> function, Input input, Step step)
        {
            Preconditions.checkNotNull(function, "function is null");
            Preconditions.checkNotNull(step, "step is null");
            this.function = function;
            this.input = input;
            this.step = step;
            this.intermediateValue = function.initialize();
        }

        @Override
        public void addValue(Page page)
        {
            // if this is a final aggregation, the input is an intermediate value
            if (step == Step.FINAL) {
                BlockCursor cursor = page.getBlock(input.getChannel()).cursor();
                while (cursor.advanceNextPosition()) {
                    intermediateValue = function.addIntermediate(cursor, 0, intermediateValue);
                }
            }
            else {
                Block block;
                int field;
                if (input != null) {
                    block = page.getBlock(input.getChannel());
                    field = input.getField();
                }
                else {
                    block = null;
                    field = -1;
                }
                intermediateValue = function.addInput(page.getPositionCount(), block, field, intermediateValue);
            }
        }

        @Override
        public void addValue(BlockCursor... cursors)
        {
            BlockCursor cursor = cursors[input.getChannel()];

            // if this is a final aggregation, the input is an intermediate value
            if (step == Step.FINAL) {
                intermediateValue = function.addIntermediate(cursor, input.getField(), intermediateValue);
            }
            else {
                intermediateValue = function.addInput(cursor, input.getField(), intermediateValue);
            }
        }


        @Override
        public Block getResult()
        {
            // if this is a partial, the output is an intermediate value
            if (step == Step.PARTIAL) {
                BlockBuilder output = new BlockBuilder(function.getIntermediateTupleInfo());
                function.evaluateIntermediate(intermediateValue, output);
                return output.build();
            }
            else {
                BlockBuilder output = new BlockBuilder(function.getFinalTupleInfo());
                function.evaluateFinal(intermediateValue, output);
                return output.build();
            }
        }
    }
}
