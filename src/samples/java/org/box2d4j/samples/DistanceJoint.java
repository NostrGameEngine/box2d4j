package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2DistanceJointDef;
import org.box2d4j.b2JointId;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class DistanceJoint {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int DEFAULT_COUNT = 1;

    private DistanceJoint() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT, DEFAULT_COUNT);
    }

    public static Result run(int stepCount) {
        return run(stepCount, DEFAULT_COUNT);
    }

    public static Result run(int stepCount, int count) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());

        float hertz = 2.0f;
        float dampingRatio = 0.5f;
        float length = 1.0f;
        float minLength = length;
        float maxLength = length;
        boolean enableSpring = false;
        boolean enableLimit = false;

        boolean interactive = SampleRuntime.isActive();
        int capacity = interactive ? 10 : count;
        b2BodyId[] bodies = new b2BodyId[capacity];
        b2JointId[] joints = new b2JointId[capacity];
        int[] activeCount = {count};
        Parameters parameters = new Parameters(length, minLength, maxLength, hertz, dampingRatio,
            enableSpring, enableLimit);
        createChain(worldId, groundId, bodies, joints, activeCount[0], parameters);
        SampleRuntime.slider("distance.length", "Length", length, 0.1f, 4.0f, 0.1f,
            value -> {
                parameters.length = value;
                forEachJoint(joints, activeCount[0], joint -> b2DistanceJoint_SetLength(joint, value));
            });
        SampleRuntime.toggle("distance.spring", "Spring", enableSpring,
            value -> {
                parameters.enableSpring = value;
                forEachJoint(joints, activeCount[0], joint -> b2DistanceJoint_EnableSpring(joint, value));
            });
        SampleRuntime.slider("distance.hertz", "Hertz", hertz, 0.0f, 15.0f, 0.1f,
            value -> {
                parameters.hertz = value;
                forEachJoint(joints, activeCount[0], joint -> b2DistanceJoint_SetSpringHertz(joint, value));
            });
        SampleRuntime.slider("distance.damping", "Damping", dampingRatio, 0.0f, 4.0f, 0.1f,
            value -> {
                parameters.dampingRatio = value;
                forEachJoint(joints, activeCount[0], joint -> b2DistanceJoint_SetSpringDampingRatio(joint, value));
            });
        SampleRuntime.toggle("distance.limit", "Limit", enableLimit,
            value -> {
                parameters.enableLimit = value;
                forEachJoint(joints, activeCount[0], joint -> b2DistanceJoint_EnableLimit(joint, value));
            });
        SampleRuntime.slider("distance.minLength", "Min Length", minLength, 0.1f, 4.0f, 0.1f, value -> {
            parameters.minLength = value;
            forEachJoint(joints, activeCount[0],
                joint -> b2DistanceJoint_SetLengthRange(joint, parameters.minLength, parameters.maxLength));
        });
        SampleRuntime.slider("distance.maxLength", "Max Length", maxLength, 0.1f, 4.0f, 0.1f, value -> {
            parameters.maxLength = value;
            forEachJoint(joints, activeCount[0],
                joint -> b2DistanceJoint_SetLengthRange(joint, parameters.minLength, parameters.maxLength));
        });
        SampleRuntime.integer("distance.count", "Count", activeCount[0], 1, 10, 1, value -> {
            destroyChain(bodies, joints, activeCount[0]);
            activeCount[0] = value;
            createChain(worldId, groundId, bodies, joints, activeCount[0], parameters);
        });

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        BodyState[] bodyStates = new BodyState[activeCount[0]];
        JointState[] jointStates = new JointState[activeCount[0]];
        for (int i = 0; i < activeCount[0]; ++i) {
            bodyStates[i] = bodyState(bodies[i]);
            jointStates[i] = jointState(joints[i]);
        }

        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), bodyStates, jointStates);
        b2DestroyWorld(worldId);
        return result;
    }

    private static void createChain(b2WorldId worldId, b2BodyId groundId, b2BodyId[] bodies, b2JointId[] joints,
                                    int count, Parameters parameters) {
        b2Circle circle = new b2Circle(new b2Vec2(0.0f, 0.0f), 0.25f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 20.0f;
        float yOffset = 20.0f;
        b2DistanceJointDef jointDef = b2DefaultDistanceJointDef();
        jointDef.hertz = parameters.hertz;
        jointDef.dampingRatio = parameters.dampingRatio;
        jointDef.length = parameters.length;
        jointDef.minLength = parameters.minLength;
        jointDef.maxLength = parameters.maxLength;
        jointDef.enableSpring = parameters.enableSpring;
        jointDef.enableLimit = parameters.enableLimit;
        b2BodyId previousBodyId = groundId;
        for (int i = 0; i < count; ++i) {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.angularDamping = 0.1f;
            bodyDef.position = new b2Vec2(parameters.length * (i + 1.0f), yOffset);
            bodies[i] = b2CreateBody(worldId, bodyDef);
            b2CreateCircleShape(bodies[i], shapeDef, circle);
            b2Vec2 pivotA = new b2Vec2(parameters.length * i, yOffset);
            b2Vec2 pivotB = new b2Vec2(parameters.length * (i + 1.0f), yOffset);
            jointDef.bodyIdA = previousBodyId;
            jointDef.bodyIdB = bodies[i];
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivotA);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivotB);
            joints[i] = b2CreateDistanceJoint(worldId, jointDef);
            previousBodyId = bodies[i];
        }
    }

    private static void destroyChain(b2BodyId[] bodies, b2JointId[] joints, int count) {
        for (int i = 0; i < count; ++i) {
            if (joints[i] != null && b2Joint_IsValid(joints[i])) {
                b2DestroyJoint(joints[i]);
            }
            joints[i] = null;
        }
        for (int i = 0; i < count; ++i) {
            if (bodies[i] != null && b2Body_IsValid(bodies[i])) {
                b2DestroyBody(bodies[i]);
            }
            bodies[i] = null;
        }
    }

    private static void forEachJoint(b2JointId[] joints, int count,
                                     java.util.function.Consumer<b2JointId> action) {
        for (int i = 0; i < count; ++i) {
            b2JointId joint = joints[i];
            action.accept(joint);
            b2Joint_WakeBodies(joint);
        }
    }

    private static final class Parameters {
        float length;
        float minLength;
        float maxLength;
        float hertz;
        float dampingRatio;
        boolean enableSpring;
        boolean enableLimit;

        Parameters(float length, float minLength, float maxLength, float hertz, float dampingRatio,
                   boolean enableSpring, boolean enableLimit) {
            this.length = length;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.hertz = hertz;
            this.dampingRatio = dampingRatio;
            this.enableSpring = enableSpring;
            this.enableLimit = enableLimit;
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
        return new JointState(b2DistanceJoint_GetCurrentLength(jointId), b2DistanceJoint_GetMotorForce(jointId),
            force.x, force.y, b2Joint_GetConstraintTorque(jointId));
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
            builder.append("distanceJoint ")
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
        public final float currentLength;
        public final float motorForce;
        public final float forceX;
        public final float forceY;
        public final float torque;

        JointState(float currentLength, float motorForce, float forceX, float forceY, float torque) {
            this.currentLength = currentLength;
            this.motorForce = motorForce;
            this.forceX = forceX;
            this.forceY = forceY;
            this.torque = torque;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s", formatFloat(currentLength), formatFloat(motorForce),
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
