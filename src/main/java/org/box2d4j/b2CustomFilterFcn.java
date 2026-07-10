package org.box2d4j;

@FunctionalInterface
public interface b2CustomFilterFcn {
    boolean invoke(b2ShapeId shapeIdA, b2ShapeId shapeIdB, Object context);
}
