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
import org.box2d4j.b2PrismaticJointDef;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2Rot;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2SurfaceMaterial;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class GearLift {
    private static final int RAND_LIMIT = 32767;
    private static final int RAND_SEED = 12345;
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int BODY_COUNT = 243;
    private static final int JOINT_COUNT = 44;
    private static final String GROUND_PATH =
        "m 63.500002,201.08333 103.187498,0 1e-5,-37.04166 h -2.64584 l 0,34.39583 h -42.33333 v -2.64583 l "
            + "-2.64584,-1e-5 v -2.64583 h -2.64583 v -2.64584 h -2.64584 v -2.64583 H 111.125 v -2.64583 h -2.64583 v "
            + "-2.64583 h -2.64583 v -2.64584 l -2.64584,1e-5 v -2.64583 l -2.64583,-1e-5 V 174.625 h -2.645834 v -2.64584 l "
            + "-2.645833,1e-5 v -2.64584 H 92.60417 v -2.64583 h -2.645834 v -2.64583 l -26.458334,0 0,37.04166";

    private GearLift() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        Scene scene = new Scene();

        b2BodyId groundId = createGround(worldId);

        float gearRadius = 1.0f;
        float toothHalfWidth = 0.09f;
        float toothHalfHeight = 0.06f;
        float toothRadius = 0.03f;
        float linkHalfLength = 0.07f;
        float linkRadius = 0.05f;
        int linkCount = 40;
        float doorHalfHeight = 1.5f;

        b2Vec2 gearPosition1 = new b2Vec2(-4.25f, 9.75f);
        b2Vec2 gearPosition2 = b2Add(gearPosition1, new b2Vec2(2.0f, 1.0f));
        b2Vec2 linkAttachPosition = b2Add(gearPosition2,
            new b2Vec2(gearRadius + 2.0f * toothHalfWidth + toothRadius, 0.0f));
        b2Vec2 doorPosition = b2Sub(linkAttachPosition,
            new b2Vec2(0.0f, 2.0f * linkCount * linkHalfLength + doorHalfHeight));

        b2BodyId driverGearId = createGear(worldId, scene, groundId, gearPosition1, gearRadius, toothHalfWidth,
            toothHalfHeight, toothRadius, true);
        b2JointId driverId = createDriverJoint(worldId, groundId, driverGearId, gearPosition1);
        scene.addJoint(driverId);
        SampleRuntime.toggle("gearLift.motor", "Motor", true, enabled -> {
            b2RevoluteJoint_EnableMotor(driverId, enabled);
            b2Joint_WakeBodies(driverId);
        });
        SampleRuntime.slider("gearLift.maxTorque", "Max Torque", 80.0f, 0.0f, 100.0f, 1.0f, value -> {
            b2RevoluteJoint_SetMaxMotorTorque(driverId, value);
            b2Joint_WakeBodies(driverId);
        });
        SampleRuntime.slider("gearLift.speed", "Speed", 0.0f, -0.3f, 0.3f, 0.01f, "A", "D", value -> {
            b2RevoluteJoint_SetMotorSpeed(driverId, value);
            b2Joint_WakeBodies(driverId);
        });

        b2BodyId followerId = createGear(worldId, scene, groundId, gearPosition2, gearRadius, toothHalfWidth,
            toothHalfHeight, toothRadius, false);
        scene.addJoint(createFollowerJoint(worldId, groundId, followerId, gearPosition2));

        b2BodyId lastLinkId = createLinks(worldId, scene, followerId, linkAttachPosition, linkHalfLength, linkRadius,
            linkCount);
        createDoor(worldId, scene, groundId, lastLinkId, doorPosition, doorHalfHeight);
        createPayload(worldId, scene);

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
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

    private static b2BodyId createGround(b2WorldId worldId) {
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2Vec2[] points = parsePath(GROUND_PATH, new b2Vec2(-120.0f, -200.0f), 64, 0.2f);
        b2SurfaceMaterial material = b2DefaultSurfaceMaterial();
        material.customColor = b2_colorDarkSeaGreen;

        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = points;
        chainDef.count = points.length;
        chainDef.isLoop = true;
        chainDef.materials = new b2SurfaceMaterial[]{material};
        chainDef.materialCount = 1;
        b2CreateChain(groundId, chainDef);
        return groundId;
    }

    private static b2BodyId createGear(b2WorldId worldId, Scene scene, b2BodyId groundId, b2Vec2 position,
                                       float gearRadius, float toothHalfWidth, float toothHalfHeight,
                                       float toothRadius, boolean driver) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(position);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        scene.addBody(bodyId);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.1f;
        shapeDef.material.customColor = b2_colorSaddleBrown;
        b2CreateCircleShape(bodyId, shapeDef, new b2Circle(b2Vec2_zero, gearRadius));

        int count = 16;
        float deltaAngle = 2.0f * B2_PI / 16.0f;
        b2Rot dq = b2MakeRot(deltaAngle);
        b2Vec2 center = new b2Vec2(gearRadius + (driver ? toothHalfHeight : toothHalfWidth), 0.0f);
        b2Rot rotation = b2Rot_identity.copy();

        for (int i = 0; i < count; ++i) {
            b2Polygon tooth = b2MakeOffsetRoundedBox(toothHalfWidth, toothHalfHeight, center, rotation, toothRadius);
            shapeDef.material.customColor = b2_colorGray;
            b2CreatePolygonShape(bodyId, shapeDef, tooth);

            rotation = b2MulRot(dq, rotation);
            center = b2RotateVector(rotation,
                new b2Vec2(gearRadius + (driver ? toothHalfHeight : toothHalfWidth), 0.0f));
        }

        return bodyId;
    }

    private static b2JointId createDriverJoint(b2WorldId worldId, b2BodyId groundId, b2BodyId bodyId,
                                               b2Vec2 position) {
        b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
        revoluteDef.bodyIdA = groundId;
        revoluteDef.bodyIdB = bodyId;
        revoluteDef.localAnchorA = b2Body_GetLocalPoint(groundId, position);
        revoluteDef.localAnchorB = b2Vec2_zero.copy();
        revoluteDef.enableMotor = true;
        revoluteDef.maxMotorTorque = 80.0f;
        revoluteDef.motorSpeed = 0.0f;
        return b2CreateRevoluteJoint(worldId, revoluteDef);
    }

    private static b2JointId createFollowerJoint(b2WorldId worldId, b2BodyId groundId, b2BodyId followerId,
                                                 b2Vec2 position) {
        b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
        revoluteDef.bodyIdA = groundId;
        revoluteDef.bodyIdB = followerId;
        revoluteDef.localAnchorA = b2Body_GetLocalPoint(groundId, position);
        revoluteDef.localAnchorB = b2Vec2_zero.copy();
        revoluteDef.enableMotor = true;
        revoluteDef.maxMotorTorque = 0.5f;
        revoluteDef.referenceAngle = 0.25f * B2_PI;
        revoluteDef.lowerAngle = -0.3f * B2_PI;
        revoluteDef.upperAngle = 0.8f * B2_PI;
        revoluteDef.enableLimit = true;
        return b2CreateRevoluteJoint(worldId, revoluteDef);
    }

    private static b2BodyId createLinks(b2WorldId worldId, Scene scene, b2BodyId followerId, b2Vec2 linkAttachPosition,
                                        float linkHalfLength, float linkRadius, int count) {
        b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, -linkHalfLength),
            new b2Vec2(0.0f, linkHalfLength), linkRadius);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        shapeDef.material.customColor = b2_colorLightSteelBlue;

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.maxMotorTorque = 0.05f;
        jointDef.enableMotor = true;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2Vec2 position = b2Add(linkAttachPosition, new b2Vec2(0.0f, -linkHalfLength));
        b2BodyId prevBodyId = followerId;
        for (int i = 0; i < count; ++i) {
            bodyDef.position = new b2Vec2(position);

            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            b2CreateCapsuleShape(bodyId, shapeDef, capsule);
            scene.addBody(bodyId);

            b2Vec2 pivot = new b2Vec2(position.x, position.y + linkHalfLength);
            jointDef.bodyIdA = prevBodyId;
            jointDef.bodyIdB = bodyId;
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            scene.addJoint(b2CreateRevoluteJoint(worldId, jointDef));

            position.y -= 2.0f * linkHalfLength;
            prevBodyId = bodyId;
        }
        return prevBodyId;
    }

    private static void createDoor(b2WorldId worldId, Scene scene, b2BodyId groundId, b2BodyId lastLinkId,
                                   b2Vec2 doorPosition, float doorHalfHeight) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(doorPosition);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        scene.addBody(bodyId);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.1f;
        shapeDef.material.customColor = b2_colorDarkCyan;
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(0.15f, doorHalfHeight));

        b2Vec2 pivot = b2Add(doorPosition, new b2Vec2(0.0f, doorHalfHeight));
        b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
        revoluteDef.bodyIdA = lastLinkId;
        revoluteDef.bodyIdB = bodyId;
        revoluteDef.localAnchorA = b2Body_GetLocalPoint(lastLinkId, pivot);
        revoluteDef.localAnchorB = new b2Vec2(0.0f, doorHalfHeight);
        revoluteDef.enableMotor = true;
        revoluteDef.maxMotorTorque = 0.05f;
        scene.addJoint(b2CreateRevoluteJoint(worldId, revoluteDef));

        b2PrismaticJointDef jointDef = b2DefaultPrismaticJointDef();
        jointDef.bodyIdA = groundId;
        jointDef.bodyIdB = bodyId;
        jointDef.localAnchorA = b2Body_GetLocalPoint(groundId, doorPosition);
        jointDef.localAnchorB = b2Vec2_zero.copy();
        jointDef.localAxisA = new b2Vec2(0.0f, 1.0f);
        jointDef.maxMotorForce = 0.2f;
        jointDef.enableMotor = true;
        jointDef.collideConnected = true;
        scene.addJoint(b2CreatePrismaticJoint(worldId, jointDef));
    }

    private static void createPayload(b2WorldId worldId, Scene scene) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.rollingResistance = 0.3f;

        int[] colors = {
            b2_colorGray, b2_colorGainsboro, b2_colorLightGray, b2_colorLightSlateGray, b2_colorDarkGray
        };
        RandomState random = new RandomState(RAND_SEED);

        float y = 4.25f;
        int xCount = 10;
        int yCount = 20;
        for (int i = 0; i < yCount; ++i) {
            float x = -3.15f;
            for (int j = 0; j < xCount; ++j) {
                bodyDef.position = new b2Vec2(x, y);
                b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
                b2Polygon poly = random.randomPolygon(0.1f);
                poly.radius = random.randomFloatRange(0.01f, 0.02f);
                shapeDef.material.customColor = colors[random.randomIntRange(0, 4)];
                b2CreatePolygonShape(bodyId, shapeDef, poly);
                scene.addBody(bodyId);
                x += 0.2f;
            }
            y += 0.2f;
        }
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
            metricC = b2PrismaticJoint_GetMaxMotorForce(jointId);
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
            builder.append("gearLift ")
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

    private static b2Vec2[] parsePath(String svgPath, b2Vec2 offset, int capacity, float scale) {
        b2Vec2[] points = new b2Vec2[capacity];
        int pointCount = 0;
        b2Vec2 currentPoint = new b2Vec2();
        int index = 0;
        char command = svgPath.charAt(index);

        while (index < svgPath.length()) {
            char ch = svgPath.charAt(index);
            if (!Character.isDigit(ch) && ch != '-') {
                command = ch;
                if (command == 'z') {
                    break;
                }
                if (isPathCommand(command)) {
                    index += 1;
                    while (index < svgPath.length() && Character.isWhitespace(svgPath.charAt(index))) {
                        index += 1;
                    }
                }
            }

            int start = index;
            while (index < svgPath.length() && !Character.isWhitespace(svgPath.charAt(index))) {
                index += 1;
            }
            String token = svgPath.substring(start, index);
            applyPathToken(command, token, currentPoint);

            points[pointCount] = new b2Vec2(scale * (currentPoint.x + offset.x),
                -scale * (currentPoint.y + offset.y));
            pointCount += 1;
            if (pointCount == capacity) {
                break;
            }
            while (index < svgPath.length() && Character.isWhitespace(svgPath.charAt(index))) {
                index += 1;
            }
        }

        b2Vec2[] result = new b2Vec2[pointCount];
        System.arraycopy(points, 0, result, 0, pointCount);
        return result;
    }

    private static boolean isPathCommand(char command) {
        return command == 'M' || command == 'L' || command == 'H' || command == 'V'
            || command == 'm' || command == 'l' || command == 'h' || command == 'v';
    }

    private static void applyPathToken(char command, String token, b2Vec2 currentPoint) {
        switch (command) {
            case 'M':
            case 'L': {
                int comma = token.indexOf(',');
                currentPoint.x = Float.parseFloat(token.substring(0, comma));
                currentPoint.y = Float.parseFloat(token.substring(comma + 1));
                break;
            }
            case 'H':
                currentPoint.x = Float.parseFloat(token);
                break;
            case 'V':
                currentPoint.y = Float.parseFloat(token);
                break;
            case 'm':
            case 'l': {
                int comma = token.indexOf(',');
                currentPoint.x += Float.parseFloat(token.substring(0, comma));
                currentPoint.y += Float.parseFloat(token.substring(comma + 1));
                break;
            }
            case 'h':
                currentPoint.x += Float.parseFloat(token);
                break;
            case 'v':
                currentPoint.y += Float.parseFloat(token);
                break;
            default:
                throw new IllegalArgumentException("Unsupported SVG path command: " + command);
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

    private static final class RandomState {
        private int seed;

        RandomState(int seed) {
            this.seed = seed;
        }

        b2Polygon randomPolygon(float extent) {
            b2Vec2[] points = b2Vec2.array(B2_MAX_POLYGON_VERTICES);
            int count = 3 + randomInt() % 6;
            for (int i = 0; i < count; ++i) {
                points[i] = randomVec2(-extent, extent);
            }

            b2Hull hull = b2ComputeHull(points, count);
            if (hull.count > 0) {
                return b2MakePolygon(hull, 0.0f);
            }

            return b2MakeSquare(extent);
        }

        float randomFloatRange(float lo, float hi) {
            float r = randomInt() & RAND_LIMIT;
            r /= RAND_LIMIT;
            return (hi - lo) * r + lo;
        }

        int randomIntRange(int lo, int hi) {
            return lo + randomInt() % (hi - lo + 1);
        }

        private b2Vec2 randomVec2(float lo, float hi) {
            return new b2Vec2(randomFloatRange(lo, hi), randomFloatRange(lo, hi));
        }

        private int randomInt() {
            int x = seed;
            x ^= x << 13;
            x ^= x >>> 17;
            x ^= x << 5;
            seed = x;
            return x & RAND_LIMIT;
        }
    }
}
