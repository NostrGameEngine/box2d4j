package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Counters;
import org.box2d4j.b2JointId;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WeldJointDef;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Cantilever {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int COUNT = 8;

    private Cantilever() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());

        float linearHertz = 15.0f;
        float linearDampingRatio = 0.5f;
        float angularHertz = 5.0f;
        float angularDampingRatio = 0.5f;
        boolean collideConnected = false;

        float hx = 0.5f;
        b2Capsule capsule = new b2Capsule(new b2Vec2(-hx, 0.0f), new b2Vec2(hx, 0.0f), 0.125f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 20.0f;

        b2WeldJointDef jointDef = b2DefaultWeldJointDef();
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.isAwake = false;

        b2BodyId[] bodies = new b2BodyId[COUNT];
        b2JointId[] joints = new b2JointId[COUNT];
        b2BodyId prevBodyId = groundId;
        for (int i = 0; i < COUNT; ++i) {
            bodyDef.position = new b2Vec2((1.0f + 2.0f * i) * hx, 0.0f);
            bodies[i] = b2CreateBody(worldId, bodyDef);
            b2CreateCapsuleShape(bodies[i], shapeDef, capsule);

            b2Vec2 pivot = new b2Vec2((2.0f * i) * hx, 0.0f);
            jointDef.bodyIdA = prevBodyId;
            jointDef.bodyIdB = bodies[i];
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            jointDef.linearHertz = linearHertz;
            jointDef.linearDampingRatio = linearDampingRatio;
            jointDef.angularHertz = angularHertz;
            jointDef.angularDampingRatio = angularDampingRatio;
            jointDef.collideConnected = collideConnected;
            joints[i] = b2CreateWeldJoint(worldId, jointDef);

            prevBodyId = bodies[i];
        }
        SampleRuntime.slider("cantilever.linearHertz", "Linear Hertz", linearHertz, 0.0f, 20.0f, 1.0f,
            value -> forEach(joints, joint -> b2WeldJoint_SetLinearHertz(joint, value)));
        SampleRuntime.slider("cantilever.linearDamping", "Linear Damping", linearDampingRatio, 0.0f, 10.0f, 0.1f,
            value -> forEach(joints, joint -> b2WeldJoint_SetLinearDampingRatio(joint, value)));
        SampleRuntime.slider("cantilever.angularHertz", "Angular Hertz", angularHertz, 0.0f, 20.0f, 1.0f,
            value -> forEach(joints, joint -> b2WeldJoint_SetAngularHertz(joint, value)));
        SampleRuntime.slider("cantilever.angularDamping", "Angular Damping", angularDampingRatio,
            0.0f, 10.0f, 0.1f,
            value -> forEach(joints, joint -> b2WeldJoint_SetAngularDampingRatio(joint, value)));
        SampleRuntime.toggle("cantilever.collide", "Collide Connected", collideConnected,
            value -> forEach(joints, joint -> b2Joint_SetCollideConnected(joint, value)));
        SampleRuntime.slider("cantilever.gravityScale", "Gravity Scale", 1.0f, -1.0f, 1.0f, 0.1f,
            value -> {
                for (b2BodyId body : bodies) {
                    b2Body_SetGravityScale(body, value);
                }
            });

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] bodyStates = new BodyState[bodies.length];
        for (int i = 0; i < bodies.length; ++i) {
            bodyStates[i] = bodyState(bodies[i]);
        }
        JointState[] jointStates = new JointState[joints.length];
        for (int i = 0; i < joints.length; ++i) {
            jointStates[i] = jointState(joints[i]);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), bodyStates, jointStates);
        b2DestroyWorld(worldId);
        return result;
    }

    private static void forEach(b2JointId[] joints, java.util.function.Consumer<b2JointId> action) {
        for (b2JointId joint : joints) {
            action.accept(joint);
        }
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, v.x, v.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    private static JointState jointState(b2JointId jointId) {
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        return new JointState(b2WeldJoint_GetLinearHertz(jointId), b2WeldJoint_GetLinearDampingRatio(jointId),
            b2WeldJoint_GetAngularHertz(jointId), b2WeldJoint_GetAngularDampingRatio(jointId), force.x, force.y,
            b2Joint_GetConstraintTorque(jointId));
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
        public final BodyState[] bodies;
        public final JointState[] joints;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, BodyState[] bodies,
            JointState[] joints) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.bodies = bodies;
            this.joints = joints;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("cantilever ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(jointCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(bodies.length).append(' ')
                .append(joints.length);
            for (BodyState body : bodies) {
                builder.append(body.toLinePart());
            }
            for (JointState joint : joints) {
                builder.append(joint.toLinePart());
            }
            return builder.toString();
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
        public final float linearHertz;
        public final float linearDampingRatio;
        public final float angularHertz;
        public final float angularDampingRatio;
        public final float forceX;
        public final float forceY;
        public final float torque;

        JointState(float linearHertz, float linearDampingRatio, float angularHertz, float angularDampingRatio,
            float forceX, float forceY, float torque) {
            this.linearHertz = linearHertz;
            this.linearDampingRatio = linearDampingRatio;
            this.angularHertz = angularHertz;
            this.angularDampingRatio = angularDampingRatio;
            this.forceX = forceX;
            this.forceY = forceY;
            this.torque = torque;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s", formatFloat(linearHertz),
                formatFloat(linearDampingRatio), formatFloat(angularHertz), formatFloat(angularDampingRatio),
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
