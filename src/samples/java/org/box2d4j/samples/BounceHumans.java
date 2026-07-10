package org.box2d4j.samples;

import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2CosSin;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class BounceHumans {
    private BounceHumans() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.restitution = 1.3f;
        shapeDef.material.friction = 0.1f;
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-10.0f, -10.0f), new b2Vec2(10.0f, -10.0f)));
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(10.0f, -10.0f), new b2Vec2(10.0f, 10.0f)));
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(10.0f, 10.0f), new b2Vec2(-10.0f, 10.0f)));
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-10.0f, 10.0f), new b2Vec2(-10.0f, -10.0f)));
        shapeDef.material.restitution = 2.0f;
        b2CreateCircleShape(groundId, shapeDef, new b2Circle(new b2Vec2(), 2.0f));

        Ragdoll.Human human = Ragdoll.Human.create(
            worldId, new b2Vec2(0.0f, 5.0f), 1.0f, 0.0f, 1.0f, 0.1f, 1);
        float[] time = {0.0f};
        return ContinuousSampleResult.simulate("bounceHumans", worldId, human.bodies[0], step -> {
            b2CosSin cs1 = b2ComputeCosSin(0.5f * time[0]);
            b2CosSin cs2 = b2ComputeCosSin(time[0]);
            b2World_SetGravity(worldId, new b2Vec2(10.0f * cs1.sine, 10.0f * cs2.cosine));
            time[0] += 1.0f / 60.0f;
        });
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
