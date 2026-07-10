package org.box2d4j;

/** Enqueues a Box2D parallel-for and returns an optional task handle. */
@FunctionalInterface
public interface b2EnqueueTaskCallback {
    /** Returns a handle to await, or null when the callback completed all work before returning. */
    Object invoke(b2TaskCallback task, int itemCount, int minRange, Object taskContext, Object userContext);
}
