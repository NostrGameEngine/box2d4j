package org.box2d4j;

@FunctionalInterface
public interface b2PlaneResultFcn {
    boolean invoke(b2ShapeId shapeId, b2PlaneResult plane);
}
