package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class RestitutionThreshold {
    private RestitutionThreshold() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        float pixelsPerMeter = 30.0f;
        b2World_SetRestitutionThreshold(worldId, 0.1f);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_staticBody;
        bodyDef.position = new b2Vec2(205.0f / pixelsPerMeter, 120.0f / pixelsPerMeter);
        bodyDef.rotation = b2MakeRot(70.0f * 3.14f / 180.0f);
        b2BodyId blockId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.0f;
        b2CreatePolygonShape(blockId, shapeDef,
            b2MakeBox(50.0f / pixelsPerMeter, 5.0f / pixelsPerMeter));

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(200.0f / pixelsPerMeter, 250.0f / pixelsPerMeter);
        b2BodyId ballId = b2CreateBody(worldId, bodyDef);
        b2Circle ball = new b2Circle();
        ball.radius = 5.0f / pixelsPerMeter;
        shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.0f;
        shapeDef.material.restitution = 1.0f;
        b2CreateCircleShape(ballId, shapeDef, ball);
        b2Body_SetLinearVelocity(ballId, new b2Vec2(0.0f, -2.9f));
        b2Body_SetFixedRotation(ballId, true);

        return ContinuousSampleResult.simulate("restitutionThreshold", worldId, ballId);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
