package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2PrismaticJointDef;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Doohickey {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int COUNT = 4;
    private static final int BODIES_PER_DOOHICKEY = 4;
    private static final int JOINTS_PER_DOOHICKEY = 3;

    private Doohickey() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, groundShapeDef,
            new b2Segment(new b2Vec2(-20.0f, 0.0f), new b2Vec2(20.0f, 0.0f)));
        b2Polygon box = b2MakeOffsetBox(1.0f, 1.0f, new b2Vec2(0.0f, 1.0f), b2Rot_identity);
        b2CreatePolygonShape(groundId, groundShapeDef, box);

        Unit[] units = new Unit[COUNT];
        float y = 4.0f;
        for (int i = 0; i < COUNT; ++i) {
            units[i] = Unit.spawn(worldId, new b2Vec2(0.0f, y), 0.5f);
            y += 2.0f;
        }

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] bodies = new BodyState[COUNT * BODIES_PER_DOOHICKEY];
        JointState[] joints = new JointState[COUNT * JOINTS_PER_DOOHICKEY];
        int bodyIndex = 0;
        int jointIndex = 0;
        for (Unit unit : units) {
            for (b2BodyId bodyId : unit.bodyIds) {
                bodies[bodyIndex++] = bodyState(bodyId);
            }
            for (b2JointId jointId : unit.jointIds) {
                joints[jointIndex++] = jointState(jointId);
            }
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
        int type = b2Joint_GetType(jointId);
        float metricA = 0.0f;
        float metricB = 0.0f;
        float metricC = 0.0f;
        if (type == b2_revoluteJoint) {
            metricA = b2RevoluteJoint_GetAngle(jointId);
            metricB = b2RevoluteJoint_GetMotorTorque(jointId);
            metricC = b2RevoluteJoint_GetMaxMotorTorque(jointId);
        } else if (type == b2_prismaticJoint) {
            metricA = b2PrismaticJoint_GetTranslation(jointId);
            metricB = b2PrismaticJoint_GetMotorForce(jointId);
            metricC = b2PrismaticJoint_GetSpringHertz(jointId);
        }
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        return new JointState(type, b2Joint_GetLinearSeparation(jointId), metricA, metricB, metricC, force.x, force.y,
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
            builder.append("doohickey ")
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
        public final int type;
        public final float linearSeparation;
        public final float metricA;
        public final float metricB;
        public final float metricC;
        public final float forceX;
        public final float forceY;
        public final float torque;

        JointState(int type, float linearSeparation, float metricA, float metricB, float metricC, float forceX,
            float forceY, float torque) {
            this.type = type;
            this.linearSeparation = linearSeparation;
            this.metricA = metricA;
            this.metricB = metricB;
            this.metricC = metricC;
            this.forceX = forceX;
            this.forceY = forceY;
            this.torque = torque;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %d %s %s %s %s %s %s %s", type, formatFloat(linearSeparation),
                formatFloat(metricA), formatFloat(metricB), formatFloat(metricC), formatFloat(forceX), formatFloat(forceY),
                formatFloat(torque));
        }
    }

    private static final class Unit {
        final b2BodyId[] bodyIds = new b2BodyId[BODIES_PER_DOOHICKEY];
        final b2JointId[] jointIds = new b2JointId[JOINTS_PER_DOOHICKEY];

        static Unit spawn(b2WorldId worldId, b2Vec2 position, float scale) {
            Unit unit = new Unit();
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.material.rollingResistance = 0.1f;

            b2Circle circle = new b2Circle(new b2Vec2(0.0f, 0.0f), 1.0f * scale);
            b2Capsule capsule = new b2Capsule(new b2Vec2(-3.5f * scale, 0.0f),
                new b2Vec2(3.5f * scale, 0.0f), 0.15f * scale);

            bodyDef.position = b2MulAdd(position, scale, new b2Vec2(-5.0f, 3.0f));
            unit.bodyIds[0] = b2CreateBody(worldId, bodyDef);
            b2CreateCircleShape(unit.bodyIds[0], shapeDef, circle);

            bodyDef.position = b2MulAdd(position, scale, new b2Vec2(5.0f, 3.0f));
            unit.bodyIds[1] = b2CreateBody(worldId, bodyDef);
            b2CreateCircleShape(unit.bodyIds[1], shapeDef, circle);

            bodyDef.position = b2MulAdd(position, scale, new b2Vec2(-1.5f, 3.0f));
            unit.bodyIds[2] = b2CreateBody(worldId, bodyDef);
            b2CreateCapsuleShape(unit.bodyIds[2], shapeDef, capsule);

            bodyDef.position = b2MulAdd(position, scale, new b2Vec2(1.5f, 3.0f));
            unit.bodyIds[3] = b2CreateBody(worldId, bodyDef);
            b2CreateCapsuleShape(unit.bodyIds[3], shapeDef, capsule);

            b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
            revoluteDef.bodyIdA = unit.bodyIds[0];
            revoluteDef.bodyIdB = unit.bodyIds[2];
            revoluteDef.localAnchorA = new b2Vec2(0.0f, 0.0f);
            revoluteDef.localAnchorB = new b2Vec2(-3.5f * scale, 0.0f);
            revoluteDef.enableMotor = true;
            revoluteDef.maxMotorTorque = 2.0f * scale;
            unit.jointIds[0] = b2CreateRevoluteJoint(worldId, revoluteDef);

            revoluteDef.bodyIdA = unit.bodyIds[1];
            revoluteDef.bodyIdB = unit.bodyIds[3];
            revoluteDef.localAnchorA = new b2Vec2(0.0f, 0.0f);
            revoluteDef.localAnchorB = new b2Vec2(3.5f * scale, 0.0f);
            revoluteDef.enableMotor = true;
            revoluteDef.maxMotorTorque = 2.0f * scale;
            unit.jointIds[1] = b2CreateRevoluteJoint(worldId, revoluteDef);

            b2PrismaticJointDef prismaticDef = b2DefaultPrismaticJointDef();
            prismaticDef.bodyIdA = unit.bodyIds[2];
            prismaticDef.bodyIdB = unit.bodyIds[3];
            prismaticDef.localAxisA = new b2Vec2(1.0f, 0.0f);
            prismaticDef.localAnchorA = new b2Vec2(2.0f * scale, 0.0f);
            prismaticDef.localAnchorB = new b2Vec2(-2.0f * scale, 0.0f);
            prismaticDef.lowerTranslation = -2.0f * scale;
            prismaticDef.upperTranslation = 2.0f * scale;
            prismaticDef.enableLimit = true;
            prismaticDef.enableMotor = true;
            prismaticDef.maxMotorForce = 2.0f * scale;
            prismaticDef.enableSpring = true;
            prismaticDef.hertz = 1.0f;
            prismaticDef.dampingRatio = 0.5f;
            unit.jointIds[2] = b2CreatePrismaticJoint(worldId, prismaticDef);
            return unit;
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
