package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class Cart {
    private Cart() {
    }

    public static RobustnessSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(0.0f, -1.0f);
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(20.0f, 1.0f));

        b2World_SetGravity(worldId, new b2Vec2(0.0f, -22.0f));
        Parameters parameters = new Parameters();
        SceneData[] scene = {createScene(worldId, null, parameters)};
        SampleRuntime.slider("cart.contactHertz", "Contact Hertz", parameters.contactHertz,
            0.0f, 240.0f, 1.0f, value -> {
                parameters.contactHertz = value;
                scene[0] = createScene(worldId, scene[0], parameters);
            });
        SampleRuntime.slider("cart.contactDamping", "Contact Damping", parameters.contactDamping,
            0.0f, 1000.0f, 1.0f, value -> {
                parameters.contactDamping = value;
                scene[0] = createScene(worldId, scene[0], parameters);
            });
        SampleRuntime.slider("cart.contactSpeed", "Contact Speed", parameters.contactSpeed,
            0.0f, 5.0f, 0.1f, value -> {
                parameters.contactSpeed = value;
                scene[0] = createScene(worldId, scene[0], parameters);
            });
        SampleRuntime.slider("cart.jointHertz", "Joint Hertz", parameters.jointHertz,
            0.0f, 240.0f, 1.0f, value -> {
                parameters.jointHertz = value;
                scene[0] = createScene(worldId, scene[0], parameters);
            });
        SampleRuntime.slider("cart.jointDamping", "Joint Damping", parameters.jointDamping,
            0.0f, 1000.0f, 1.0f, value -> {
                parameters.jointDamping = value;
                scene[0] = createScene(worldId, scene[0], parameters);
            });
        SampleRuntime.action("cart.reset", "Reset Scene",
            () -> scene[0] = createScene(worldId, scene[0], parameters));

        return RobustnessSampleResult.simulate("cart", worldId, scene[0].bodies());
    }

    private static SceneData createScene(b2WorldId worldId, SceneData previous, Parameters parameters) {
        if (previous != null) {
            if (b2Body_IsValid(previous.chassisId)) {
                b2DestroyBody(previous.chassisId);
            }
            if (b2Body_IsValid(previous.wheel1Id)) {
                b2DestroyBody(previous.wheel1Id);
            }
            if (b2Body_IsValid(previous.wheel2Id)) {
                b2DestroyBody(previous.wheel2Id);
            }
        }
        b2World_SetContactTuning(worldId, parameters.contactHertz, parameters.contactDamping,
            parameters.contactSpeed);
        float yBase = 2.0f;
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, yBase);
        b2BodyId chassisId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 100.0f;
        b2CreatePolygonShape(chassisId, shapeDef,
            b2MakeOffsetBox(0.5f, 0.25f, new b2Vec2(0.0f, 0.25f), b2Rot_identity));

        shapeDef = b2DefaultShapeDef();
        shapeDef.material.rollingResistance = 0.02f;
        shapeDef.density = 10.0f;
        b2Circle circle = new b2Circle(new b2Vec2(), 0.1f);
        bodyDef.position = new b2Vec2(-0.4f, yBase - 0.15f);
        b2BodyId wheel1Id = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(wheel1Id, shapeDef, circle);
        bodyDef.position = new b2Vec2(0.4f, yBase - 0.15f);
        b2BodyId wheel2Id = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(wheel2Id, shapeDef, circle);

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = chassisId;
        jointDef.bodyIdB = wheel1Id;
        jointDef.localAnchorA = new b2Vec2(-0.4f, -0.15f);
        jointDef.localAnchorB = new b2Vec2();
        b2JointId joint1Id = b2CreateRevoluteJoint(worldId, jointDef);
        b2Joint_SetConstraintTuning(joint1Id, parameters.jointHertz, parameters.jointDamping);
        jointDef.bodyIdB = wheel2Id;
        jointDef.localAnchorA = new b2Vec2(0.4f, -0.15f);
        b2JointId joint2Id = b2CreateRevoluteJoint(worldId, jointDef);
        b2Joint_SetConstraintTuning(joint2Id, parameters.jointHertz, parameters.jointDamping);

        return new SceneData(chassisId, wheel1Id, wheel2Id, joint1Id, joint2Id);
    }

    private static final class SceneData {
        final b2BodyId chassisId;
        final b2BodyId wheel1Id;
        final b2BodyId wheel2Id;
        final b2JointId joint1Id;
        final b2JointId joint2Id;

        SceneData(b2BodyId chassisId, b2BodyId wheel1Id, b2BodyId wheel2Id,
                  b2JointId joint1Id, b2JointId joint2Id) {
            this.chassisId = chassisId;
            this.wheel1Id = wheel1Id;
            this.wheel2Id = wheel2Id;
            this.joint1Id = joint1Id;
            this.joint2Id = joint2Id;
        }

        b2BodyId[] bodies() {
            return new b2BodyId[] {chassisId, wheel1Id, wheel2Id};
        }
    }

    private static final class Parameters {
        float contactHertz = 30.0f;
        float contactDamping = 10.0f;
        float contactSpeed = 3.0f;
        float jointHertz = 60.0f;
        float jointDamping = 1.0f;
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
