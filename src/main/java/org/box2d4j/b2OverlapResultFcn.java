package org.box2d4j;

@FunctionalInterface
public interface b2OverlapResultFcn {
    boolean invoke(b2ShapeId shapeId);
}
