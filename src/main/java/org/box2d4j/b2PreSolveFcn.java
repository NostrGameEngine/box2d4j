package org.box2d4j;

@FunctionalInterface
public interface b2PreSolveFcn {
    boolean invoke(b2ShapeId shapeIdA, b2ShapeId shapeIdB, b2Manifold manifold, Object context);
}
