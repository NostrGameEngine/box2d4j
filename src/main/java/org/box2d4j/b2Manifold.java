package org.box2d4j;

public class b2Manifold {
    public b2Vec2 normal = new b2Vec2();
    public float rollingImpulse;
    public b2ManifoldPoint[] points = {new b2ManifoldPoint(), new b2ManifoldPoint()};
    public int pointCount;
}
