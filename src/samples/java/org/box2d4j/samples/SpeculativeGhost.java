package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class SpeculativeGhost {
    private SpeculativeGhost() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreateSegmentShape(groundId, b2DefaultShapeDef(),
            new b2Segment(new b2Vec2(-10.0f, 0.0f), new b2Vec2(10.0f, 0.0f)));
        b2Polygon obstacle = b2MakeOffsetBox(1.0f, 0.1f, new b2Vec2(0.0f, 0.9f), b2Rot_identity);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), obstacle);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.015f, 2.515f);
        bodyDef.linearVelocity = new b2Vec2(0.1f * 1.25f * 60.0f, -0.1f * 1.25f * 60.0f);
        bodyDef.gravityScale = 0.0f;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyId, b2DefaultShapeDef(), b2MakeSquare(0.25f));

        return ContinuousSampleResult.simulate("speculativeGhost", worldId, bodyId);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
