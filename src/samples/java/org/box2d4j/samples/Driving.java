package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Hull;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2Rot;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WheelJointDef;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Driving {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int BRIDGE_COUNT = 20;
    private static final int BOX_COUNT = 5;

    private Driving() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyId groundId = createGround(worldId);
        BodyRefs refs = new BodyRefs();
        refs.teeter = createTeeter(worldId, groundId);
        createBridge(worldId, groundId, refs.bridge);
        createBoxes(worldId, refs.boxes);
        Car car = new Car();
        car.spawn(worldId, new b2Vec2(0.0f, 0.0f), 1.0f, 5.0f, 0.7f, 5.0f);
        configureRuntime(car);

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] carBodies = {
            bodyState(car.chassisId), bodyState(car.rearWheelId), bodyState(car.frontWheelId)
        };
        BodyState[] bridgeBodies = {
            bodyState(refs.bridge[0]), bodyState(refs.bridge[BRIDGE_COUNT / 2]), bodyState(refs.bridge[BRIDGE_COUNT - 1])
        };
        BodyState[] boxBodies = new BodyState[refs.boxes.length];
        for (int i = 0; i < refs.boxes.length; ++i) {
            boxBodies[i] = bodyState(refs.boxes[i]);
        }
        AxleState[] axles = {axleState(car.rearAxleId), axleState(car.frontAxleId)};
        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), bodyState(refs.teeter), carBodies, bridgeBodies, boxBodies, axles);
        b2DestroyWorld(worldId);
        return result;
    }

    private static void configureRuntime(Car car) {
        RuntimeState state = new RuntimeState();
        SampleRuntime.hold("driving.left", "Drive Left", "A", value -> state.leftPressed = value);
        SampleRuntime.hold("driving.brake", "Brake", "S", value -> state.brakePressed = value);
        SampleRuntime.hold("driving.right", "Drive Right", "D", value -> state.rightPressed = value);
        SampleRuntime.slider("driving.hertz", "Spring Hertz", state.hertz, 0.0f, 20.0f, 1.0f,
            car::setHertz);
        SampleRuntime.slider("driving.damping", "Damping Ratio", state.dampingRatio, 0.0f, 10.0f, 0.1f,
            car::setDampingRatio);
        SampleRuntime.slider("driving.speed", "Speed", state.speed, 0.0f, 50.0f, 1.0f, value -> {
            state.speed = value;
            car.setSpeed(state.throttle * state.speed);
        });
        SampleRuntime.slider("driving.torque", "Torque", state.torque, 0.0f, 10.0f, 0.1f,
            car::setTorque);
        SampleRuntime.camera(() -> {
            b2Vec2 position = b2Body_GetPosition(car.chassisId);
            return new SampleRuntime.CameraPosition(position.x, 5.0f, 30.0f);
        });
        SampleRuntime.beforeStep(() -> {
            if (state.leftPressed) {
                state.throttle = 1.0f;
                car.setSpeed(state.speed);
            }
            if (state.brakePressed) {
                state.throttle = 0.0f;
                car.setSpeed(0.0f);
            }
            if (state.rightPressed) {
                state.throttle = -1.0f;
                car.setSpeed(-state.speed);
            }
        });
    }

    private static b2BodyId createGround(b2WorldId worldId) {
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2Vec2[] points = new b2Vec2[25];
        int count = 24;

        points[count--] = new b2Vec2(-20.0f, -20.0f);
        points[count--] = new b2Vec2(-20.0f, 0.0f);
        points[count--] = new b2Vec2(20.0f, 0.0f);

        float[] hs = {0.25f, 1.0f, 4.0f, 0.0f, 0.0f, -1.0f, -2.0f, -2.0f, -1.25f, 0.0f};
        float x = 20.0f;
        float dx = 5.0f;
        for (int j = 0; j < 2; ++j) {
            for (int i = 0; i < 10; ++i) {
                points[count--] = new b2Vec2(x + dx, hs[i]);
                x += dx;
            }
        }

        points[count--] = new b2Vec2(x + 40.0f, 0.0f);
        points[count--] = new b2Vec2(x + 40.0f, -20.0f);

        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = points;
        chainDef.count = 25;
        chainDef.isLoop = true;
        b2CreateChain(groundId, chainDef);

        x += 80.0f;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, shapeDef, new b2Segment(new b2Vec2(x, 0.0f), new b2Vec2(x + 40.0f, 0.0f)));
        x += 40.0f;
        b2CreateSegmentShape(groundId, shapeDef, new b2Segment(new b2Vec2(x, 0.0f), new b2Vec2(x + 10.0f, 5.0f)));
        x += 20.0f;
        b2CreateSegmentShape(groundId, shapeDef, new b2Segment(new b2Vec2(x, 0.0f), new b2Vec2(x + 40.0f, 0.0f)));
        x += 40.0f;
        b2CreateSegmentShape(groundId, shapeDef, new b2Segment(new b2Vec2(x, 0.0f), new b2Vec2(x, 20.0f)));
        return groundId;
    }

    private static b2BodyId createTeeter(b2WorldId worldId, b2BodyId groundId) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(140.0f, 1.0f);
        bodyDef.angularVelocity = 1.0f;
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2CreatePolygonShape(bodyId, b2DefaultShapeDef(), b2MakeBox(10.0f, 0.25f));

        b2Vec2 pivot = new b2Vec2(bodyDef.position);
        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = groundId;
        jointDef.bodyIdB = bodyId;
        jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
        jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
        jointDef.lowerAngle = -8.0f * B2_PI / 180.0f;
        jointDef.upperAngle = 8.0f * B2_PI / 180.0f;
        jointDef.enableLimit = true;
        b2CreateRevoluteJoint(worldId, jointDef);
        return bodyId;
    }

    private static void createBridge(b2WorldId worldId, b2BodyId groundId, b2BodyId[] bridge) {
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Capsule capsule = new b2Capsule(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f), 0.125f);
        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        b2BodyId prevBodyId = groundId;
        for (int i = 0; i < BRIDGE_COUNT; ++i) {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(161.0f + 2.0f * i, -0.125f);
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            b2CreateCapsuleShape(bodyId, shapeDef, capsule);

            b2Vec2 pivot = new b2Vec2(160.0f + 2.0f * i, -0.125f);
            jointDef.bodyIdA = prevBodyId;
            jointDef.bodyIdB = bodyId;
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            b2CreateRevoluteJoint(worldId, jointDef);
            bridge[i] = bodyId;
            prevBodyId = bodyId;
        }

        b2Vec2 pivot = new b2Vec2(160.0f + 2.0f * BRIDGE_COUNT, -0.125f);
        jointDef.bodyIdA = prevBodyId;
        jointDef.bodyIdB = groundId;
        jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
        jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
        jointDef.enableMotor = true;
        jointDef.maxMotorTorque = 50.0f;
        b2CreateRevoluteJoint(worldId, jointDef);
    }

    private static void createBoxes(b2WorldId worldId, b2BodyId[] boxes) {
        b2Polygon box = b2MakeBox(0.5f, 0.5f);
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.25f;
        shapeDef.material.restitution = 0.25f;
        shapeDef.density = 0.25f;
        for (int i = 0; i < boxes.length; ++i) {
            bodyDef.position = new b2Vec2(230.0f, 0.5f + i);
            boxes[i] = b2CreateBody(worldId, bodyDef);
            b2CreatePolygonShape(boxes[i], shapeDef, box);
        }
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x, velocity.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    private static AxleState axleState(b2JointId jointId) {
        b2Vec2 force = b2Joint_GetConstraintForce(jointId);
        return new AxleState(b2WheelJoint_GetMotorSpeed(jointId), b2WheelJoint_GetMaxMotorTorque(jointId),
            b2WheelJoint_GetMotorTorque(jointId), b2WheelJoint_GetSpringHertz(jointId),
            b2WheelJoint_GetSpringDampingRatio(jointId), b2Joint_GetLinearSeparation(jointId), force.x, force.y,
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
        public final BodyState teeter;
        public final BodyState[] carBodies;
        public final BodyState[] bridgeBodies;
        public final BodyState[] boxes;
        public final AxleState[] axles;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, BodyState teeter,
            BodyState[] carBodies, BodyState[] bridgeBodies, BodyState[] boxes, AxleState[] axles) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.teeter = teeter;
            this.carBodies = carBodies;
            this.bridgeBodies = bridgeBodies;
            this.boxes = boxes;
            this.axles = axles;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("driving ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(jointCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(carBodies.length).append(' ')
                .append(bridgeBodies.length).append(' ')
                .append(boxes.length).append(' ')
                .append(axles.length)
                .append(teeter.toLinePart());
            for (BodyState body : carBodies) {
                builder.append(body.toLinePart());
            }
            for (BodyState body : bridgeBodies) {
                builder.append(body.toLinePart());
            }
            for (BodyState body : boxes) {
                builder.append(body.toLinePart());
            }
            for (AxleState axle : axles) {
                builder.append(axle.toLinePart());
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

    public static final class AxleState {
        public final float motorSpeed;
        public final float maxMotorTorque;
        public final float motorTorque;
        public final float springHertz;
        public final float dampingRatio;
        public final float linearSeparation;
        public final float forceX;
        public final float forceY;
        public final float torque;

        AxleState(float motorSpeed, float maxMotorTorque, float motorTorque, float springHertz, float dampingRatio,
            float linearSeparation, float forceX, float forceY, float torque) {
            this.motorSpeed = motorSpeed;
            this.maxMotorTorque = maxMotorTorque;
            this.motorTorque = motorTorque;
            this.springHertz = springHertz;
            this.dampingRatio = dampingRatio;
            this.linearSeparation = linearSeparation;
            this.forceX = forceX;
            this.forceY = forceY;
            this.torque = torque;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s %s %s", formatFloat(motorSpeed),
                formatFloat(maxMotorTorque), formatFloat(motorTorque), formatFloat(springHertz),
                formatFloat(dampingRatio), formatFloat(linearSeparation), formatFloat(forceX), formatFloat(forceY),
                formatFloat(torque));
        }
    }

    private static final class BodyRefs {
        final b2BodyId[] bridge = new b2BodyId[BRIDGE_COUNT];
        final b2BodyId[] boxes = new b2BodyId[BOX_COUNT];
        b2BodyId teeter;
    }

    private static final class RuntimeState {
        float speed = 35.0f;
        float torque = 5.0f;
        float hertz = 5.0f;
        float dampingRatio = 0.7f;
        float throttle;
        boolean leftPressed;
        boolean brakePressed;
        boolean rightPressed;
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

        void setSpeed(float speed) {
            b2WheelJoint_SetMotorSpeed(rearAxleId, speed);
            b2WheelJoint_SetMotorSpeed(frontAxleId, speed);
            b2Joint_WakeBodies(rearAxleId);
        }

        void setTorque(float torque) {
            b2WheelJoint_SetMaxMotorTorque(rearAxleId, torque);
            b2WheelJoint_SetMaxMotorTorque(frontAxleId, torque);
        }

        void setHertz(float hertz) {
            b2WheelJoint_SetSpringHertz(rearAxleId, hertz);
            b2WheelJoint_SetSpringHertz(frontAxleId, hertz);
        }

        void setDampingRatio(float dampingRatio) {
            b2WheelJoint_SetSpringDampingRatio(rearAxleId, dampingRatio);
            b2WheelJoint_SetSpringDampingRatio(frontAxleId, dampingRatio);
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
