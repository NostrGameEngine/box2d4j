package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.ArrayList;
import java.util.List;

import static org.box2d4j.B2.*;

public final class Drop {
    private Drop() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2World_EnableSleeping(worldId, false);
        Scene scene = new Scene(worldId);
        scene.scene1();

        SampleRuntime.action("drop.scene1", "Scene 1: Ball", "1", scene::scene1);
        SampleRuntime.action("drop.scene2", "Scene 2: Ruler", "2", scene::scene2);
        SampleRuntime.action("drop.scene3", "Scene 3: Ragdoll", "3", scene::scene3);
        SampleRuntime.action("drop.scene4", "Scene 4: Bullet Stack", "4", scene::scene4);
        SampleRuntime.toggle("drop.continuous", "Continuous", "C", true, scene::setContinuous);
        SampleRuntime.toggle("drop.speculative", "Speculative", "V", true, scene::setSpeculative);
        SampleRuntime.toggle("drop.slowTime", "Slow Time", "S", false,
            value -> SampleRuntime.targetHz(value ? 1.0f : 60.0f));

        return ContinuousSampleResult.simulate("drop", worldId, scene.primaryBodyId);
    }

    private static final class Scene {
        final b2WorldId worldId;
        final List<b2BodyId> groundIds = new ArrayList<>();
        final List<b2BodyId> bodyIds = new ArrayList<>();
        Ragdoll.Human human;
        b2BodyId primaryBodyId = b2_nullBodyId;
        boolean continuous = true;
        boolean speculative = true;

        Scene(b2WorldId worldId) {
            this.worldId = worldId;
        }

        void clear() {
            for (b2BodyId bodyId : bodyIds) {
                if (b2Body_IsValid(bodyId)) {
                    b2DestroyBody(bodyId);
                }
            }
            bodyIds.clear();
            if (human != null) {
                human.destroy();
                human = null;
            }
            primaryBodyId = b2_nullBodyId;
        }

        void clearGround() {
            for (b2BodyId bodyId : groundIds) {
                if (b2Body_IsValid(bodyId)) {
                    b2DestroyBody(bodyId);
                }
            }
            groundIds.clear();
        }

        void createGround1() {
            clearGround();
            b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            float width = 0.25f;
            int count = 40;
            b2CreateSegmentShape(groundId, shapeDef, new b2Segment(
                new b2Vec2(-0.5f * count * width, 0.0f), new b2Vec2(0.5f * count * width, 0.0f)));
            groundIds.add(groundId);
        }

        void createGround2() {
            clearGround();
            b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            float width = 0.25f;
            int count = 40;
            float x = -0.5f * count * width;
            float halfHeight = 0.05f;
            for (int i = 0; i <= count; ++i) {
                b2Polygon box = b2MakeOffsetBox(0.5f * width, halfHeight, new b2Vec2(x, 0.0f), b2Rot_identity);
                b2CreatePolygonShape(groundId, shapeDef, box);
                x += width;
            }
            groundIds.add(groundId);
        }

        void createGround3() {
            clearGround();
            b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateSegmentShape(groundId, shapeDef,
                new b2Segment(new b2Vec2(-5.0f, 0.0f), new b2Vec2(5.0f, 0.0f)));
            b2CreateSegmentShape(groundId, shapeDef,
                new b2Segment(new b2Vec2(3.0f, 0.0f), new b2Vec2(3.0f, 8.0f)));
            groundIds.add(groundId);
        }

        void scene1() {
            clear();
            createGround2();
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(0.0f, 4.0f);
            bodyDef.linearVelocity = new b2Vec2(0.0f, -100.0f);
            primaryBodyId = b2CreateBody(worldId, bodyDef);
            b2CreateCircleShape(primaryBodyId, b2DefaultShapeDef(),
                new b2Circle(new b2Vec2(), 0.125f));
            bodyIds.add(primaryBodyId);
        }

        void scene2() {
            clear();
            createGround1();
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(0.0f, 4.0f);
            bodyDef.rotation = b2MakeRot(0.5f * B2_PI);
            bodyDef.angularVelocity = -0.5f;
            primaryBodyId = b2CreateBody(worldId, bodyDef);
            b2CreatePolygonShape(primaryBodyId, b2DefaultShapeDef(), b2MakeBox(0.75f, 0.01f));
            bodyIds.add(primaryBodyId);
        }

        void scene3() {
            clear();
            createGround2();
            human = Ragdoll.Human.create(worldId, new b2Vec2(0.0f, 40.0f), 1.0f,
                0.03f, 1.0f, 0.5f, 1);
            primaryBodyId = human.bodies[0];
        }

        void scene4() {
            clear();
            createGround3();
            float halfExtent = 0.25f;
            b2Polygon box = b2MakeSquare(halfExtent);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            float offset = 0.01f;
            for (int i = 0; i < 5; ++i) {
                b2BodyDef bodyDef = b2DefaultBodyDef();
                bodyDef.type = b2_dynamicBody;
                float shift = i % 2 == 0 ? -offset : offset;
                bodyDef.position = new b2Vec2(2.5f + shift, halfExtent + 2.0f * halfExtent * i);
                b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
                b2CreatePolygonShape(bodyId, shapeDef, box);
                bodyIds.add(bodyId);
            }
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(-7.7f, 1.9f);
            bodyDef.linearVelocity = new b2Vec2(200.0f, 0.0f);
            bodyDef.isBullet = true;
            shapeDef.density = 4.0f;
            primaryBodyId = b2CreateBody(worldId, bodyDef);
            b2CreateCircleShape(primaryBodyId, shapeDef, new b2Circle(new b2Vec2(), 0.125f));
            bodyIds.add(primaryBodyId);
        }

        void setContinuous(boolean value) {
            if (continuous != value) {
                clear();
                continuous = value;
                b2World_EnableContinuous(worldId, value);
            }
        }

        void setSpeculative(boolean value) {
            if (speculative != value) {
                clear();
                speculative = value;
                b2World_EnableSpeculative(worldId, value);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
