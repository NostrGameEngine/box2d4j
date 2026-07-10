package org.box2d4j;

public class b2DistanceInput {
    public b2ShapeProxy proxyA = new b2ShapeProxy();
    public b2ShapeProxy proxyB = new b2ShapeProxy();
    public b2Transform transformA = new b2Transform();
    public b2Transform transformB = new b2Transform();
    public boolean useRadii;
}
