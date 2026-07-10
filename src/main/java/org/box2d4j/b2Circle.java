package org.box2d4j;

public class b2Circle {
    public b2Vec2 center = new b2Vec2();
    public float radius;

    public b2Circle() {
    }

    public b2Circle(b2Vec2 center, float radius) {
        this.center = new b2Vec2(center);
        this.radius = radius;
    }
}
