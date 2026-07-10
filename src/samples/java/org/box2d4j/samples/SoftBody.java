package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Counters;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WeldJointDef;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class SoftBody {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int SIDES = 7;

    private SoftBody() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreateSegmentShape(groundId, b2DefaultShapeDef(),
            new b2Segment(new b2Vec2(-20.0f, 0.0f), new b2Vec2(20.0f, 0.0f)));

        Donut donut = Donut.create(worldId, new b2Vec2(0.0f, 10.0f), 2.0f, 0, false);

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] bodies = new BodyState[SIDES];
        JointState[] joints = new JointState[SIDES];
        for (int i = 0; i < SIDES; ++i) {
            bodies[i] = bodyState(donut.bodyIds[i]);
            joints[i] = jointState(donut.jointIds[i]);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), bodies, joints);
        b2DestroyWorld(worldId);
        return result;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x, velocity.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    private static JointState jointState(b2JointId jointId) {
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        return new JointState(b2Joint_GetLinearSeparation(jointId), b2Joint_GetAngularSeparation(jointId),
            b2WeldJoint_GetLinearHertz(jointId), b2WeldJoint_GetAngularHertz(jointId), force.x, force.y,
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
            builder.append("softBody ")
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
        public final float linearSeparation;
        public final float angularSeparation;
        public final float linearHertz;
        public final float angularHertz;
        public final float forceX;
        public final float forceY;
        public final float torque;

        JointState(float linearSeparation, float angularSeparation, float linearHertz, float angularHertz, float forceX,
            float forceY, float torque) {
            this.linearSeparation = linearSeparation;
            this.angularSeparation = angularSeparation;
            this.linearHertz = linearHertz;
            this.angularHertz = angularHertz;
            this.forceX = forceX;
            this.forceY = forceY;
            this.torque = torque;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s", formatFloat(linearSeparation),
                formatFloat(angularSeparation), formatFloat(linearHertz), formatFloat(angularHertz), formatFloat(forceX),
                formatFloat(forceY), formatFloat(torque));
        }
    }

    private static final class Donut {
        final b2BodyId[] bodyIds = new b2BodyId[SIDES];
        final b2JointId[] jointIds = new b2JointId[SIDES];

        static Donut create(b2WorldId worldId, b2Vec2 position, float scale, int groupIndex, boolean enableSensorEvents) {
            Donut donut = new Donut();
            float radius = 1.0f * scale;
            float deltaAngle = 2.0f * B2_PI / SIDES;
            float length = 2.0f * B2_PI * radius / SIDES;
            b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, -0.5f * length),
                new b2Vec2(0.0f, 0.5f * length), 0.25f * scale);

            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.enableSensorEvents = enableSensorEvents;
            shapeDef.filter.groupIndex = -groupIndex;
            shapeDef.material.friction = 0.3f;

            float angle = 0.0f;
            for (int i = 0; i < SIDES; ++i) {
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);
                bodyDef.position = new b2Vec2(radius * cos + position.x, radius * sin + position.y);
                bodyDef.rotation = b2MakeRot(angle);
                donut.bodyIds[i] = b2CreateBody(worldId, bodyDef);
                b2CreateCapsuleShape(donut.bodyIds[i], shapeDef, capsule);
                angle += deltaAngle;
            }

            b2WeldJointDef weldDef = b2DefaultWeldJointDef();
            weldDef.angularHertz = 5.0f;
            weldDef.angularDampingRatio = 0.0f;
            weldDef.localAnchorA = new b2Vec2(0.0f, 0.5f * length);
            weldDef.localAnchorB = new b2Vec2(0.0f, -0.5f * length);
            b2BodyId prevBodyId = donut.bodyIds[SIDES - 1];
            for (int i = 0; i < SIDES; ++i) {
                weldDef.bodyIdA = prevBodyId;
                weldDef.bodyIdB = donut.bodyIds[i];
                weldDef.referenceAngle = b2RelativeAngle(b2Body_GetRotation(donut.bodyIds[i]), b2Body_GetRotation(prevBodyId));
                donut.jointIds[i] = b2CreateWeldJoint(worldId, weldDef);
                prevBodyId = weldDef.bodyIdB;
            }
            return donut;
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
