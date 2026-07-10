package org.box2d4j;

/** 2D vector. Mirrors Box2D's {@code b2Vec2}. */
public class b2Vec2 {
    public float x;
    public float y;

    public b2Vec2() {
    }

    public b2Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public b2Vec2(b2Vec2 other) {
        this(other.x, other.y);
    }

    public b2Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public b2Vec2 set(b2Vec2 other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    public b2Vec2 copy() {
        return new b2Vec2(this);
    }

    public static b2Vec2[] array(int count) {
        b2Vec2[] values = new b2Vec2[count];
        for (int i = 0; i < count; ++i) {
            values[i] = new b2Vec2();
        }
        return values;
    }
}
