package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Counters;
import org.box2d4j.b2JointId;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WheelJointDef;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class WheelJoint {
    private static final int DEFAULT_STEP_COUNT = 120;

    private WheelJoint() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());

        boolean enableSpring = true;
        boolean enableLimit = true;
        boolean enableMotor = true;
        float motorSpeed = 2.0f;
        float motorTorque = 5.0f;
        float hertz = 1.0f;
        float dampingRatio = 0.7f;

        b2BodyId bodyId;
        b2JointId jointId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(0.0f, 10.25f);
            bodyDef.type = b2_dynamicBody;
            bodyId = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, -0.5f), new b2Vec2(0.0f, 0.5f), 0.5f);
            b2CreateCapsuleShape(bodyId, shapeDef, capsule);

            b2Vec2 pivot = new b2Vec2(0.0f, 10.0f);
            b2Vec2 axis = b2Normalize(new b2Vec2(1.0f, 1.0f));
            b2WheelJointDef jointDef = b2DefaultWheelJointDef();
            jointDef.bodyIdA = groundId;
            jointDef.bodyIdB = bodyId;
            jointDef.localAxisA = b2Body_GetLocalVector(jointDef.bodyIdA, axis);
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            jointDef.motorSpeed = motorSpeed;
            jointDef.maxMotorTorque = motorTorque;
            jointDef.enableMotor = enableMotor;
            jointDef.lowerTranslation = -3.0f;
            jointDef.upperTranslation = 3.0f;
            jointDef.enableLimit = enableLimit;
            jointDef.enableSpring = enableSpring;
            jointDef.hertz = hertz;
            jointDef.dampingRatio = dampingRatio;
            jointId = b2CreateWheelJoint(worldId, jointDef);
        }
        SampleRuntime.toggle("wheel.limit", "Limit", enableLimit,
            value -> b2WheelJoint_EnableLimit(jointId, value));
        SampleRuntime.toggle("wheel.motor", "Motor", enableMotor,
            value -> b2WheelJoint_EnableMotor(jointId, value));
        SampleRuntime.slider("wheel.torque", "Torque", motorTorque, 0.0f, 20.0f, 1.0f,
            value -> b2WheelJoint_SetMaxMotorTorque(jointId, value));
        SampleRuntime.slider("wheel.speed", "Speed", motorSpeed, -20.0f, 20.0f, 1.0f,
            value -> b2WheelJoint_SetMotorSpeed(jointId, value));
        SampleRuntime.toggle("wheel.spring", "Spring", enableSpring,
            value -> b2WheelJoint_EnableSpring(jointId, value));
        SampleRuntime.slider("wheel.hertz", "Hertz", hertz, 0.0f, 10.0f, 0.1f,
            value -> b2WheelJoint_SetSpringHertz(jointId, value));
        SampleRuntime.slider("wheel.damping", "Damping", dampingRatio, 0.0f, 2.0f, 0.1f,
            value -> b2WheelJoint_SetSpringDampingRatio(jointId, value));

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), bodyState(bodyId), jointState(jointId));
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
        return new JointState(b2Joint_GetLinearSeparation(jointId), b2WheelJoint_GetMotorTorque(jointId), force.x,
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
        public final BodyState body;
        public final JointState joint;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, BodyState body,
            JointState joint) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.body = body;
            this.joint = joint;
        }

        public String toLine() {
            return "wheelJoint " + bodyCount + " " + shapeCount + " " + contactCount + " " + jointCount + " "
                + awakeBodyCount + body.toLinePart() + joint.toLinePart();
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
        public final float linearSeparation;
        public final float motorTorque;
        public final float forceX;
        public final float forceY;
        public final float torque;

        JointState(float linearSeparation, float motorTorque, float forceX, float forceY, float torque) {
            this.linearSeparation = linearSeparation;
            this.motorTorque = motorTorque;
            this.forceX = forceX;
            this.forceY = forceY;
            this.torque = torque;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s", formatFloat(linearSeparation),
                formatFloat(motorTorque), formatFloat(forceX), formatFloat(forceY), formatFloat(torque));
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
