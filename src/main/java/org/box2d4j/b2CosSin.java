package org.box2d4j;

/** Cosine/sine pair. Mirrors Box2D's {@code b2CosSin}. */
public class b2CosSin {
    public float cosine;
    public float sine;

    public b2CosSin() {
    }

    public b2CosSin(float cosine, float sine) {
        this.cosine = cosine;
        this.sine = sine;
    }

    public b2CosSin(b2CosSin other) {
        this(other.cosine, other.sine);
    }

    public b2CosSin copy() {
        return new b2CosSin(this);
    }
}
