package org.box2d4j;

/** Plane represented as {@code dot(normal, point) - offset}. */
public class b2Plane {
    public b2Vec2 normal = new b2Vec2();
    public float offset;

    public b2Plane() {
    }

    public b2Plane(b2Vec2 normal, float offset) {
        this.normal = new b2Vec2(normal);
        this.offset = offset;
    }

    public b2Plane(b2Plane other) {
        this(other.normal, other.offset);
    }

    public b2Plane copy() {
        return new b2Plane(this);
    }
}
