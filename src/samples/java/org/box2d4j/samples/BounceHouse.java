package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class BounceHouse {
    private BounceHouse() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-10.0f, -10.0f), new b2Vec2(10.0f, -10.0f)));
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(10.0f, -10.0f), new b2Vec2(10.0f, 10.0f)));
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(10.0f, 10.0f), new b2Vec2(-10.0f, 10.0f)));
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-10.0f, 10.0f), new b2Vec2(-10.0f, -10.0f)));

        int[] shapeType = {2};
        boolean[] hitEvents = {true};
        b2BodyId[] bodyId = {launch(worldId, null, shapeType[0], hitEvents[0])};
        SampleRuntime.choice("bounceHouse.shape", "Shape", shapeType[0],
            new String[] {"Circle", "Capsule", "Box"}, value -> {
                shapeType[0] = value;
                bodyId[0] = launch(worldId, bodyId[0], shapeType[0], hitEvents[0]);
            });
        SampleRuntime.toggle("bounceHouse.hitEvents", "Hit Events", hitEvents[0], value -> {
            hitEvents[0] = value;
            if (b2Body_IsValid(bodyId[0])) {
                b2Body_EnableHitEvents(bodyId[0], value);
            }
        });
        SampleRuntime.action("bounceHouse.launch", "Launch",
            () -> bodyId[0] = launch(worldId, bodyId[0], shapeType[0], hitEvents[0]));

        return ContinuousSampleResult.simulate("bounceHouse", worldId, bodyId[0]);
    }

    private static b2BodyId launch(b2WorldId worldId, b2BodyId previousBodyId, int shapeType,
                                   boolean enableHitEvents) {
        if (previousBodyId != null && b2Body_IsValid(previousBodyId)) {
            b2DestroyBody(previousBodyId);
        }

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.linearVelocity = new b2Vec2(10.0f, 20.0f);
        bodyDef.position = new b2Vec2();
        bodyDef.gravityScale = 0.0f;
        bodyDef.allowFastRotation = shapeType == 0;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.restitution = 1.2f;
        shapeDef.material.friction = 0.3f;
        shapeDef.enableHitEvents = enableHitEvents;
        if (shapeType == 0) {
            b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(), 0.5f));
        } else if (shapeType == 1) {
            b2CreateCapsuleShape(bodyId, shapeDef,
                new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f));
        } else {
            float halfHeight = 0.1f;
            b2Polygon box = b2MakeBox(20.0f * halfHeight, halfHeight);
            b2CreatePolygonShape(bodyId, shapeDef, box);
        }
        return bodyId;
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
