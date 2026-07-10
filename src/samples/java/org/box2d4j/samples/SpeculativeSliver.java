package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Segment;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class SpeculativeSliver {
    private SpeculativeSliver() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreateSegmentShape(groundId, b2DefaultShapeDef(),
            new b2Segment(new b2Vec2(-10.0f, 0.0f), new b2Vec2(10.0f, 0.0f)));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 12.0f);
        bodyDef.linearVelocity = new b2Vec2(0.0f, -100.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2Vec2[] points = {
            new b2Vec2(-2.0f, 0.0f),
            new b2Vec2(-1.0f, 0.0f),
            new b2Vec2(2.0f, 0.5f)
        };
        b2Hull hull = b2ComputeHull(points, points.length);
        b2CreatePolygonShape(bodyId, b2DefaultShapeDef(), b2MakePolygon(hull, 0.0f));

        return ContinuousSampleResult.simulate("speculativeSliver", worldId, bodyId);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
