package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2DistanceJointDef;
import org.box2d4j.b2Hull;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WheelJointDef;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ScissorLift {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int LINK_PAIR_COUNT = 3;
    private static final int BODY_COUNT = 10;
    private static final int JOINT_COUNT = 14;

    private ScissorLift() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-20.0f, 0.0f), new b2Vec2(20.0f, 0.0f)));

        Scene scene = new Scene();
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.sleepThreshold = 0.01f;

        b2Capsule capsule = new b2Capsule(new b2Vec2(-2.5f, 0.0f), new b2Vec2(2.5f, 0.0f), 0.15f);
        b2BodyId baseId1 = groundId;
        b2BodyId baseId2 = groundId;
        b2Vec2 baseAnchor1 = new b2Vec2(-2.5f, 0.2f);
        b2Vec2 baseAnchor2 = new b2Vec2(2.5f, 0.2f);
        float y = 0.5f;
        b2BodyId linkId1 = null;

        for (int i = 0; i < LINK_PAIR_COUNT; ++i) {
            bodyDef.position = new b2Vec2(0.0f, y);
            bodyDef.rotation = b2MakeRot(0.15f);
            b2BodyId bodyId1 = b2CreateBody(worldId, bodyDef);
            b2CreateCapsuleShape(bodyId1, shapeDef, capsule);
            scene.addBody(bodyId1);

            bodyDef.position = new b2Vec2(0.0f, y);
            bodyDef.rotation = b2MakeRot(-0.15f);
            b2BodyId bodyId2 = b2CreateBody(worldId, bodyDef);
            b2CreateCapsuleShape(bodyId2, shapeDef, capsule);
            scene.addBody(bodyId2);

            if (i == 1) {
                linkId1 = bodyId2;
            }

            b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
            revoluteDef.bodyIdA = baseId1;
            revoluteDef.bodyIdB = bodyId1;
            revoluteDef.localAnchorA = new b2Vec2(baseAnchor1);
            revoluteDef.localAnchorB = new b2Vec2(-2.5f, 0.0f);
            revoluteDef.collideConnected = i == 0;
            scene.addJoint(b2CreateRevoluteJoint(worldId, revoluteDef));

            if (i == 0) {
                b2WheelJointDef wheelDef = b2DefaultWheelJointDef();
                wheelDef.bodyIdA = baseId2;
                wheelDef.bodyIdB = bodyId2;
                wheelDef.localAxisA = new b2Vec2(1.0f, 0.0f);
                wheelDef.localAnchorA = new b2Vec2(baseAnchor2);
                wheelDef.localAnchorB = new b2Vec2(2.5f, 0.0f);
                wheelDef.enableSpring = false;
                wheelDef.collideConnected = true;
                scene.addJoint(b2CreateWheelJoint(worldId, wheelDef));
            } else {
                revoluteDef.bodyIdA = baseId2;
                revoluteDef.bodyIdB = bodyId2;
                revoluteDef.localAnchorA = new b2Vec2(baseAnchor2);
                revoluteDef.localAnchorB = new b2Vec2(2.5f, 0.0f);
                revoluteDef.collideConnected = false;
                scene.addJoint(b2CreateRevoluteJoint(worldId, revoluteDef));
            }

            revoluteDef.bodyIdA = bodyId1;
            revoluteDef.bodyIdB = bodyId2;
            revoluteDef.localAnchorA = new b2Vec2(0.0f, 0.0f);
            revoluteDef.localAnchorB = new b2Vec2(0.0f, 0.0f);
            revoluteDef.collideConnected = false;
            scene.addJoint(b2CreateRevoluteJoint(worldId, revoluteDef));

            baseId1 = bodyId2;
            baseId2 = bodyId1;
            baseAnchor1 = new b2Vec2(-2.5f, 0.0f);
            baseAnchor2 = new b2Vec2(2.5f, 0.0f);
            y += 1.0f;
        }

        bodyDef.position = new b2Vec2(0.0f, y);
        bodyDef.rotation = b2Rot_identity.copy();
        b2BodyId platformId = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(platformId, shapeDef, b2MakeBox(3.0f, 0.2f));
        scene.addBody(platformId);

        b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
        revoluteDef.bodyIdA = platformId;
        revoluteDef.bodyIdB = baseId1;
        revoluteDef.localAnchorA = new b2Vec2(-2.5f, -0.4f);
        revoluteDef.localAnchorB = new b2Vec2(baseAnchor1);
        revoluteDef.collideConnected = true;
        scene.addJoint(b2CreateRevoluteJoint(worldId, revoluteDef));

        b2WheelJointDef wheelDef = b2DefaultWheelJointDef();
        wheelDef.bodyIdA = platformId;
        wheelDef.bodyIdB = baseId2;
        wheelDef.localAxisA = new b2Vec2(1.0f, 0.0f);
        wheelDef.localAnchorA = new b2Vec2(2.5f, -0.4f);
        wheelDef.localAnchorB = new b2Vec2(baseAnchor2);
        wheelDef.enableSpring = false;
        wheelDef.collideConnected = true;
        scene.addJoint(b2CreateWheelJoint(worldId, wheelDef));

        b2DistanceJointDef distanceDef = b2DefaultDistanceJointDef();
        distanceDef.bodyIdA = groundId;
        distanceDef.bodyIdB = linkId1;
        distanceDef.localAnchorA = new b2Vec2(-2.5f, 0.2f);
        distanceDef.localAnchorB = new b2Vec2(0.5f, 0.0f);
        distanceDef.enableSpring = true;
        distanceDef.minLength = 0.2f;
        distanceDef.maxLength = 5.5f;
        distanceDef.enableLimit = true;
        distanceDef.enableMotor = false;
        distanceDef.motorSpeed = 0.25f;
        distanceDef.maxMotorForce = 2000.0f;
        b2JointId liftJointId = b2CreateDistanceJoint(worldId, distanceDef);
        scene.addJoint(liftJointId);
        SampleRuntime.toggle("scissor.motor", "Motor", false, value -> {
            b2DistanceJoint_EnableMotor(liftJointId, value);
            b2Joint_WakeBodies(liftJointId);
        });
        SampleRuntime.slider("scissor.maxForce", "Max Force", distanceDef.maxMotorForce, 0.0f, 3000.0f, 50.0f,
            value -> {
                b2DistanceJoint_SetMaxMotorForce(liftJointId, value);
                b2Joint_WakeBodies(liftJointId);
            });
        SampleRuntime.slider("scissor.speed", "Speed", distanceDef.motorSpeed, -0.3f, 0.3f, 0.01f,
            value -> {
                b2DistanceJoint_SetMotorSpeed(liftJointId, value);
                b2Joint_WakeBodies(liftJointId);
            });

        Car car = new Car();
        car.spawn(worldId, new b2Vec2(0.0f, y + 2.0f), 1.0f, 3.0f, 0.7f, 0.0f);
        scene.addBody(car.chassisId);
        scene.addBody(car.rearWheelId);
        scene.addBody(car.frontWheelId);
        scene.addJoint(car.rearAxleId);
        scene.addJoint(car.frontAxleId);

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 8);
        }

        BodyState[] bodyStates = new BodyState[scene.bodyCount];
        for (int i = 0; i < scene.bodyCount; ++i) {
            bodyStates[i] = bodyState(scene.bodies[i]);
        }
        JointState[] jointStates = new JointState[scene.jointCount];
        for (int i = 0; i < scene.jointCount; ++i) {
            jointStates[i] = jointState(scene.joints[i]);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), bodyStates, jointStates);
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
        } else if (type == b2_wheelJoint) {
            metricA = b2WheelJoint_GetMotorTorque(jointId);
            metricB = b2WheelJoint_GetMaxMotorTorque(jointId);
            metricC = b2WheelJoint_GetSpringHertz(jointId);
        } else if (type == b2_distanceJoint) {
            metricA = b2DistanceJoint_GetCurrentLength(jointId);
            metricB = b2DistanceJoint_GetMotorForce(jointId);
            metricC = b2DistanceJoint_GetMaxMotorForce(jointId);
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
            builder.append("scissorLift ")
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

    private static final class Scene {
        final b2BodyId[] bodies = new b2BodyId[BODY_COUNT];
        final b2JointId[] joints = new b2JointId[JOINT_COUNT];
        int bodyCount;
        int jointCount;

        void addBody(b2BodyId bodyId) {
            bodies[bodyCount++] = bodyId;
        }

        void addJoint(b2JointId jointId) {
            joints[jointCount++] = jointId;
        }
    }

    private static final class Car {
        b2BodyId chassisId;
        b2BodyId rearWheelId;
        b2BodyId frontWheelId;
        b2JointId rearAxleId;
        b2JointId frontAxleId;

        void spawn(b2WorldId worldId, b2Vec2 position, float scale, float hertz, float dampingRatio, float torque) {
            b2Vec2[] vertices = {
                new b2Vec2(-1.5f, -0.5f), new b2Vec2(1.5f, -0.5f), new b2Vec2(1.5f, 0.0f),
                new b2Vec2(0.0f, 0.9f), new b2Vec2(-1.15f, 0.9f), new b2Vec2(-1.5f, 0.2f)
            };
            for (int i = 0; i < vertices.length; ++i) {
                vertices[i].x *= 0.85f * scale;
                vertices[i].y *= 0.85f * scale;
            }
            b2Hull hull = b2ComputeHull(vertices, 6);
            b2Polygon chassis = b2MakePolygon(hull, 0.15f * scale);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f / scale;
            shapeDef.material.friction = 0.2f;

            b2Circle circle = new b2Circle(new b2Vec2(0.0f, 0.0f), 0.4f * scale);
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = b2Add(new b2Vec2(0.0f, 1.0f * scale), position);
            chassisId = b2CreateBody(worldId, bodyDef);
            b2CreatePolygonShape(chassisId, shapeDef, chassis);

            shapeDef.density = 2.0f / scale;
            shapeDef.material.friction = 1.5f;
            shapeDef.material.rollingResistance = 0.1f;

            bodyDef.position = b2Add(new b2Vec2(-1.0f * scale, 0.35f * scale), position);
            bodyDef.allowFastRotation = true;
            rearWheelId = b2CreateBody(worldId, bodyDef);
            b2CreateCircleShape(rearWheelId, shapeDef, circle);

            bodyDef.position = b2Add(new b2Vec2(1.0f * scale, 0.4f * scale), position);
            bodyDef.allowFastRotation = true;
            frontWheelId = b2CreateBody(worldId, bodyDef);
            b2CreateCircleShape(frontWheelId, shapeDef, circle);

            b2Vec2 axis = new b2Vec2(0.0f, 1.0f);
            b2Vec2 pivot = b2Body_GetPosition(rearWheelId);
            b2WheelJointDef jointDef = b2DefaultWheelJointDef();
            jointDef.bodyIdA = chassisId;
            jointDef.bodyIdB = rearWheelId;
            jointDef.localAxisA = b2Body_GetLocalVector(jointDef.bodyIdA, axis);
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            jointDef.motorSpeed = 0.0f;
            jointDef.maxMotorTorque = torque;
            jointDef.enableMotor = true;
            jointDef.hertz = hertz;
            jointDef.dampingRatio = dampingRatio;
            jointDef.lowerTranslation = -0.25f * scale;
            jointDef.upperTranslation = 0.25f * scale;
            jointDef.enableLimit = true;
            rearAxleId = b2CreateWheelJoint(worldId, jointDef);

            pivot = b2Body_GetPosition(frontWheelId);
            jointDef.bodyIdA = chassisId;
            jointDef.bodyIdB = frontWheelId;
            jointDef.localAxisA = b2Body_GetLocalVector(jointDef.bodyIdA, axis);
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            jointDef.motorSpeed = 0.0f;
            jointDef.maxMotorTorque = torque;
            jointDef.enableMotor = true;
            jointDef.hertz = hertz;
            jointDef.dampingRatio = dampingRatio;
            jointDef.lowerTranslation = -0.25f * scale;
            jointDef.upperTranslation = 0.25f * scale;
            jointDef.enableLimit = true;
            frontAxleId = b2CreateWheelJoint(worldId, jointDef);
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
