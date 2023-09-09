/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.presto.operator.aggregation;

import com.facebook.presto.tuple.TupleInfo;

public interface AggregationFunction
{
    TupleInfo getFinalTupleInfo();

    TupleInfo getIntermediateTupleInfo();
}
