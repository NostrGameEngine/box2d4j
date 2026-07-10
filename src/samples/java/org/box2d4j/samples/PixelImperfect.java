package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class PixelImperfect {
    private PixelImperfect() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        float pixelsPerMeter = 30.0f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_staticBody;
        bodyDef.position = new b2Vec2(175.0f / pixelsPerMeter, 150.0f / pixelsPerMeter);
        b2BodyId blockId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.0f;
        b2CreatePolygonShape(blockId, shapeDef,
            b2MakeBox(20.0f / pixelsPerMeter, 10.0f / pixelsPerMeter));

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(200.0f / pixelsPerMeter, 275.0f / pixelsPerMeter);
        bodyDef.gravityScale = 0.0f;
        b2BodyId ballId = b2CreateBody(worldId, bodyDef);
        b2Polygon ball = b2MakeRoundedBox(
            4.0f / pixelsPerMeter, 4.0f / pixelsPerMeter, 0.9f / pixelsPerMeter);
        shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.0f;
        b2CreatePolygonShape(ballId, shapeDef, ball);
        b2Body_SetLinearVelocity(ballId, new b2Vec2(0.0f, -5.0f));
        b2Body_SetFixedRotation(ballId, true);

        return ContinuousSampleResult.simulate("pixelImperfect", worldId, ballId);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
