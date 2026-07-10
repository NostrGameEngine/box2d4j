package org.box2d4j;

@FunctionalInterface
public interface b2CastResultFcn {
    float invoke(b2ShapeId shapeId, b2Vec2 point, b2Vec2 normal, float fraction);
}
