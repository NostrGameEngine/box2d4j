package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class Wedge {
    private Wedge() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-4.0f, 8.0f), new b2Vec2(0.0f, 0.0f)));
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(0.0f, 0.0f), new b2Vec2(0.0f, 8.0f)));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(-0.45f, 10.75f);
        bodyDef.linearVelocity = new b2Vec2(0.0f, -200.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2Circle circle = new b2Circle();
        circle.radius = 0.3f;
        shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.2f;
        b2CreateCircleShape(bodyId, shapeDef, circle);

        return ContinuousSampleResult.simulate("wedge", worldId, bodyId);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
