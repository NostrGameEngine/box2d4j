package org.box2d4j;

/** Waits for a task handle returned by {@link b2EnqueueTaskCallback}. */
@FunctionalInterface
public interface b2FinishTaskCallback {
    /** Waits until every range represented by the task handle has completed. */
    void invoke(Object userTask, Object userContext);
}
