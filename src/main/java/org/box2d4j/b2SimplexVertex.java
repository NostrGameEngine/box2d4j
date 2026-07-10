package org.box2d4j;

public class b2SimplexVertex {
    public b2Vec2 wA = new b2Vec2();
    public b2Vec2 wB = new b2Vec2();
    public b2Vec2 w = new b2Vec2();
    public float a;
    public int indexA;
    public int indexB;

    public b2SimplexVertex set(b2SimplexVertex other) {
        this.wA.set(other.wA);
        this.wB.set(other.wB);
        this.w.set(other.w);
        this.a = other.a;
        this.indexA = other.indexA;
        this.indexB = other.indexB;
        return this;
    }
}
