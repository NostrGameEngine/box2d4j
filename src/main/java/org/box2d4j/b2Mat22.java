package org.box2d4j;

/** 2-by-2 matrix stored by columns. Mirrors Box2D's {@code b2Mat22}. */
public class b2Mat22 {
    public b2Vec2 cx = new b2Vec2();
    public b2Vec2 cy = new b2Vec2();

    public b2Mat22() {
    }

    public b2Mat22(b2Vec2 cx, b2Vec2 cy) {
        this.cx = new b2Vec2(cx);
        this.cy = new b2Vec2(cy);
    }

    public b2Mat22(b2Mat22 other) {
        this(other.cx, other.cy);
    }

    public b2Mat22 copy() {
        return new b2Mat22(this);
    }
}
