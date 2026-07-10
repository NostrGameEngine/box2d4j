package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class ChainDrop {
    private ChainDrop() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(0.0f, -6.0f);
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2Vec2[] points = {
            new b2Vec2(-10.0f, -2.0f),
            new b2Vec2(10.0f, -2.0f),
            new b2Vec2(10.0f, 1.0f),
            new b2Vec2(-10.0f, 1.0f)
        };
        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = points;
        chainDef.count = points.length;
        chainDef.isLoop = true;
        b2CreateChain(groundId, chainDef);

        float[] parameters = {-42.0f, -0.1f};
        b2BodyId[] bodyId = {launch(worldId, null, parameters[0], parameters[1])};
        SampleRuntime.slider("chainDrop.speed", "Speed", parameters[0], -100.0f, 0.0f, 1.0f,
            value -> parameters[0] = value);
        SampleRuntime.slider("chainDrop.yOffset", "Y Offset", parameters[1], -1.0f, 1.0f, 0.1f,
            value -> parameters[1] = value);
        SampleRuntime.action("chainDrop.launch", "Launch",
            () -> bodyId[0] = launch(worldId, bodyId[0], parameters[0], parameters[1]));

        return ContinuousSampleResult.simulate("chainDrop", worldId, bodyId[0]);
    }

    private static b2BodyId launch(b2WorldId worldId, b2BodyId previousBodyId, float speed, float yOffset) {
        if (previousBodyId != null && b2Body_IsValid(previousBodyId)) {
            b2DestroyBody(previousBodyId);
        }
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.linearVelocity = new b2Vec2(0.0f, speed);
        bodyDef.position = new b2Vec2(0.0f, 10.0f + yOffset);
        bodyDef.rotation = b2MakeRot(0.5f * B2_PI);
        bodyDef.fixedRotation = true;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(bodyId, b2DefaultShapeDef(), new b2Circle(new b2Vec2(), 0.5f));
        return bodyId;
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
