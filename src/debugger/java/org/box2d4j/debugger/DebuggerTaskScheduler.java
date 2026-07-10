package org.box2d4j.debugger;

import org.box2d4j.B2TaskScheduler;
import org.box2d4j.b2TaskCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** ExecutorService integration owned by the graphical sample viewer, not by the Box2D core. */
final class DebuggerTaskScheduler implements B2TaskScheduler, AutoCloseable {
    private final ExecutorService executor;
    private final int workerCount;
    private final ArrayBlockingQueue<Integer> availableWorkerIndices;

    DebuggerTaskScheduler(int workerCount) {
        if (workerCount < 1 || workerCount > 64) {
            throw new IllegalArgumentException("workerCount must be in [1, 64]");
        }
        this.workerCount = workerCount;
        this.availableWorkerIndices = new ArrayBlockingQueue<>(workerCount);
        for (int workerIndex = 0; workerIndex < workerCount; ++workerIndex) {
            availableWorkerIndices.add(workerIndex);
        }

        AtomicInteger threadIndex = new AtomicInteger();
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "box2d4j-debug-worker-" + threadIndex.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
        executor = Executors.newFixedThreadPool(workerCount, threadFactory);
    }

    static int defaultWorkerCount() {
        return Math.max(1, Math.min(8, Runtime.getRuntime().availableProcessors() / 2));
    }

    @Override
    public int workerCount() {
        return workerCount;
    }

    @Override
    public Object enqueue(b2TaskCallback task, int itemCount, int minRange, Object taskContext) {
        if (itemCount <= 0) {
            return null;
        }
        int range = Math.max(1, minRange);
        int taskCount = Math.min(workerCount, Math.max(1, itemCount / range));
        if (workerCount == 1) {
            task.invoke(0, itemCount, 0, taskContext);
            return null;
        }

        List<Future<?>> futures = new ArrayList<>(taskCount);
        int baseCount = itemCount / taskCount;
        int remainder = itemCount - baseCount * taskCount;
        int startIndex = 0;
        for (int taskIndex = 0; taskIndex < taskCount; ++taskIndex) {
            int count = baseCount + (taskIndex < remainder ? 1 : 0);
            int rangeStart = startIndex;
            int rangeEnd = rangeStart + count;
            futures.add(executor.submit(() -> runRange(task, rangeStart, rangeEnd, taskContext)));
            startIndex = rangeEnd;
        }
        return futures;
    }

    private void runRange(b2TaskCallback task, int startIndex, int endIndex, Object taskContext) {
        int workerIndex;
        try {
            workerIndex = availableWorkerIndices.take();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while acquiring a Box2D worker index", exception);
        }
        try {
            task.invoke(startIndex, endIndex, workerIndex, taskContext);
        } finally {
            availableWorkerIndices.add(workerIndex);
        }
    }

    @Override
    public void finish(Object taskHandle) {
        if (!(taskHandle instanceof List<?>)) {
            throw new IllegalArgumentException("Unknown Box2D task handle");
        }
        Throwable failure = null;
        boolean interrupted = false;
        for (Object item : (List<?>) taskHandle) {
            Future<?> future = (Future<?>) item;
            boolean complete = false;
            while (!complete) {
                try {
                    future.get();
                    complete = true;
                } catch (InterruptedException exception) {
                    interrupted = true;
                    if (failure == null) {
                        failure = exception;
                    }
                } catch (ExecutionException exception) {
                    if (failure == null) {
                        failure = exception.getCause();
                    }
                    complete = true;
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        if (failure instanceof RuntimeException) {
            throw (RuntimeException) failure;
        }
        if (failure instanceof Error) {
            throw (Error) failure;
        }
        if (failure != null) {
            throw new IllegalStateException("Box2D task failed", failure);
        }
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
