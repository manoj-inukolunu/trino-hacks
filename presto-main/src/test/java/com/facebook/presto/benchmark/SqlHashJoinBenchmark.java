package com.facebook.presto.benchmark;

import com.facebook.presto.block.BlockCursor;
import com.facebook.presto.operator.Operator;
import com.facebook.presto.operator.OperatorStats;
import com.facebook.presto.operator.Page;
import com.facebook.presto.operator.PageIterator;
import com.facebook.presto.tpch.TpchBlocksProvider;

public class SqlHashJoinBenchmark
        extends AbstractSqlBenchmark
{
    public SqlHashJoinBenchmark()
    {
        super("sql_hash_join", 4, 5, "select lineitem.orderkey, lineitem.quantity, orders.totalprice, orders.orderkey from lineitem join orders using (orderkey)");
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
        new SqlHashJoinBenchmark().runBenchmark(new SimpleLineBenchmarkResultWriter(System.out));
    }
}
