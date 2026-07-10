package org.box2d4j;

public class b2ContactHitEvent {
    public b2ShapeId shapeIdA = new b2ShapeId();
    public b2ShapeId shapeIdB = new b2ShapeId();
    public b2Vec2 point = new b2Vec2();
    public b2Vec2 normal = new b2Vec2();
    public float approachSpeed;
}
