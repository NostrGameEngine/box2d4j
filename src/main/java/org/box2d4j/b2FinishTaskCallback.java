package org.box2d4j;

/** Waits for a task handle returned by {@link b2EnqueueTaskCallback}. */
@FunctionalInterface
public interface b2FinishTaskCallback {
    /**
     * Waits until every range represented by the task handle has completed.
     *
     * @param userTask handle returned by {@link b2EnqueueTaskCallback}
     * @param userContext scheduler context from {@link b2WorldDef#userTaskContext}
     */
    void invoke(Object userTask, Object userContext);
}
