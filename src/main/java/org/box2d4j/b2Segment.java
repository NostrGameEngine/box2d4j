package org.box2d4j;

public class b2Segment {
    public b2Vec2 point1 = new b2Vec2();
    public b2Vec2 point2 = new b2Vec2();

    public b2Segment() {
    }

    public b2Segment(b2Vec2 point1, b2Vec2 point2) {
        this.point1 = new b2Vec2(point1);
        this.point2 = new b2Vec2(point2);
    }
}
