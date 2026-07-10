package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class SegmentSlide {
    private SegmentSlide() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-40.0f, 0.0f), new b2Vec2(40.0f, 0.0f)));
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(40.0f, 0.0f), new b2Vec2(40.0f, 10.0f)));

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.linearVelocity = new b2Vec2(100.0f, 0.0f);
        bodyDef.position = new b2Vec2(-20.0f, 0.7f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(bodyId, b2DefaultShapeDef(), new b2Circle(new b2Vec2(), 0.5f));

        return ContinuousSampleResult.simulate("segmentSlide", worldId, bodyId);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
