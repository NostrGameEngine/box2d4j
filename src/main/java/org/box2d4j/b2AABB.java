package org.box2d4j;

/** Axis-aligned bounding box. Mirrors Box2D's {@code b2AABB}. */
public class b2AABB {
    public b2Vec2 lowerBound = new b2Vec2();
    public b2Vec2 upperBound = new b2Vec2();

    public b2AABB() {
    }

    public b2AABB(b2Vec2 lowerBound, b2Vec2 upperBound) {
        this.lowerBound = new b2Vec2(lowerBound);
        this.upperBound = new b2Vec2(upperBound);
    }

    public b2AABB(b2AABB other) {
        this(other.lowerBound, other.upperBound);
    }

    public b2AABB copy() {
        return new b2AABB(this);
    }
}
