package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
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

public final class Door {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final boolean DEFAULT_APPLY_IMPULSE = true;

    private Door() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT, DEFAULT_APPLY_IMPULSE);
    }

    public static Result run(int stepCount) {
        return run(stepCount, DEFAULT_APPLY_IMPULSE);
    }

    public static Result run(int stepCount, boolean applyImpulse) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef groundDef = b2DefaultBodyDef();
        groundDef.position = new b2Vec2(0.0f, 0.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundDef);

        boolean enableLimit = true;
        float impulse = 50000.0f;
        float[] translationError = {0.0f};
        float jointHertz = 240.0f;
        float jointDampingRatio = 1.0f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 1.5f);
        bodyDef.gravityScale = 0.0f;
        b2BodyId doorId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1000.0f;
        b2Polygon box = b2MakeBox(0.1f, 1.5f);
        b2CreatePolygonShape(doorId, shapeDef, box);

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = groundId;
        jointDef.bodyIdB = doorId;
        jointDef.localAnchorA = new b2Vec2(0.0f, 0.0f);
        jointDef.localAnchorB = new b2Vec2(0.0f, -1.5f);
        jointDef.targetAngle = 0.0f;
        jointDef.enableSpring = true;
        jointDef.hertz = 1.0f;
        jointDef.dampingRatio = 0.5f;
        jointDef.motorSpeed = 0.0f;
        jointDef.maxMotorTorque = 0.0f;
        jointDef.enableMotor = false;
        jointDef.referenceAngle = 0.0f;
        jointDef.lowerAngle = -0.5f * B2_PI;
        jointDef.upperAngle = 0.5f * B2_PI;
        jointDef.enableLimit = enableLimit;
        b2JointId jointId = b2CreateRevoluteJoint(worldId, jointDef);
        b2Joint_SetConstraintTuning(jointId, jointHertz, jointDampingRatio);
        float[] controls = {impulse, jointHertz, jointDampingRatio};
        SampleRuntime.action("door.impulse", "Impulse", () -> {
            applyDoorImpulse(doorId, controls[0]);
            translationError[0] = 0.0f;
        });
        SampleRuntime.slider("door.magnitude", "Magnitude", controls[0], 1000.0f, 100000.0f, 1000.0f,
            value -> controls[0] = value);
        SampleRuntime.toggle("door.limit", "Limit", enableLimit,
            value -> b2RevoluteJoint_EnableLimit(jointId, value));
        SampleRuntime.slider("door.hertz", "Hertz", controls[1], 15.0f, 480.0f, 5.0f, value -> {
            controls[1] = value;
            b2Joint_SetConstraintTuning(jointId, controls[1], controls[2]);
        });
        SampleRuntime.slider("door.damping", "Damping", controls[2], 0.0f, 10.0f, 0.1f, value -> {
            controls[2] = value;
            b2Joint_SetConstraintTuning(jointId, controls[1], controls[2]);
        });

        if (applyImpulse && !SampleRuntime.isActive()) {
            applyDoorImpulse(doorId, impulse);
        }

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            translationError[0] = b2MaxFloat(translationError[0], b2Joint_GetLinearSeparation(jointId));
        }

        b2Counters counters = b2World_GetCounters(worldId);
        BodyState bodyState = bodyState(doorId);
        JointState jointState = jointState(jointId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), bodyState, jointState, translationError[0]);
        b2DestroyWorld(worldId);
        return result;
    }

    private static void applyDoorImpulse(b2BodyId doorId, float impulse) {
        b2Vec2 point = b2Body_GetWorldPoint(doorId, new b2Vec2(0.0f, 1.5f));
        b2Body_ApplyLinearImpulse(doorId, new b2Vec2(impulse, 0.0f), point, true);
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x, velocity.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    private static JointState jointState(b2JointId jointId) {
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        float[] constraintHertz = {0.0f};
        float[] constraintDampingRatio = {0.0f};
        b2Joint_GetConstraintTuning(jointId, constraintHertz, constraintDampingRatio);
        return new JointState(b2RevoluteJoint_GetAngle(jointId), b2RevoluteJoint_GetTargetAngle(jointId),
            b2RevoluteJoint_IsSpringEnabled(jointId), b2RevoluteJoint_GetSpringHertz(jointId),
            b2RevoluteJoint_GetSpringDampingRatio(jointId), b2RevoluteJoint_IsLimitEnabled(jointId),
            b2RevoluteJoint_GetLowerLimit(jointId), b2RevoluteJoint_GetUpperLimit(jointId),
            b2Joint_GetLinearSeparation(jointId), b2Joint_GetAngularSeparation(jointId),
            b2RevoluteJoint_GetMotorTorque(jointId), b2RevoluteJoint_GetMaxMotorTorque(jointId), force.x, force.y,
            b2Joint_GetConstraintTorque(jointId), constraintHertz[0], constraintDampingRatio[0]);
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        boolean applyImpulse = args.length < 2 || Boolean.parseBoolean(args[1]);
        System.out.println(run(stepCount, applyImpulse).toLine());
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final BodyState body;
        public final JointState joint;
        public final float translationError;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, BodyState body,
               JointState joint, float translationError) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.body = body;
            this.joint = joint;
            this.translationError = translationError;
        }

        public String toLine() {
            return "door " + bodyCount + ' ' + shapeCount + ' ' + contactCount + ' ' + jointCount + ' '
                + awakeBodyCount + ' ' + formatFloat(translationError) + body.toLinePart() + joint.toLinePart();
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
        public final float targetAngle;
        public final boolean springEnabled;
        public final float springHertz;
        public final float springDampingRatio;
        public final boolean limitEnabled;
        public final float lowerLimit;
        public final float upperLimit;
        public final float linearSeparation;
        public final float angularSeparation;
        public final float motorTorque;
        public final float maxMotorTorque;
        public final float forceX;
        public final float forceY;
        public final float torque;
        public final float constraintHertz;
        public final float constraintDampingRatio;

        JointState(float angle, float targetAngle, boolean springEnabled, float springHertz, float springDampingRatio,
                   boolean limitEnabled, float lowerLimit, float upperLimit, float linearSeparation,
                   float angularSeparation, float motorTorque, float maxMotorTorque, float forceX, float forceY,
                   float torque, float constraintHertz, float constraintDampingRatio) {
            this.angle = angle;
            this.targetAngle = targetAngle;
            this.springEnabled = springEnabled;
            this.springHertz = springHertz;
            this.springDampingRatio = springDampingRatio;
            this.limitEnabled = limitEnabled;
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
            this.linearSeparation = linearSeparation;
            this.angularSeparation = angularSeparation;
            this.motorTorque = motorTorque;
            this.maxMotorTorque = maxMotorTorque;
            this.forceX = forceX;
            this.forceY = forceY;
            this.torque = torque;
            this.constraintHertz = constraintHertz;
            this.constraintDampingRatio = constraintDampingRatio;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %d %s %s %d %s %s %s %s %s %s %s %s %s %s %s",
                formatFloat(angle), formatFloat(targetAngle), springEnabled ? 1 : 0, formatFloat(springHertz),
                formatFloat(springDampingRatio), limitEnabled ? 1 : 0, formatFloat(lowerLimit), formatFloat(upperLimit),
                formatFloat(linearSeparation), formatFloat(angularSeparation), formatFloat(motorTorque),
                formatFloat(maxMotorTorque), formatFloat(forceX), formatFloat(forceY), formatFloat(torque),
                formatFloat(constraintHertz), formatFloat(constraintDampingRatio));
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
