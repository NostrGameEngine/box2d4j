package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class SkinnyBox {
    private SkinnyBox() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.9f;
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-10.0f, 0.0f), new b2Vec2(10.0f, 0.0f)));
        b2Polygon obstacle = b2MakeOffsetBox(0.1f, 1.0f, new b2Vec2(0.0f, 1.0f), b2Rot_identity);
        b2CreatePolygonShape(groundId, shapeDef, obstacle);

        int[] random = {12345};
        boolean[] capsule = {false};
        boolean[] autoTest = {false};
        b2BodyId[] bodyId = {launch(worldId, null, capsule[0], random)};
        SampleRuntime.toggle("skinnyBox.capsule", "Capsule", capsule[0], value -> capsule[0] = value);
        SampleRuntime.action("skinnyBox.launch", "Launch",
            () -> bodyId[0] = launch(worldId, bodyId[0], capsule[0], random));
        SampleRuntime.toggle("skinnyBox.autoTest", "Auto Test", autoTest[0], value -> autoTest[0] = value);
        int[] runtimeStep = {0};
        SampleRuntime.afterStep(() -> {
            runtimeStep[0] += 1;
            if (autoTest[0] && runtimeStep[0] % 60 == 0) {
                bodyId[0] = launch(worldId, bodyId[0], capsule[0], random);
            }
        });

        return ContinuousSampleResult.simulate("skinnyBox", worldId, bodyId[0]);
    }

    private static b2BodyId launch(b2WorldId worldId, b2BodyId previousBodyId, boolean capsule, int[] random) {
        if (previousBodyId != null && b2Body_IsValid(previousBodyId)) {
            b2DestroyBody(previousBodyId);
        }
        int value = random[0];
        value ^= value << 13;
        value ^= value >>> 17;
        value ^= value << 5;
        random[0] = value;
        float angularVelocity = 100.0f * ((value & 32767) / 32767.0f) - 50.0f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 8.0f);
        bodyDef.angularVelocity = angularVelocity;
        bodyDef.linearVelocity = new b2Vec2(0.0f, -100.0f);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.9f;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        if (capsule) {
            b2CreateCapsuleShape(bodyId, shapeDef,
                new b2Capsule(new b2Vec2(0.0f, -1.0f), new b2Vec2(0.0f, 1.0f), 0.1f));
        } else {
            b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(2.0f, 0.05f));
        }
        return bodyId;
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
