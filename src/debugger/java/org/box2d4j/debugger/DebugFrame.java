package org.box2d4j.debugger;

final class DebugFrame {
    final WorldDrawBatch batch = new WorldDrawBatch();
    long version;
    int stepCount;
    int bodyCount;
    int shapeCount;
    int contactCount;
    int jointCount;
    int awakeBodyCount;
}
