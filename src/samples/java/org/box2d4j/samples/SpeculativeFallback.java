package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class SpeculativeFallback {
    private SpeculativeFallback() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-10.0f, 0.0f), new b2Vec2(10.0f, 0.0f)));
        b2Vec2[] points = {
            new b2Vec2(-2.0f, 4.0f),
            new b2Vec2(2.0f, 4.0f),
            new b2Vec2(2.0f, 4.1f),
            new b2Vec2(-0.5f, 4.2f),
            new b2Vec2(-2.0f, 4.2f)
        };
        b2Hull hull = b2ComputeHull(points, points.length);
        b2CreatePolygonShape(groundId, shapeDef, b2MakePolygon(hull, 0.0f));

        float offset = 8.0f;
        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(offset, 12.0f);
        bodyDef.linearVelocity = new b2Vec2(0.0f, -100.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2Polygon box = b2MakeOffsetBox(2.0f, 0.05f, new b2Vec2(-offset, 0.0f), b2MakeRot(B2_PI));
        b2CreatePolygonShape(bodyId, b2DefaultShapeDef(), box);

        return ContinuousSampleResult.simulate("speculativeFallback", worldId, bodyId);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
