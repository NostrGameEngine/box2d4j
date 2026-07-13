package org.box2d4j;

/** Enqueues a Box2D parallel-for and returns an optional task handle. */
@FunctionalInterface
public interface b2EnqueueTaskCallback {
    /**
     * Enqueues a parallel-for task.
     *
     * @param task callback that executes one half-open item range
     * @param itemCount total number of items
     * @param minRange minimum preferred items per task range
     * @param taskContext context forwarded to {@code task}
     * @param userContext scheduler context from {@link b2WorldDef#userTaskContext}
     * @return handle to await, or {@code null} when all work completed before returning
     */
    Object invoke(b2TaskCallback task, int itemCount, int minRange, Object taskContext, Object userContext);
}
