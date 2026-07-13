package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2JointId;
import org.box2d4j.b2MotorJointDef;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class MotorJoint {
    private static final int DEFAULT_STEP_COUNT = 110;

    private MotorJoint() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        return run(stepCount, false);
    }

    public static Result runDeterministic(int stepCount) {
        return run(stepCount, true);
    }

    private static Result run(int stepCount, boolean deterministicTarget) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyId groundId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            groundId = b2CreateBody(worldId, bodyDef);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2Segment segment = new b2Segment(new b2Vec2(-20.0f, 0.0f), new b2Vec2(20.0f, 0.0f));
            b2CreateSegmentShape(groundId, shapeDef, segment);
        }

        b2BodyId bodyId;
        b2JointId jointId;
        float maxForce = 500.0f;
        float maxTorque = 500.0f;
        float correctionFactor = 0.3f;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(0.0f, 8.0f);
            bodyId = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeBox(2.0f, 0.5f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            b2CreatePolygonShape(bodyId, shapeDef, box);

            b2MotorJointDef jointDef = b2DefaultMotorJointDef();
            jointDef.bodyIdA = groundId;
            jointDef.bodyIdB = bodyId;
            jointDef.maxForce = maxForce;
            jointDef.maxTorque = maxTorque;
            jointDef.correctionFactor = correctionFactor;
            jointId = b2CreateMotorJoint(worldId, jointDef);
        }

        RuntimeState state = new RuntimeState();
        boolean interactive = SampleRuntime.isActive();
        SampleRuntime.toggle("motorJoint.go", "Go", true, value -> state.go = value);
        SampleRuntime.slider("motorJoint.maxForce", "Max Force", maxForce, 0.0f, 10000.0f, 100.0f,
            value -> b2MotorJoint_SetMaxForce(jointId, value));
        SampleRuntime.slider("motorJoint.maxTorque", "Max Torque", maxTorque, 0.0f, 10000.0f, 100.0f,
            value -> b2MotorJoint_SetMaxTorque(jointId, value));
        SampleRuntime.slider("motorJoint.correction", "Correction", correctionFactor, 0.0f, 1.0f, 0.1f,
            value -> b2MotorJoint_SetCorrectionFactor(jointId, value));
        SampleRuntime.action("motorJoint.impulse", "Apply Impulse",
            () -> b2Body_ApplyLinearImpulseToCenter(bodyId, new b2Vec2(100.0f, 0.0f), true));
        SampleRuntime.beforeStep(() -> updateTarget(state, jointId, deterministicTarget));

        b2Vec2 linearOffset = new b2Vec2();
        float angularOffset = 0.0f;
        for (int step = 0; step < stepCount; ++step) {
            if (!interactive) {
                updateTarget(state, jointId, deterministicTarget);
            }
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }
        linearOffset = state.linearOffset;
        angularOffset = state.angularOffset;

        b2Counters counters = b2World_GetCounters(worldId);
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), state.time, linearOffset.x, linearOffset.y, angularOffset,
            b2Joint_GetConstraintTorque(jointId), force.x, force.y, bodyState(bodyId));
        b2DestroyWorld(worldId);
        return result;
    }

    private static void updateTarget(RuntimeState state, b2JointId jointId, boolean deterministicTarget) {
        if (state.go) {
            state.time += 1.0f / 60.0f;
        }
        float sin2;
        float sin1;
        if (deterministicTarget) {
            sin2 = b2ComputeCosSin(2.0f * state.time).sine;
            sin1 = b2ComputeCosSin(state.time).sine;
        } else {
            sin2 = (float) Math.sin(2.0f * state.time);
            sin1 = (float) Math.sin(state.time);
        }
        state.linearOffset = new b2Vec2(6.0f * sin2, 8.0f + 4.0f * sin1);
        state.angularOffset = 2.0f * state.time;
        b2MotorJoint_SetLinearOffset(jointId, state.linearOffset);
        b2MotorJoint_SetAngularOffset(jointId, state.angularOffset);
    }

    private static final class RuntimeState {
        boolean go = true;
        float time;
        b2Vec2 linearOffset = new b2Vec2();
        float angularOffset;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, v.x, v.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetContactCapacity(bodyId));
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
        public final float time;
        public final float targetX;
        public final float targetY;
        public final float angularOffset;
        public final float torque;
        public final float forceX;
        public final float forceY;
        public final BodyState body;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, float time,
            float targetX, float targetY, float angularOffset, float torque, float forceX, float forceY,
            BodyState body) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.time = time;
            this.targetX = targetX;
            this.targetY = targetY;
            this.angularOffset = angularOffset;
            this.torque = torque;
            this.forceX = forceX;
            this.forceY = forceY;
            this.body = body;
        }

        public String toLine() {
            return String.format(Locale.ROOT, "motorJoint %d %d %d %d %d %s %s %s %s %s %s %s%s",
                bodyCount, shapeCount, contactCount, jointCount, awakeBodyCount, formatFloat(time),
                formatFloat(targetX), formatFloat(targetY), formatFloat(angularOffset), formatFloat(forceX),
                formatFloat(forceY), formatFloat(torque), body.toLinePart());
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
