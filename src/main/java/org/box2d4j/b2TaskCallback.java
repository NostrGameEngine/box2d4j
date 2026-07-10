package org.box2d4j;

/** A parallel-for range produced by Box2D's task system. */
@FunctionalInterface
public interface b2TaskCallback {
    /** Executes the half-open item range using an exclusive worker slot in the configured world range. */
    void invoke(int startIndex, int endIndex, int workerIndex, Object taskContext);
}
