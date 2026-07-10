package org.box2d4j;

public class b2ManifoldPoint {
    public b2Vec2 point = new b2Vec2();
    public b2Vec2 anchorA = new b2Vec2();
    public b2Vec2 anchorB = new b2Vec2();
    public float separation;
    public float normalImpulse;
    public float tangentImpulse;
    public float totalNormalImpulse;
    public float normalVelocity;
    public int id;
    public boolean persisted;
}
