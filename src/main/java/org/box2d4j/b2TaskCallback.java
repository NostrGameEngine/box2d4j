package org.box2d4j;

/** A parallel-for range produced by Box2D's task system. */
@FunctionalInterface
public interface b2TaskCallback {
    /**
     * Executes a half-open item range using an exclusive worker slot.
     *
     * @param startIndex first item index, inclusive
     * @param endIndex final item index, exclusive
     * @param workerIndex exclusive worker slot in the configured world range
     * @param taskContext context supplied by Box2D when the task was enqueued
     */
    void invoke(int startIndex, int endIndex, int workerIndex, Object taskContext);
}
