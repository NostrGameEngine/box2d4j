package org.box2d4j;

/** 2D rotation represented as cosine/sine. Mirrors Box2D's {@code b2Rot}. */
public class b2Rot {
    public float c;
    public float s;

    public b2Rot() {
    }

    public b2Rot(float c, float s) {
        this.c = c;
        this.s = s;
    }

    public b2Rot(b2Rot other) {
        this(other.c, other.s);
    }

    public b2Rot set(float c, float s) {
        this.c = c;
        this.s = s;
        return this;
    }

    public b2Rot set(b2Rot other) {
        this.c = other.c;
        this.s = other.s;
        return this;
    }

    public b2Rot copy() {
        return new b2Rot(this);
    }
}
