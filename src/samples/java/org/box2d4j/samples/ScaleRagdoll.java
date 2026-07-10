package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ScaleRagdoll {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int BONE_COUNT = 11;
    private static final float DEFAULT_SCALE = 1.0f;
    private static final float SCRIPTED_SCALE = 1.75f;

    private ScaleRagdoll() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT, SCRIPTED_SCALE);
    }

    public static Result run(int stepCount) {
        return run(stepCount, SCRIPTED_SCALE);
    }

    public static Result run(int stepCount, float scriptedScale) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon box = b2MakeOffsetBox(20.0f, 1.0f, new b2Vec2(0.0f, -1.0f), b2Rot_identity);
        b2CreatePolygonShape(groundId, shapeDef, box);

        Ragdoll.Human human = Ragdoll.Human.create(worldId, new b2Vec2(0.0f, 5.0f), DEFAULT_SCALE, 0.03f, 1.0f,
            0.5f, 1);
        human.applyRandomAngularImpulse(10.0f);
        boolean interactive = SampleRuntime.isActive();
        float initialScale = interactive ? DEFAULT_SCALE : scriptedScale;
        human.setScale(initialScale);
        float[] currentScale = {initialScale};
        SampleRuntime.slider("scaleRagdoll.scale", "Scale", initialScale, 0.1f, 10.0f, 0.1f, value -> {
            human.setScale(value);
            currentScale[0] = value;
        });

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] bodies = new BodyState[BONE_COUNT];
        JointState[] joints = new JointState[BONE_COUNT - 1];
        for (int i = 0; i < BONE_COUNT; ++i) {
            bodies[i] = bodyState(human.bodies[i]);
            if (i > 0) {
                joints[i - 1] = jointState(human.joints[i]);
            }
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), currentScale[0], bodies, joints);
        b2DestroyWorld(worldId);
        return result;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x, velocity.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetShapeCount(bodyId), b2Body_GetContactCapacity(bodyId),
            b2Body_GetMass(bodyId), b2Body_GetRotationalInertia(bodyId));
    }

    private static JointState jointState(b2JointId jointId) {
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        return new JointState(b2RevoluteJoint_GetAngle(jointId), b2RevoluteJoint_GetMotorTorque(jointId),
            b2RevoluteJoint_GetMaxMotorTorque(jointId), b2RevoluteJoint_GetSpringHertz(jointId),
            b2RevoluteJoint_GetSpringDampingRatio(jointId), b2Joint_GetLinearSeparation(jointId),
            b2Joint_GetAngularSeparation(jointId), force.x, force.y, b2Joint_GetConstraintTorque(jointId));
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        float scale = args.length < 2 ? SCRIPTED_SCALE : Float.parseFloat(args[1]);
        System.out.println(run(stepCount, scale).toLine());
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final float scale;
        public final BodyState[] bodies;
        public final JointState[] joints;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, float scale,
               BodyState[] bodies, JointState[] joints) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.scale = scale;
            this.bodies = bodies;
            this.joints = joints;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("scaleRagdoll ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(jointCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(formatFloat(scale)).append(' ')
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
        public final int shapeCount;
        public final int contactCapacity;
        public final float mass;
        public final float inertia;

        BodyState(float x, float y, float cos, float sin, float velocityX, float velocityY, float angularVelocity,
                  int shapeCount, int contactCapacity, float mass, float inertia) {
            this.x = x;
            this.y = y;
            this.cos = cos;
            this.sin = sin;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.shapeCount = shapeCount;
            this.contactCapacity = contactCapacity;
            this.mass = mass;
            this.inertia = inertia;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s %d %d %s %s", formatFloat(x), formatFloat(y),
                formatFloat(cos), formatFloat(sin), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), shapeCount, contactCapacity, formatFloat(mass), formatFloat(inertia));
        }
    }

    public static final class JointState {
        public final float angle;
        public final float motorTorque;
        public final float maxMotorTorque;
        public final float springHertz;
        public final float dampingRatio;
        public final float linearSeparation;
        public final float angularSeparation;
        public final float forceX;
        public final float forceY;
        public final float torque;

        JointState(float angle, float motorTorque, float maxMotorTorque, float springHertz, float dampingRatio,
                   float linearSeparation, float angularSeparation, float forceX, float forceY, float torque) {
            this.angle = angle;
            this.motorTorque = motorTorque;
            this.maxMotorTorque = maxMotorTorque;
            this.springHertz = springHertz;
            this.dampingRatio = dampingRatio;
            this.linearSeparation = linearSeparation;
            this.angularSeparation = angularSeparation;
            this.forceX = forceX;
            this.forceY = forceY;
            this.torque = torque;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s %s %s %s", formatFloat(angle),
                formatFloat(motorTorque), formatFloat(maxMotorTorque), formatFloat(springHertz),
                formatFloat(dampingRatio), formatFloat(linearSeparation), formatFloat(angularSeparation),
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
