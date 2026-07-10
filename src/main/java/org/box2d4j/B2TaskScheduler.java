package org.box2d4j;

/**
 * Java-friendly owner-neutral adapter for Box2D's user-provided task system.
 * Implementations must keep worker indices in {@code [0, workerCount())} exclusive while callbacks run.
 */
public interface B2TaskScheduler {
    int workerCount();

    Object enqueue(b2TaskCallback task, int itemCount, int minRange, Object taskContext);

    void finish(Object taskHandle);
}
