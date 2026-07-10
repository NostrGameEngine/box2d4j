package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2Circle;
import org.box2d4j.b2CollisionPlane;
import org.box2d4j.b2Counters;
import org.box2d4j.b2PlaneResult;
import org.box2d4j.b2PlaneSolverResult;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2QueryFilter;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2Rot;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2ShapeProxy;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Mover {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int PLANE_CAPACITY = 8;
    private static final int POGO_SEGMENT = 2;
    private static final long STATIC_BIT = 0x0001L;
    private static final long MOVER_BIT = 0x0002L;
    private static final long DYNAMIC_BIT = 0x0004L;
    private static final long DEBRIS_BIT = 0x0008L;
    private static final long ALL_BITS = 0xFFFFFFFFL;
    private static final b2Vec2 ELEVATOR_BASE = new b2Vec2(112.0f, 10.0f);
    private static final float ELEVATOR_AMPLITUDE = 4.0f;

    private Mover() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        Scene scene = createScene(worldId);
        float timeStep = 1.0f / 60.0f;
        boolean interactive = SampleRuntime.isActive();
        configureRuntime(scene, timeStep);

        for (int step = 0; step < stepCount; ++step) {
            if (!interactive) {
                beforeStep(scene, timeStep);
            }
            b2World_Step(worldId, timeStep, 4);
            if (!interactive) {
                afterStep(scene, timeStep);
            }
        }

        b2Counters counters = b2World_GetCounters(worldId);
        PlaneState[] planes = new PlaneState[scene.planeCount];
        for (int i = 0; i < scene.planeCount; ++i) {
            b2CollisionPlane plane = scene.planes[i];
            planes[i] = new PlaneState(plane.plane.normal.x, plane.plane.normal.y, plane.plane.offset,
                plane.pushLimit, plane.push, plane.clipVelocity);
        }

        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            counters.jointCount, b2World_GetAwakeBodyCount(worldId),
            scene.transform.p.x, scene.transform.p.y, scene.transform.q.c, scene.transform.q.s,
            scene.velocity.x, scene.velocity.y, scene.onGround, scene.pogoVelocity,
            scene.totalIterations, scene.time, castState(scene.castResult), planes,
            bodyState(scene.ballBodyId), bodyState(scene.elevatorId),
            bodyState(scene.bridgeBodies[0]), bodyState(scene.bridgeBodies[24]),
            bodyState(scene.bridgeBodies[49]), b2Shape_IsValid(scene.ballId));
        b2DestroyWorld(worldId);
        return result;
    }

    private static void configureRuntime(Scene scene, float timeStep) {
        SampleRuntime.hold("mover.left", "Move Left", "A", value -> scene.leftPressed = value);
        SampleRuntime.hold("mover.right", "Move Right", "D", value -> scene.rightPressed = value);
        SampleRuntime.hold("mover.jump", "Jump", "SPACE", value -> scene.jumpPressed = value);
        SampleRuntime.action("mover.kick", "Kick", "K", () -> kick(scene));
        SampleRuntime.slider("mover.jumpSpeed", "Jump Speed", scene.jumpSpeed, 0.0f, 40.0f, 1.0f,
            value -> scene.jumpSpeed = value);
        SampleRuntime.slider("mover.minSpeed", "Min Speed", scene.minSpeed, 0.0f, 1.0f, 0.01f,
            value -> scene.minSpeed = value);
        SampleRuntime.slider("mover.maxSpeed", "Max Speed", scene.maxSpeed, 0.0f, 20.0f, 1.0f,
            value -> scene.maxSpeed = value);
        SampleRuntime.slider("mover.stopSpeed", "Stop Speed", scene.stopSpeed, 0.0f, 10.0f, 0.1f,
            value -> scene.stopSpeed = value);
        SampleRuntime.slider("mover.accelerate", "Accelerate", scene.accelerate, 0.0f, 100.0f, 1.0f,
            value -> scene.accelerate = value);
        SampleRuntime.slider("mover.friction", "Friction", scene.friction, 0.0f, 10.0f, 0.1f,
            value -> scene.friction = value);
        SampleRuntime.slider("mover.gravity", "Gravity", scene.gravity, 0.0f, 100.0f, 0.1f,
            value -> scene.gravity = value);
        SampleRuntime.slider("mover.airSteer", "Air Steer", scene.airSteer, 0.0f, 1.0f, 0.01f,
            value -> scene.airSteer = value);
        SampleRuntime.slider("mover.pogoHertz", "Pogo Hertz", scene.pogoHertz, 0.0f, 30.0f, 1.0f,
            value -> scene.pogoHertz = value);
        SampleRuntime.slider("mover.pogoDamping", "Pogo Damping", scene.pogoDampingRatio, 0.0f, 4.0f, 0.1f,
            value -> scene.pogoDampingRatio = value);
        SampleRuntime.choice("mover.pogoShape", "Pogo Shape", scene.pogoShape,
            new String[] {"Point", "Circle", "Segment"}, value -> scene.pogoShape = value);
        SampleRuntime.toggle("mover.lockCamera", "Lock Camera", true, value -> scene.lockCamera = value);
        SampleRuntime.camera(() -> scene.lockCamera
            ? new SampleRuntime.CameraPosition(scene.transform.p.x, scene.transform.p.y, 30.0f) : null);
        SampleRuntime.beforeStep(() -> beforeStep(scene, timeStep));
        SampleRuntime.afterStep(() -> afterStep(scene, timeStep));
    }

    private static void beforeStep(Scene scene, float timeStep) {
        b2Vec2 point = new b2Vec2(ELEVATOR_BASE.x,
            ELEVATOR_AMPLITUDE * (float) Math.cos(scene.time + B2_PI) + ELEVATOR_BASE.y);
        b2Body_SetTargetTransform(scene.elevatorId, new b2Transform(point, b2Rot_identity), timeStep);
        scene.time += timeStep;
    }

    private static void afterStep(Scene scene, float timeStep) {
        float throttle = (scene.leftPressed ? -1.0f : 0.0f) + (scene.rightPressed ? 1.0f : 0.0f);
        if (scene.jumpPressed) {
            if (scene.onGround && scene.jumpReleased) {
                scene.velocity.y = scene.jumpSpeed;
                scene.onGround = false;
                scene.jumpReleased = false;
            }
        } else {
            scene.jumpReleased = true;
        }
        solveMove(scene, timeStep, throttle);
    }

    private static void kick(Scene scene) {
        b2Vec2 point = b2TransformPoint(scene.transform,
            new b2Vec2(0.0f, scene.capsule.center1.y - 3.0f * scene.capsule.radius));
        b2ShapeProxy proxy = b2MakeProxy(new b2Vec2[] {point}, 1, 0.5f);
        b2World_OverlapShape(scene.worldId, proxy, queryFilter(MOVER_BIT, DEBRIS_BIT), shapeId -> {
            b2BodyId bodyId = b2Shape_GetBody(shapeId);
            if (b2Body_GetType(bodyId) == b2_dynamicBody) {
                b2Vec2 direction = b2Normalize(b2Sub(b2Body_GetWorldCenterOfMass(bodyId), scene.transform.p));
                b2Body_ApplyLinearImpulseToCenter(bodyId, new b2Vec2(2.0f * direction.x, 2.0f), true);
            }
            return true;
        });
    }

    private static Scene createScene(b2WorldId worldId) {
        Scene scene = new Scene();
        scene.worldId = worldId;
        scene.transform = new b2Transform(new b2Vec2(2.0f, 8.0f), b2Rot_identity);
        scene.velocity = new b2Vec2();
        scene.capsule = new b2Capsule(new b2Vec2(0.0f, -0.5f), new b2Vec2(0.0f, 0.5f), 0.3f);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId1 = b2CreateBody(worldId, bodyDef);
        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = parsePath(PATH_1, new b2Vec2(-50.0f, -200.0f), 0.2f);
        chainDef.count = chainDef.points.length;
        chainDef.isLoop = true;
        b2CreateChain(groundId1, chainDef);

        bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(98.0f, 0.0f);
        b2BodyId groundId2 = b2CreateBody(worldId, bodyDef);
        chainDef = b2DefaultChainDef();
        chainDef.points = parsePath(PATH_2, new b2Vec2(0.0f, -200.0f), 0.2f);
        chainDef.count = chainDef.points.length;
        chainDef.isLoop = true;
        b2CreateChain(groundId2, chainDef);

        b2Polygon box = b2MakeBox(0.5f, 0.125f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.maxMotorTorque = 10.0f;
        jointDef.enableMotor = true;
        jointDef.hertz = 3.0f;
        jointDef.dampingRatio = 0.8f;
        jointDef.enableSpring = true;

        float xBase = 48.7f;
        float yBase = 9.2f;
        b2BodyId previousBodyId = groundId1;
        for (int i = 0; i < scene.bridgeBodies.length; ++i) {
            bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(xBase + 0.5f + 1.0f * i, yBase);
            bodyDef.angularDamping = 0.2f;
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            scene.bridgeBodies[i] = bodyId;
            b2CreatePolygonShape(bodyId, shapeDef, box);

            b2Vec2 pivot = new b2Vec2(xBase + 1.0f * i, yBase);
            jointDef.bodyIdA = previousBodyId;
            jointDef.bodyIdB = bodyId;
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            b2CreateRevoluteJoint(worldId, jointDef);
            previousBodyId = bodyId;
        }

        b2Vec2 pivot = new b2Vec2(xBase + scene.bridgeBodies.length, yBase);
        jointDef.bodyIdA = previousBodyId;
        jointDef.bodyIdB = groundId2;
        jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
        jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
        b2CreateRevoluteJoint(worldId, jointDef);

        bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(32.0f, 4.5f);
        shapeDef = b2DefaultShapeDef();
        scene.friendlyShape = new ShapeUserData(0.025f, false);
        shapeDef.filter.categoryBits = MOVER_BIT;
        shapeDef.filter.maskBits = ALL_BITS;
        shapeDef.userData = scene.friendlyShape;
        b2BodyId friendlyBodyId = b2CreateBody(worldId, bodyDef);
        b2CreateCapsuleShape(friendlyBodyId, shapeDef, scene.capsule);

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(7.0f, 7.0f);
        scene.ballBodyId = b2CreateBody(worldId, bodyDef);
        shapeDef = b2DefaultShapeDef();
        shapeDef.filter.categoryBits = DEBRIS_BIT;
        shapeDef.filter.maskBits = ALL_BITS;
        shapeDef.material.restitution = 0.7f;
        shapeDef.material.rollingResistance = 0.2f;
        scene.ballId = b2CreateCircleShape(scene.ballBodyId, shapeDef,
            new b2Circle(new b2Vec2(), 0.3f));

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_kinematicBody;
        bodyDef.position = new b2Vec2(ELEVATOR_BASE.x, ELEVATOR_BASE.y - ELEVATOR_AMPLITUDE);
        scene.elevatorId = b2CreateBody(worldId, bodyDef);
        scene.elevatorShape = new ShapeUserData(0.1f, true);
        shapeDef = b2DefaultShapeDef();
        shapeDef.filter.categoryBits = DYNAMIC_BIT;
        shapeDef.filter.maskBits = ALL_BITS;
        shapeDef.userData = scene.elevatorShape;
        b2CreatePolygonShape(scene.elevatorId, shapeDef, b2MakeBox(2.0f, 0.1f));

        for (int i = 0; i < scene.planes.length; ++i) {
            scene.planes[i] = new b2CollisionPlane();
        }
        return scene;
    }

    private static void solveMove(Scene scene, float timeStep, float throttle) {
        float speed = b2Length(scene.velocity);
        if (speed < scene.minSpeed) {
            scene.velocity.x = 0.0f;
            scene.velocity.y = 0.0f;
        } else if (scene.onGround) {
            float control = speed < scene.stopSpeed ? scene.stopSpeed : speed;
            float drop = control * scene.friction * timeStep;
            float newSpeed = Math.max(0.0f, speed - drop);
            scene.velocity = b2MulSV(newSpeed / speed, scene.velocity);
        }

        b2Vec2 desiredVelocity = new b2Vec2(scene.maxSpeed * throttle, 0.0f);
        float[] desiredSpeed = new float[1];
        b2Vec2 desiredDirection = b2GetLengthAndNormalize(desiredSpeed, desiredVelocity);
        if (desiredSpeed[0] > scene.maxSpeed) {
            desiredSpeed[0] = scene.maxSpeed;
        }
        if (scene.onGround) {
            scene.velocity.y = 0.0f;
        }

        float currentSpeed = b2Dot(scene.velocity, desiredDirection);
        float addSpeed = desiredSpeed[0] - currentSpeed;
        if (addSpeed > 0.0f) {
            float steer = scene.onGround ? 1.0f : scene.airSteer;
            float accelSpeed = steer * scene.accelerate * scene.maxSpeed * timeStep;
            if (accelSpeed > addSpeed) {
                accelSpeed = addSpeed;
            }
            scene.velocity = b2MulAdd(scene.velocity, accelSpeed, desiredDirection);
        }
        scene.velocity.y -= scene.gravity * timeStep;

        float pogoRestLength = 3.0f * scene.capsule.radius;
        float rayLength = pogoRestLength + scene.capsule.radius;
        b2Vec2 origin = b2TransformPoint(scene.transform, scene.capsule.center1);
        b2Vec2 segmentOffset = new b2Vec2(0.75f * scene.capsule.radius, 0.0f);
        b2Vec2 segmentPoint1 = b2Sub(origin, segmentOffset);
        b2Vec2 segmentPoint2 = b2Add(origin, segmentOffset);

        b2ShapeProxy proxy;
        b2Vec2 translation;
        if (scene.pogoShape == 0) {
            proxy = b2MakeProxy(new b2Vec2[] {origin}, 1, 0.0f);
            translation = new b2Vec2(0.0f, -rayLength);
        } else if (scene.pogoShape == 1) {
            float radius = 0.5f * scene.capsule.radius;
            proxy = b2MakeProxy(new b2Vec2[] {origin}, 1, radius);
            translation = new b2Vec2(0.0f, -rayLength + radius);
        } else {
            proxy = b2MakeProxy(new b2Vec2[] {segmentPoint1, segmentPoint2}, 2, 0.0f);
            translation = new b2Vec2(0.0f, -rayLength);
        }

        b2QueryFilter pogoFilter = queryFilter(MOVER_BIT, STATIC_BIT | DYNAMIC_BIT);
        scene.castResult = new CastResult();
        b2World_CastShape(scene.worldId, proxy, translation, pogoFilter,
            (shapeId, point, normal, fraction) -> castCallback(scene.castResult, shapeId, point, normal, fraction));

        if (!scene.onGround) {
            scene.onGround = scene.castResult.hit && scene.velocity.y <= 0.01f;
        } else {
            scene.onGround = scene.castResult.hit;
        }

        if (!scene.castResult.hit) {
            scene.pogoVelocity = 0.0f;
        } else {
            float pogoCurrentLength = scene.castResult.fraction * rayLength;
            float offset = pogoCurrentLength - pogoRestLength;
            scene.pogoVelocity = b2SpringDamper(scene.pogoHertz, scene.pogoDampingRatio,
                offset, scene.pogoVelocity, timeStep);
            b2Body_ApplyForce(scene.castResult.bodyId, new b2Vec2(0.0f, -50.0f),
                scene.castResult.point, true);
        }

        b2Vec2 target = b2MulAdd(b2MulAdd(scene.transform.p, timeStep, scene.velocity),
            timeStep * scene.pogoVelocity, new b2Vec2(0.0f, 1.0f));
        b2QueryFilter collideFilter = queryFilter(MOVER_BIT, STATIC_BIT | DYNAMIC_BIT | MOVER_BIT);
        b2QueryFilter castFilter = queryFilter(MOVER_BIT, STATIC_BIT | DYNAMIC_BIT);
        scene.totalIterations = 0;
        float tolerance = 0.01f;

        for (int iteration = 0; iteration < 5; ++iteration) {
            scene.planeCount = 0;
            b2Capsule mover = new b2Capsule(
                b2TransformPoint(scene.transform, scene.capsule.center1),
                b2TransformPoint(scene.transform, scene.capsule.center2), scene.capsule.radius);
            b2World_CollideMover(scene.worldId, mover, collideFilter,
                (shapeId, planeResult) -> planeResult(scene, shapeId, planeResult));
            b2PlaneSolverResult result = b2SolvePlanes(b2Sub(target, scene.transform.p),
                scene.planes, scene.planeCount);
            scene.totalIterations += result.iterationCount;
            float fraction = b2World_CastMover(scene.worldId, mover, result.translation, castFilter);
            b2Vec2 delta = b2MulSV(fraction, result.translation);
            scene.transform.p = b2Add(scene.transform.p, delta);
            if (b2LengthSquared(delta) < tolerance * tolerance) {
                break;
            }
        }
        scene.velocity = b2ClipVector(scene.velocity, scene.planes, scene.planeCount);
    }

    private static float castCallback(CastResult result, b2ShapeId shapeId, b2Vec2 point,
                                      b2Vec2 normal, float fraction) {
        result.point = point.copy();
        result.normal = normal.copy();
        result.bodyId = b2Shape_GetBody(shapeId);
        result.fraction = fraction;
        result.hit = true;
        return fraction;
    }

    private static boolean planeResult(Scene scene, b2ShapeId shapeId, b2PlaneResult planeResult) {
        ShapeUserData userData = (ShapeUserData) b2Shape_GetUserData(shapeId);
        float pushLimit = Float.MAX_VALUE;
        boolean clipVelocity = true;
        if (userData != null) {
            pushLimit = userData.maxPush;
            clipVelocity = userData.clipVelocity;
        }
        if (scene.planeCount < PLANE_CAPACITY) {
            b2CollisionPlane plane = scene.planes[scene.planeCount++];
            plane.plane.normal = planeResult.plane.normal.copy();
            plane.plane.offset = planeResult.plane.offset;
            plane.pushLimit = pushLimit;
            plane.push = 0.0f;
            plane.clipVelocity = clipVelocity;
        }
        return true;
    }

    private static b2QueryFilter queryFilter(long categoryBits, long maskBits) {
        b2QueryFilter filter = new b2QueryFilter();
        filter.categoryBits = categoryBits;
        filter.maskBits = maskBits;
        return filter;
    }

    private static b2Vec2[] parsePath(String path, b2Vec2 offset, float scale) {
        String[] tokens = path.trim().split("\\s+");
        List<b2Vec2> points = new ArrayList<>();
        char command = 0;
        float currentX = 0.0f;
        float currentY = 0.0f;
        for (String token : tokens) {
            if (token.length() == 1 && Character.isLetter(token.charAt(0))) {
                command = token.charAt(0);
                if (command == 'z') {
                    break;
                }
                continue;
            }
            int comma = token.indexOf(',');
            float x;
            float y;
            switch (command) {
                case 'M':
                case 'L':
                    currentX = Float.parseFloat(token.substring(0, comma));
                    currentY = Float.parseFloat(token.substring(comma + 1));
                    break;
                case 'H':
                    currentX = Float.parseFloat(token);
                    break;
                case 'V':
                    currentY = Float.parseFloat(token);
                    break;
                case 'm':
                case 'l':
                    x = Float.parseFloat(token.substring(0, comma));
                    y = Float.parseFloat(token.substring(comma + 1));
                    currentX += x;
                    currentY += y;
                    break;
                case 'h':
                    currentX += Float.parseFloat(token);
                    break;
                case 'v':
                    currentY += Float.parseFloat(token);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported SVG path command: " + command);
            }
            points.add(new b2Vec2(scale * (currentX + offset.x), -scale * (currentY + offset.y)));
        }
        return points.toArray(new b2Vec2[0]);
    }

    private static CastState castState(CastResult result) {
        return new CastState(result.hit, result.fraction, result.point.x, result.point.y,
            result.normal.x, result.normal.y, result.hit && b2Body_IsValid(result.bodyId));
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s,
            velocity.x, velocity.y, b2Body_GetAngularVelocity(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    private static final class Scene {
        b2WorldId worldId;
        b2Transform transform;
        b2Vec2 velocity;
        b2Capsule capsule;
        b2BodyId elevatorId;
        b2BodyId ballBodyId;
        b2ShapeId ballId;
        final b2BodyId[] bridgeBodies = new b2BodyId[50];
        ShapeUserData friendlyShape;
        ShapeUserData elevatorShape;
        final b2CollisionPlane[] planes = new b2CollisionPlane[PLANE_CAPACITY];
        CastResult castResult = new CastResult();
        int planeCount;
        int totalIterations;
        int pogoShape = POGO_SEGMENT;
        float maxSpeed = 6.0f;
        float minSpeed = 0.1f;
        float stopSpeed = 3.0f;
        float accelerate = 20.0f;
        float airSteer = 0.2f;
        float friction = 8.0f;
        float gravity = 30.0f;
        float pogoHertz = 5.0f;
        float pogoDampingRatio = 0.8f;
        float jumpSpeed = 10.0f;
        float pogoVelocity;
        float time;
        boolean onGround;
        boolean jumpReleased = true;
        boolean leftPressed;
        boolean rightPressed;
        boolean jumpPressed;
        boolean lockCamera = true;
    }

    private static final class ShapeUserData {
        final float maxPush;
        final boolean clipVelocity;

        ShapeUserData(float maxPush, boolean clipVelocity) {
            this.maxPush = maxPush;
            this.clipVelocity = clipVelocity;
        }
    }

    private static final class CastResult {
        b2Vec2 point = new b2Vec2();
        b2Vec2 normal = new b2Vec2();
        b2BodyId bodyId = new b2BodyId();
        float fraction;
        boolean hit;
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final float moverX;
        public final float moverY;
        public final float moverCos;
        public final float moverSin;
        public final float velocityX;
        public final float velocityY;
        public final boolean onGround;
        public final float pogoVelocity;
        public final int totalIterations;
        public final float time;
        public final CastState cast;
        public final PlaneState[] planes;
        public final BodyState ball;
        public final BodyState elevator;
        public final BodyState bridgeFirst;
        public final BodyState bridgeMiddle;
        public final BodyState bridgeLast;
        public final boolean ballShapeValid;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount,
               float moverX, float moverY, float moverCos, float moverSin, float velocityX, float velocityY,
               boolean onGround, float pogoVelocity, int totalIterations, float time, CastState cast,
               PlaneState[] planes, BodyState ball, BodyState elevator, BodyState bridgeFirst,
               BodyState bridgeMiddle, BodyState bridgeLast, boolean ballShapeValid) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.moverX = moverX;
            this.moverY = moverY;
            this.moverCos = moverCos;
            this.moverSin = moverSin;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.onGround = onGround;
            this.pogoVelocity = pogoVelocity;
            this.totalIterations = totalIterations;
            this.time = time;
            this.cast = cast;
            this.planes = planes;
            this.ball = ball;
            this.elevator = elevator;
            this.bridgeFirst = bridgeFirst;
            this.bridgeMiddle = bridgeMiddle;
            this.bridgeLast = bridgeLast;
            this.ballShapeValid = ballShapeValid;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("mover ").append(bodyCount).append(' ').append(shapeCount).append(' ')
                .append(contactCount).append(' ').append(jointCount).append(' ').append(awakeBodyCount)
                .append(formatFloats(moverX, moverY, moverCos, moverSin, velocityX, velocityY))
                .append(' ').append(onGround ? 1 : 0).append(formatFloats(pogoVelocity))
                .append(' ').append(totalIterations).append(formatFloats(time)).append(cast.toLinePart())
                .append(' ').append(planes.length);
            for (PlaneState plane : planes) {
                builder.append(plane.toLinePart());
            }
            builder.append(ball.toLinePart()).append(elevator.toLinePart()).append(bridgeFirst.toLinePart())
                .append(bridgeMiddle.toLinePart()).append(bridgeLast.toLinePart())
                .append(' ').append(ballShapeValid ? 1 : 0);
            return builder.toString();
        }
    }

    public static final class CastState {
        public final boolean hit;
        public final float fraction;
        public final float pointX;
        public final float pointY;
        public final float normalX;
        public final float normalY;
        public final boolean bodyValid;

        CastState(boolean hit, float fraction, float pointX, float pointY, float normalX, float normalY,
                  boolean bodyValid) {
            this.hit = hit;
            this.fraction = fraction;
            this.pointX = pointX;
            this.pointY = pointY;
            this.normalX = normalX;
            this.normalY = normalY;
            this.bodyValid = bodyValid;
        }

        String toLinePart() {
            return " " + (hit ? 1 : 0) + formatFloats(fraction, pointX, pointY, normalX, normalY)
                + " " + (bodyValid ? 1 : 0);
        }
    }

    public static final class PlaneState {
        public final float normalX;
        public final float normalY;
        public final float offset;
        public final float pushLimit;
        public final float push;
        public final boolean clipVelocity;

        PlaneState(float normalX, float normalY, float offset, float pushLimit, float push,
                   boolean clipVelocity) {
            this.normalX = normalX;
            this.normalY = normalY;
            this.offset = offset;
            this.pushLimit = pushLimit;
            this.push = push;
            this.clipVelocity = clipVelocity;
        }

        String toLinePart() {
            return formatFloats(normalX, normalY, offset, pushLimit, push) + " " + (clipVelocity ? 1 : 0);
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

        BodyState(float x, float y, float cos, float sin, float velocityX, float velocityY,
                  float angularVelocity, int contactCapacity) {
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
            return formatFloats(x, y, cos, sin, velocityX, velocityY, angularVelocity)
                + " " + contactCapacity;
        }
    }

    private static String formatFloats(float... values) {
        StringBuilder builder = new StringBuilder();
        for (float value : values) {
            builder.append(' ').append(String.format(Locale.ROOT, "%.9g", value));
        }
        return builder.toString();
    }

    private static final String PATH_1 =
        "M 2.6458333,201.08333 H 293.68751 v -47.625 h -2.64584 l -10.58333,7.9375 -13.22916,7.9375 -13.24648,5.29167 "
            + "-31.73269,7.9375 -21.16667,2.64583 -23.8125,10.58333 H 142.875 v -5.29167 h -5.29166 v 5.29167 H 119.0625 v "
            + "-2.64583 h -2.64583 v -2.64584 h -2.64584 v -2.64583 H 111.125 v -2.64583 H 84.666668 v -2.64583 h -5.291666 v "
            + "-2.64584 h -5.291667 v -2.64583 H 68.791668 V 174.625 h -5.291666 v -2.64584 H 52.916669 L 39.6875,177.27083 H "
            + "34.395833 L 23.8125,185.20833 H 15.875 L 5.2916669,187.85416 V 153.45833 H 2.6458333 v 47.625";

    private static final String PATH_2 =
        "M 2.6458333,201.08333 H 293.68751 l 0,-23.8125 h -23.8125 l 21.16667,21.16667 h -23.8125 l -39.68751,-13.22917 "
            + "-26.45833,7.9375 -23.8125,2.64583 h -13.22917 l -0.0575,2.64584 h -5.29166 v -2.64583 l -7.86855,-1e-5 "
            + "-0.0114,-2.64583 h -2.64583 l -2.64583,2.64584 h -7.9375 l -2.64584,2.64583 -2.58891,-2.64584 h -13.28609 v "
            + "-2.64583 h -2.64583 v -2.64584 l -5.29167,1e-5 v -2.64583 h -2.64583 v -2.64583 l -5.29167,-1e-5 v -2.64583 h "
            + "-2.64583 v -2.64584 h -5.291667 v -2.64583 H 92.60417 V 174.625 h -5.291667 v -2.64584 l -34.395835,1e-5 "
            + "-7.9375,-2.64584 -7.9375,-2.64583 -5.291667,-5.29167 H 21.166667 L 13.229167,158.75 5.2916668,153.45833 H "
            + "2.6458334 l -10e-8,47.625";
}
