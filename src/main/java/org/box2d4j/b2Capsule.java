package org.box2d4j;

public class b2Capsule {
    public b2Vec2 center1 = new b2Vec2();
    public b2Vec2 center2 = new b2Vec2();
    public float radius;

    public b2Capsule() {
    }

    public b2Capsule(b2Vec2 center1, b2Vec2 center2, float radius) {
        this.center1 = new b2Vec2(center1);
        this.center2 = new b2Vec2(center2);
        this.radius = radius;
    }
}
