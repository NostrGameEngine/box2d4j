package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2DistanceJointDef;
import org.box2d4j.b2JointId;
import org.box2d4j.b2MotorJointDef;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2PrismaticJointDef;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WeldJointDef;
import org.box2d4j.b2WheelJointDef;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class FixedRotation {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int COUNT = 6;

    private FixedRotation() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        boolean fixedRotation = true;

        b2Vec2 position = new b2Vec2(-12.5f, 10.0f);
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.fixedRotation = fixedRotation;

        b2Polygon box = b2MakeBox(1.0f, 1.0f);
        b2BodyId[] bodies = new b2BodyId[COUNT];
        b2JointId[] joints = new b2JointId[COUNT];
        int index = 0;

        {
            bodyDef.position = new b2Vec2(position);
            bodies[index] = b2CreateBody(worldId, bodyDef);
            b2CreatePolygonShape(bodies[index], b2DefaultShapeDef(), box);

            float length = 2.0f;
            b2Vec2 pivot1 = new b2Vec2(position.x, position.y + 1.0f + length);
            b2Vec2 pivot2 = new b2Vec2(position.x, position.y + 1.0f);
            b2DistanceJointDef jointDef = b2DefaultDistanceJointDef();
            jointDef.bodyIdA = groundId;
            jointDef.bodyIdB = bodies[index];
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot1);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot2);
            jointDef.length = length;
            joints[index] = b2CreateDistanceJoint(worldId, jointDef);
        }

        position.x += 5.0f;
        ++index;
        {
            bodyDef.position = new b2Vec2(position);
            bodies[index] = b2CreateBody(worldId, bodyDef);
            b2CreatePolygonShape(bodies[index], b2DefaultShapeDef(), box);

            b2MotorJointDef jointDef = b2DefaultMotorJointDef();
            jointDef.bodyIdA = groundId;
            jointDef.bodyIdB = bodies[index];
            jointDef.linearOffset = new b2Vec2(position);
            jointDef.maxForce = 200.0f;
            jointDef.maxTorque = 20.0f;
            joints[index] = b2CreateMotorJoint(worldId, jointDef);
        }

        position.x += 5.0f;
        ++index;
        {
            bodyDef.position = new b2Vec2(position);
            bodies[index] = b2CreateBody(worldId, bodyDef);
            b2CreatePolygonShape(bodies[index], b2DefaultShapeDef(), box);

            b2Vec2 pivot = new b2Vec2(position.x - 1.0f, position.y);
            b2PrismaticJointDef jointDef = b2DefaultPrismaticJointDef();
            jointDef.bodyIdA = groundId;
            jointDef.bodyIdB = bodies[index];
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            jointDef.localAxisA = b2Body_GetLocalVector(jointDef.bodyIdA, new b2Vec2(1.0f, 0.0f));
            joints[index] = b2CreatePrismaticJoint(worldId, jointDef);
        }

        position.x += 5.0f;
        ++index;
        {
            bodyDef.position = new b2Vec2(position);
            bodies[index] = b2CreateBody(worldId, bodyDef);
            b2CreatePolygonShape(bodies[index], b2DefaultShapeDef(), box);

            b2Vec2 pivot = new b2Vec2(position.x - 1.0f, position.y);
            b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
            jointDef.bodyIdA = groundId;
            jointDef.bodyIdB = bodies[index];
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            joints[index] = b2CreateRevoluteJoint(worldId, jointDef);
        }

        position.x += 5.0f;
        ++index;
        {
            bodyDef.position = new b2Vec2(position);
            bodies[index] = b2CreateBody(worldId, bodyDef);
            b2CreatePolygonShape(bodies[index], b2DefaultShapeDef(), box);

            b2Vec2 pivot = new b2Vec2(position.x - 1.0f, position.y);
            b2WeldJointDef jointDef = b2DefaultWeldJointDef();
            jointDef.bodyIdA = groundId;
            jointDef.bodyIdB = bodies[index];
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            jointDef.angularHertz = 1.0f;
            jointDef.angularDampingRatio = 0.5f;
            jointDef.linearHertz = 1.0f;
            jointDef.linearDampingRatio = 0.5f;
            joints[index] = b2CreateWeldJoint(worldId, jointDef);
        }

        position.x += 5.0f;
        ++index;
        {
            bodyDef.position = new b2Vec2(position);
            bodies[index] = b2CreateBody(worldId, bodyDef);
            b2CreatePolygonShape(bodies[index], b2DefaultShapeDef(), box);

            b2Vec2 pivot = new b2Vec2(position.x - 1.0f, position.y);
            b2WheelJointDef jointDef = b2DefaultWheelJointDef();
            jointDef.bodyIdA = groundId;
            jointDef.bodyIdB = bodies[index];
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            jointDef.localAxisA = b2Body_GetLocalVector(jointDef.bodyIdA, new b2Vec2(1.0f, 0.0f));
            jointDef.hertz = 1.0f;
            jointDef.dampingRatio = 0.7f;
            jointDef.lowerTranslation = -1.0f;
            jointDef.upperTranslation = 1.0f;
            jointDef.enableLimit = true;
            jointDef.enableMotor = true;
            jointDef.maxMotorTorque = 10.0f;
            jointDef.motorSpeed = 1.0f;
            joints[index] = b2CreateWheelJoint(worldId, jointDef);
        }
        SampleRuntime.toggle("fixedRotation.enabled", "Fixed Rotation", fixedRotation, value -> {
            for (b2BodyId body : bodies) {
                b2Body_SetFixedRotation(body, value);
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

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, v.x, v.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetContactCapacity(bodyId), b2Body_IsFixedRotation(bodyId));
    }

    private static JointState jointState(b2JointId jointId) {
        int type = b2Joint_GetType(jointId);
        float metricA = 0.0f;
        float metricB = 0.0f;
        if (type == b2_distanceJoint) {
            metricA = b2DistanceJoint_GetCurrentLength(jointId);
            metricB = b2DistanceJoint_GetMotorForce(jointId);
        } else if (type == b2_motorJoint) {
            b2Vec2 linearOffset = b2MotorJoint_GetLinearOffset(jointId);
            metricA = linearOffset.x;
            metricB = linearOffset.y;
        } else if (type == b2_prismaticJoint) {
            metricA = b2PrismaticJoint_GetTranslation(jointId);
            metricB = b2PrismaticJoint_GetSpeed(jointId);
        } else if (type == b2_revoluteJoint) {
            metricA = b2RevoluteJoint_GetAngle(jointId);
            metricB = b2RevoluteJoint_GetMotorTorque(jointId);
        } else if (type == b2_weldJoint) {
            metricA = b2WeldJoint_GetLinearHertz(jointId);
            metricB = b2WeldJoint_GetAngularHertz(jointId);
        } else if (type == b2_wheelJoint) {
            metricA = b2WheelJoint_GetMotorTorque(jointId);
            metricB = b2WheelJoint_GetMotorSpeed(jointId);
        }
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        return new JointState(type, b2Joint_GetLinearSeparation(jointId), metricA, metricB, force.x, force.y,
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
            builder.append("fixedRotation ")
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
        public final boolean fixedRotation;

        BodyState(float x, float y, float cos, float sin, float velocityX, float velocityY, float angularVelocity,
            int contactCapacity, boolean fixedRotation) {
            this.x = x;
            this.y = y;
            this.cos = cos;
            this.sin = sin;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.contactCapacity = contactCapacity;
            this.fixedRotation = fixedRotation;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s %d %d", formatFloat(x), formatFloat(y),
                formatFloat(cos), formatFloat(sin), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), contactCapacity, fixedRotation ? 1 : 0);
        }
    }

    public static final class JointState {
        public final int type;
        public final float linearSeparation;
        public final float metricA;
        public final float metricB;
        public final float forceX;
        public final float forceY;
        public final float torque;

        JointState(int type, float linearSeparation, float metricA, float metricB, float forceX, float forceY,
            float torque) {
            this.type = type;
            this.linearSeparation = linearSeparation;
            this.metricA = metricA;
            this.metricB = metricB;
            this.forceX = forceX;
            this.forceY = forceY;
            this.torque = torque;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %d %s %s %s %s %s %s", type, formatFloat(linearSeparation),
                formatFloat(metricA), formatFloat(metricB), formatFloat(forceX), formatFloat(forceY),
                formatFloat(torque));
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
