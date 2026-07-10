package org.box2d4j;

public class b2ShapeCastPairInput {
    public b2ShapeProxy proxyA = new b2ShapeProxy();
    public b2ShapeProxy proxyB = new b2ShapeProxy();
    public b2Transform transformA = new b2Transform();
    public b2Transform transformB = new b2Transform();
    public b2Vec2 translationB = new b2Vec2();
    public float maxFraction;
    public boolean canEncroach;
}
