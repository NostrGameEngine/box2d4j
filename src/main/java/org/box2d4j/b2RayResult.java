package org.box2d4j;

public class b2RayResult {
    public b2ShapeId shapeId = new b2ShapeId();
    public b2Vec2 point = new b2Vec2();
    public b2Vec2 normal = new b2Vec2();
    public float fraction;
    public int nodeVisits;
    public int leafVisits;
    public boolean hit;
}
