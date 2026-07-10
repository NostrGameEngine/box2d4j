package org.box2d4j;

public class b2Polygon {
    public b2Vec2[] vertices = b2Vec2.array(B2.B2_MAX_POLYGON_VERTICES);
    public b2Vec2[] normals = b2Vec2.array(B2.B2_MAX_POLYGON_VERTICES);
    public b2Vec2 centroid = new b2Vec2();
    public float radius;
    public int count;
}
