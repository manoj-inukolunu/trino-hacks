package com.facebook.presto.benchmark;

import com.facebook.presto.block.BlockCursor;
import com.facebook.presto.operator.Operator;
import com.facebook.presto.operator.OperatorStats;
import com.facebook.presto.operator.Page;
import com.facebook.presto.operator.PageIterator;
import com.facebook.presto.tpch.TpchBlocksProvider;

public class SqlDoubleSumAggregationBenchmark
        extends AbstractSqlBenchmark
{
    public SqlDoubleSumAggregationBenchmark()
    {
        super("sql_double_sum_agg", 10, 100, "select sum(totalprice) from orders");
    }

    @Override
    protected long execute(TpchBlocksProvider blocksProvider)
    {
        Operator operator = createBenchmarkedOperator(blocksProvider);

        long outputRows = 0;
        PageIterator iterator = operator.iterator(new OperatorStats());
        while (iterator.hasNext()) {
            Page page = iterator.next();
            BlockCursor cursor = page.getBlock(0).cursor();
            while (cursor.advanceNextPosition()) {
                outputRows++;
            }
        }
        return outputRows;
    }

    public static void main(String[] args)
    {
        new SqlDoubleSumAggregationBenchmark().runBenchmark(new SimpleLineBenchmarkResultWriter(System.out));
    }
}
