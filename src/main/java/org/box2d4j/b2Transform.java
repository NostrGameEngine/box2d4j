package org.box2d4j;

/** 2D rigid transform. Mirrors Box2D's {@code b2Transform}. */
public class b2Transform {
    public b2Vec2 p = new b2Vec2();
    public b2Rot q = new b2Rot();

    public b2Transform() {
    }

    public b2Transform(b2Vec2 p, b2Rot q) {
        this.p = new b2Vec2(p);
        this.q = new b2Rot(q);
    }

    public b2Transform(b2Transform other) {
        this(other.p, other.q);
    }

    public b2Transform copy() {
        return new b2Transform(this);
    }
}
