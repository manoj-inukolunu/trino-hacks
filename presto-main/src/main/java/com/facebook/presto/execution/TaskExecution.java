/*
 * Copyright 2004-present Facebook. All Rights Reserved.
 */
package com.facebook.presto.execution;

import com.facebook.presto.operator.Page;
import io.airlift.units.Duration;

import java.util.List;

public interface TaskExecution
{
    String getTaskId();

    TaskInfo getTaskInfo();

    void run();

    void cancel();

    List<Page> getResults(String outputId, int maxPageCount, Duration maxWait)
            throws InterruptedException;

    void abortResults(String outputId);
}
