package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class RevoluteJoint {
    private static final int DEFAULT_STEP_COUNT = 120;

    private RevoluteJoint() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyId groundId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(0.0f, -1.0f);
            groundId = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeBox(40.0f, 1.0f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreatePolygonShape(groundId, shapeDef, box);
        }

        boolean enableSpring = false;
        boolean enableLimit = true;
        boolean enableMotor = false;
        float hertz = 2.0f;
        float dampingRatio = 0.5f;
        float targetDegrees = 45.0f;
        float motorSpeed = 1.0f;
        float motorTorque = 1000.0f;

        b2BodyId capsuleId;
        b2JointId jointId1;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(-10.0f, 20.0f);
            capsuleId = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, -1.0f), new b2Vec2(0.0f, 6.0f), 0.5f);
            b2CreateCapsuleShape(capsuleId, shapeDef, capsule);

            b2Vec2 pivot = new b2Vec2(-10.0f, 20.5f);
            b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
            jointDef.bodyIdA = groundId;
            jointDef.bodyIdB = capsuleId;
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            jointDef.targetAngle = B2_PI * targetDegrees / 180.0f;
            jointDef.enableSpring = enableSpring;
            jointDef.hertz = hertz;
            jointDef.dampingRatio = dampingRatio;
            jointDef.motorSpeed = motorSpeed;
            jointDef.maxMotorTorque = motorTorque;
            jointDef.enableMotor = enableMotor;
            jointDef.referenceAngle = 0.5f * B2_PI;
            jointDef.lowerAngle = -0.5f * B2_PI;
            jointDef.upperAngle = 0.75f * B2_PI;
            jointDef.enableLimit = enableLimit;
            jointId1 = b2CreateRevoluteJoint(worldId, jointDef);
        }

        b2BodyId ballId;
        {
            b2Circle circle = new b2Circle();
            circle.radius = 2.0f;

            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(5.0f, 30.0f);
            ballId = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            b2CreateCircleShape(ballId, shapeDef, circle);
        }

        b2BodyId leverId;
        b2JointId jointId2;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(20.0f, 10.0f);
            bodyDef.type = b2_dynamicBody;
            leverId = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeOffsetBox(10.0f, 0.5f, new b2Vec2(-10.0f, 0.0f), b2Rot_identity);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            b2CreatePolygonShape(leverId, shapeDef, box);

            b2Vec2 pivot = new b2Vec2(19.0f, 10.0f);
            b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
            jointDef.bodyIdA = groundId;
            jointDef.bodyIdB = leverId;
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            jointDef.lowerAngle = -0.25f * B2_PI;
            jointDef.upperAngle = 0.1f * B2_PI;
            jointDef.enableLimit = true;
            jointDef.enableMotor = true;
            jointDef.motorSpeed = 0.0f;
            jointDef.maxMotorTorque = motorTorque;
            jointId2 = b2CreateRevoluteJoint(worldId, jointDef);
        }

        SampleRuntime.toggle("revolute.limit", "Limit", enableLimit,
            value -> b2RevoluteJoint_EnableLimit(jointId1, value));
        SampleRuntime.toggle("revolute.motor", "Motor", enableMotor,
            value -> b2RevoluteJoint_EnableMotor(jointId1, value));
        SampleRuntime.slider("revolute.maxTorque", "Max Torque", motorTorque, 0.0f, 5000.0f, 50.0f,
            value -> b2RevoluteJoint_SetMaxMotorTorque(jointId1, value));
        SampleRuntime.slider("revolute.speed", "Speed", motorSpeed, -20.0f, 20.0f, 1.0f,
            value -> b2RevoluteJoint_SetMotorSpeed(jointId1, value));
        SampleRuntime.toggle("revolute.spring", "Spring", enableSpring,
            value -> b2RevoluteJoint_EnableSpring(jointId1, value));
        SampleRuntime.slider("revolute.hertz", "Hertz", hertz, 0.0f, 30.0f, 0.1f,
            value -> b2RevoluteJoint_SetSpringHertz(jointId1, value));
        SampleRuntime.slider("revolute.damping", "Damping", dampingRatio, 0.0f, 2.0f, 0.1f,
            value -> b2RevoluteJoint_SetSpringDampingRatio(jointId1, value));
        SampleRuntime.slider("revolute.degrees", "Degrees", 0.0f, -180.0f, 180.0f, 5.0f,
            value -> b2RevoluteJoint_SetTargetAngle(jointId1, value * B2_PI / 180.0f));

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), bodyState(capsuleId), bodyState(ballId), bodyState(leverId),
            jointState(jointId1), jointState(jointId2));
        b2DestroyWorld(worldId);
        return result;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, v.x, v.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    private static JointState jointState(b2JointId jointId) {
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        return new JointState(b2RevoluteJoint_GetAngle(jointId), b2RevoluteJoint_GetMotorTorque(jointId), force.x,
            force.y, b2Joint_GetConstraintTorque(jointId));
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final BodyState capsule;
        public final BodyState ball;
        public final BodyState lever;
        public final JointState joint1;
        public final JointState joint2;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, BodyState capsule,
            BodyState ball, BodyState lever, JointState joint1, JointState joint2) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.capsule = capsule;
            this.ball = ball;
            this.lever = lever;
            this.joint1 = joint1;
            this.joint2 = joint2;
        }

        public String toLine() {
            return "revoluteJoint " + bodyCount + " " + shapeCount + " " + contactCount + " " + jointCount + " "
                + awakeBodyCount + capsule.toLinePart() + ball.toLinePart() + lever.toLinePart()
                + joint1.toLinePart() + joint2.toLinePart();
        }
    }

    public static final class BodyState {
        public final float x;
        public final float y;
        public final float cos;
        public final float sin;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final int contactCapacity;

        BodyState(float x, float y, float cos, float sin, float velocityX, float velocityY, float angularVelocity,
            int contactCapacity) {
            this.x = x;
            this.y = y;
            this.cos = cos;
            this.sin = sin;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.contactCapacity = contactCapacity;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s %d", formatFloat(x), formatFloat(y),
                formatFloat(cos), formatFloat(sin), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), contactCapacity);
        }
    }

    public static final class JointState {
        public final float angle;
        public final float motorTorque;
        public final float forceX;
        public final float forceY;
        public final float torque;

        JointState(float angle, float motorTorque, float forceX, float forceY, float torque) {
            this.angle = angle;
            this.motorTorque = motorTorque;
            this.forceX = forceX;
            this.forceY = forceY;
            this.torque = torque;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s", formatFloat(angle), formatFloat(motorTorque),
                formatFloat(forceX), formatFloat(forceY), formatFloat(torque));
        }
    }

    private static String formatFloat(float value) {
        if (value == 0.0f) {
            return "0";
        }
        String text = String.format(Locale.ROOT, "%.9g", value);
        int exponent = Math.max(text.indexOf('e'), text.indexOf('E'));
        String suffix = "";
        if (exponent >= 0) {
            suffix = text.substring(exponent);
            text = text.substring(0, exponent);
        }
        if (text.indexOf('.') >= 0) {
            while (text.endsWith("0")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text + suffix;
    }
}
