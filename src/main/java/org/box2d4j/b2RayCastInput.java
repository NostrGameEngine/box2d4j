package org.box2d4j;

public class b2RayCastInput {
    public b2Vec2 origin = new b2Vec2();
    public b2Vec2 translation = new b2Vec2();
    public float maxFraction;

    public b2RayCastInput() {
    }

    public b2RayCastInput(b2Vec2 origin, b2Vec2 translation, float maxFraction) {
        this.origin = new b2Vec2(origin);
        this.translation = new b2Vec2(translation);
        this.maxFraction = maxFraction;
    }
}
