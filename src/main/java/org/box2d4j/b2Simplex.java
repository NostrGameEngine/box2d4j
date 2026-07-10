package org.box2d4j;

public class b2Simplex {
    public b2SimplexVertex v1 = new b2SimplexVertex();
    public b2SimplexVertex v2 = new b2SimplexVertex();
    public b2SimplexVertex v3 = new b2SimplexVertex();
    public int count;

    public b2SimplexVertex[] vertices() {
        return new b2SimplexVertex[] {v1, v2, v3};
    }

    public b2Simplex set(b2Simplex other) {
        this.v1.set(other.v1);
        this.v2.set(other.v2);
        this.v3.set(other.v3);
        this.count = other.count;
        return this;
    }
}
