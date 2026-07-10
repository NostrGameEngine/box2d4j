package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2Circle;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class Pinball {
    private Pinball() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2Vec2[] boundary = {
            new b2Vec2(-8.0f, 6.0f),
            new b2Vec2(-8.0f, 20.0f),
            new b2Vec2(8.0f, 20.0f),
            new b2Vec2(8.0f, 6.0f),
            new b2Vec2(0.0f, -2.0f)
        };
        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = boundary;
        chainDef.count = boundary.length;
        chainDef.isLoop = true;
        b2CreateChain(groundId, chainDef);

        b2Vec2 leftAnchor = new b2Vec2(-2.0f, 0.0f);
        b2Vec2 rightAnchor = new b2Vec2(2.0f, 0.0f);
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.enableSleep = false;
        bodyDef.position = leftAnchor;
        b2BodyId leftFlipperId = b2CreateBody(worldId, bodyDef);
        bodyDef.position = rightAnchor;
        b2BodyId rightFlipperId = b2CreateBody(worldId, bodyDef);
        b2Polygon flipper = b2MakeBox(1.75f, 0.2f);
        b2CreatePolygonShape(leftFlipperId, b2DefaultShapeDef(), flipper);
        b2CreatePolygonShape(rightFlipperId, b2DefaultShapeDef(), flipper);

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = groundId;
        jointDef.localAnchorB = new b2Vec2();
        jointDef.enableMotor = true;
        jointDef.maxMotorTorque = 1000.0f;
        jointDef.enableLimit = true;
        jointDef.localAnchorA = leftAnchor;
        jointDef.bodyIdB = leftFlipperId;
        jointDef.lowerAngle = -30.0f * B2_PI / 180.0f;
        jointDef.upperAngle = 5.0f * B2_PI / 180.0f;
        b2JointId leftJointId = b2CreateRevoluteJoint(worldId, jointDef);

        jointDef.localAnchorA = rightAnchor;
        jointDef.bodyIdB = rightFlipperId;
        jointDef.lowerAngle = -5.0f * B2_PI / 180.0f;
        jointDef.upperAngle = 30.0f * B2_PI / 180.0f;
        b2JointId rightJointId = b2CreateRevoluteJoint(worldId, jointDef);

        boolean interactive = SampleRuntime.isActive();
        SampleRuntime.hold("pinball.flippers", "Flippers", "SPACE", pressed -> {
            b2RevoluteJoint_SetMotorSpeed(leftJointId, pressed ? 20.0f : -10.0f);
            b2RevoluteJoint_SetMotorSpeed(rightJointId, pressed ? -20.0f : 10.0f);
        });

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(-4.0f, 17.0f);
        b2BodyId spinnerId = b2CreateBody(worldId, bodyDef);
        b2Polygon spinnerHorizontal = b2MakeBox(1.5f, 0.125f);
        b2Polygon spinnerVertical = b2MakeBox(0.125f, 1.5f);
        b2CreatePolygonShape(spinnerId, b2DefaultShapeDef(), spinnerHorizontal);
        b2CreatePolygonShape(spinnerId, b2DefaultShapeDef(), spinnerVertical);
        jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = groundId;
        jointDef.bodyIdB = spinnerId;
        jointDef.localAnchorA = bodyDef.position;
        jointDef.localAnchorB = new b2Vec2();
        jointDef.enableMotor = true;
        jointDef.maxMotorTorque = 0.1f;
        b2CreateRevoluteJoint(worldId, jointDef);

        bodyDef.position = new b2Vec2(4.0f, 8.0f);
        spinnerId = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(spinnerId, b2DefaultShapeDef(), spinnerHorizontal);
        b2CreatePolygonShape(spinnerId, b2DefaultShapeDef(), spinnerVertical);
        jointDef.bodyIdB = spinnerId;
        jointDef.localAnchorA = bodyDef.position;
        b2CreateRevoluteJoint(worldId, jointDef);

        bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(-4.0f, 8.0f);
        b2BodyId bumperId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef bumperDef = b2DefaultShapeDef();
        bumperDef.material.restitution = 1.5f;
        b2Circle bumper = new b2Circle(new b2Vec2(), 1.0f);
        b2CreateCircleShape(bumperId, bumperDef, bumper);
        bodyDef.position = new b2Vec2(4.0f, 17.0f);
        bumperId = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(bumperId, bumperDef, bumper);

        bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(1.0f, 15.0f);
        bodyDef.type = b2_dynamicBody;
        bodyDef.isBullet = true;
        b2BodyId ballId = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(ballId, b2DefaultShapeDef(), new b2Circle(new b2Vec2(), 0.2f));

        return ContinuousSampleResult.simulate("pinball", worldId, ballId, step -> {
            if (!interactive && step > 1) {
                b2RevoluteJoint_SetMotorSpeed(leftJointId, -10.0f);
                b2RevoluteJoint_SetMotorSpeed(rightJointId, 10.0f);
            }
        });
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
