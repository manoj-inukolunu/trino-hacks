package com.facebook.presto.concurrent;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import static org.testng.Assert.assertTrue;

public class TestFairBatchExecutor
{
    @Test
    public void testSanity()
            throws Exception
    {
        FairBatchExecutor executor = new FairBatchExecutor(1, new ThreadFactoryBuilder().setDaemon(true).build());

        // first, block the executor until we're ready
        final CountDownLatch readyToStart = new CountDownLatch(1);
        executor.processBatch(ImmutableList.of(new Callable<Object>()
        {
            @Override
            public Object call()
                    throws Exception
            {
                readyToStart.await();
                return null;
            }
        }));

        BlockingQueue<Integer> executions = new LinkedBlockingDeque<>();
        List<Future<?>> futures = new ArrayList<>();

        // the "meetingPoint" is so that we can schedule additional task at a predictable point in the execution
        MeetingPoint meetingPoint = new MeetingPoint();
        futures.addAll(executor.processBatch(ImmutableList.of(
                newTask(1, executions),
                newTask(2, executions),
                newTask(3, executions),
                newTask(4, executions),
                newTask(5, meetingPoint, executions),
                newTask(6, executions),
                newTask(7, executions),
                newTask(8, executions),
                newTask(9, executions))));

        futures.addAll(executor.processBatch(ImmutableList.of(
                newTask(2, executions),
                newTask(3, executions),
                newTask(4, executions),
                newTask(5, meetingPoint, executions),
                newTask(6, executions))));

        futures.addAll(executor.processBatch(ImmutableList.of(
                newTask(3, executions),
                newTask(4, executions),
                newTask(5, meetingPoint, executions),
                newTask(6, executions),
                newTask(7, executions),
                newTask(8, executions))));

        readyToStart.countDown();

        meetingPoint.waitForArrival();

        futures.addAll(executor.processBatch(ImmutableList.of(
                newTask(5, executions),
                newTask(6, executions),
                newTask(7, executions),
                newTask(8, executions),
                newTask(9, executions))));

        meetingPoint.notifyAdvance();

        // wait for all tasks to complete
        for (Future<?> future : futures) {
            future.get();
        }

        assertTrue(Ordering.<Integer>natural().isOrdered(executions), executions.toString());

        executor.shutdown();
    }

    private static class MeetingPoint
    {
        private final CountDownLatch arrived = new CountDownLatch(1);
        private final CountDownLatch readyForNextPhase = new CountDownLatch(1);

        public void waitForNextPhase()
                throws InterruptedException
        {
            arrived.countDown();
            readyForNextPhase.await();
        }

        public void waitForArrival()
                throws InterruptedException
        {
            arrived.await();
        }

        public void notifyAdvance()
        {
            readyForNextPhase.countDown();
        }
    }

    private static Callable<Void> newTask(int group, Queue<Integer> executions)
    {
        return newTask(group, null, executions);
    }

    private static Callable<Void> newTask(final int group, final MeetingPoint meetingPoint, final Queue<Integer> executions)
    {
        return new Callable<Void>()
        {
            @Override
            public Void call()
            {
                executions.add(group);

                if (meetingPoint != null) {
                    try {
                        meetingPoint.waitForNextPhase();
                    }
                    catch (Exception e) {
                        throw Throwables.propagate(e);
                    }
                }

                return null;
            }
        };
    }
}
