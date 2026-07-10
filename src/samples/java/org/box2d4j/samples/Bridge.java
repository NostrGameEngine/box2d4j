package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Hull;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Bridge {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int COUNT = 160;

    private Bridge() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());

        float springHertz = 2.0f;
        float springDampingRatio = 0.7f;
        float frictionTorque = 200.0f;

        b2BodyId[] bodies = new b2BodyId[COUNT + 5];
        b2JointId[] joints = new b2JointId[COUNT + 1];
        int bodyIndex = 0;
        int jointIndex = 0;

        {
            b2Polygon box = b2MakeBox(0.5f, 0.125f);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 20.0f;

            b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
            jointDef.enableMotor = true;
            jointDef.maxMotorTorque = frictionTorque;
            jointDef.enableSpring = true;
            jointDef.hertz = springHertz;
            jointDef.dampingRatio = springDampingRatio;

            float xbase = -80.0f;
            b2BodyId prevBodyId = groundId;
            for (int i = 0; i < COUNT; ++i) {
                b2BodyDef bodyDef = b2DefaultBodyDef();
                bodyDef.type = b2_dynamicBody;
                bodyDef.position = new b2Vec2(xbase + 0.5f + 1.0f * i, 20.0f);
                bodyDef.linearDamping = 0.1f;
                bodyDef.angularDamping = 0.1f;

                b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
                bodies[bodyIndex++] = bodyId;
                b2CreatePolygonShape(bodyId, shapeDef, box);

                b2Vec2 pivot = new b2Vec2(xbase + 1.0f * i, 20.0f);
                jointDef.bodyIdA = prevBodyId;
                jointDef.bodyIdB = bodyId;
                jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
                jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
                joints[jointIndex++] = b2CreateRevoluteJoint(worldId, jointDef);

                prevBodyId = bodyId;
            }

            b2Vec2 pivot = new b2Vec2(xbase + 1.0f * COUNT, 20.0f);
            jointDef.bodyIdA = prevBodyId;
            jointDef.bodyIdB = groundId;
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            joints[jointIndex++] = b2CreateRevoluteJoint(worldId, jointDef);
        }

        for (int i = 0; i < 2; ++i) {
            b2Vec2[] vertices = {
                new b2Vec2(-0.5f, 0.0f),
                new b2Vec2(0.5f, 0.0f),
                new b2Vec2(0.0f, 1.5f)
            };
            b2Hull hull = b2ComputeHull(vertices, 3);
            b2Polygon triangle = b2MakePolygon(hull, 0.0f);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 20.0f;

            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(-8.0f + 8.0f * i, 22.0f);
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            bodies[bodyIndex++] = bodyId;
            b2CreatePolygonShape(bodyId, shapeDef, triangle);
        }

        for (int i = 0; i < 3; ++i) {
            b2Circle circle = new b2Circle(new b2Vec2(0.0f, 0.0f), 0.5f);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 20.0f;

            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(-6.0f + 6.0f * i, 25.0f);
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            bodies[bodyIndex++] = bodyId;
            b2CreateCircleShape(bodyId, shapeDef, circle);
        }
        float[] tuning = {60.0f, 2.0f};
        SampleRuntime.slider("bridge.friction", "Joint Friction", frictionTorque, 0.0f, 10000.0f, 100.0f,
            value -> forEach(joints, joint -> b2RevoluteJoint_SetMaxMotorTorque(joint, value)));
        SampleRuntime.slider("bridge.springHertz", "Spring Hertz", springHertz, 0.0f, 30.0f, 1.0f,
            value -> forEach(joints, joint -> b2RevoluteJoint_SetSpringHertz(joint, value)));
        SampleRuntime.slider("bridge.springDamping", "Spring Damping", springDampingRatio, 0.0f, 2.0f, 0.1f,
            value -> forEach(joints, joint -> b2RevoluteJoint_SetSpringDampingRatio(joint, value)));
        SampleRuntime.slider("bridge.constraintHertz", "Constraint Hertz", tuning[0], 15.0f, 240.0f, 5.0f, value -> {
            tuning[0] = value;
            forEach(joints, joint -> b2Joint_SetConstraintTuning(joint, tuning[0], tuning[1]));
        });
        SampleRuntime.slider("bridge.constraintDamping", "Constraint Damping", tuning[1], 0.0f, 10.0f, 0.1f,
            value -> {
                tuning[1] = value;
                forEach(joints, joint -> b2Joint_SetConstraintTuning(joint, tuning[0], tuning[1]));
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
            builder.append("bridge ")
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
