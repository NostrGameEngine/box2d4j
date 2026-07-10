package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2ContactData;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Manifold;
import org.box2d4j.b2ManifoldPoint;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Platformer {
    private static final int DEFAULT_STEP_COUNT = 120;

    private Platformer() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        Scene scene = createScene(worldId);
        boolean interactive = SampleRuntime.isActive();
        configureRuntime(scene);

        for (int step = 0; step < stepCount; ++step) {
            if (!interactive) {
                beforeStep(scene);
            }
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (!interactive) {
                afterStep(scene);
            }
        }

        ContactState movingContact = contactState(scene.movingPlatformId, 1);
        ContactState playerContact = contactState(scene.playerId, 4);
        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            counters.jointCount, b2World_GetAwakeBodyCount(worldId), scene.preSolveCalls,
            scene.preSolveDisabled, scene.lastCanJump, scene.jumping, scene.jumpDelay, movingContact, playerContact,
            bodyState(scene.playerId), bodyState(scene.movingPlatformId), b2Shape_IsValid(scene.playerShapeId));
        b2DestroyWorld(worldId);
        return result;
    }

    private static void configureRuntime(Scene scene) {
        SampleRuntime.hold("platformer.left", "Move Left", "A", value -> scene.leftPressed = value);
        SampleRuntime.hold("platformer.right", "Move Right", "D", value -> scene.rightPressed = value);
        SampleRuntime.hold("platformer.jump", "Jump", "SPACE", value -> scene.jumpPressed = value);
        SampleRuntime.slider("platformer.force", "Force", scene.force, 0.0f, 50.0f, 1.0f,
            value -> scene.force = value);
        SampleRuntime.slider("platformer.impulse", "Impulse", scene.impulse, 0.0f, 50.0f, 1.0f,
            value -> scene.impulse = value);
        SampleRuntime.beforeStep(() -> beforeStep(scene));
        SampleRuntime.afterStep(() -> afterStep(scene));
    }

    private static void beforeStep(Scene scene) {
        scene.lastCanJump = false;
        b2Vec2 velocity = b2Body_GetLinearVelocity(scene.playerId);
        if (scene.jumpDelay == 0.0f && !scene.jumping && velocity.y < 0.01f) {
            int capacity = Math.min(b2Body_GetContactCapacity(scene.playerId), 4);
            b2ContactData[] contactData = new b2ContactData[Math.max(capacity, 1)];
            int count = b2Body_GetContactData(scene.playerId, contactData, capacity);
            for (int i = 0; i < count; ++i) {
                b2BodyId bodyIdA = b2Shape_GetBody(contactData[i].shapeIdA);
                float sign = B2_ID_EQUALS(bodyIdA, scene.playerId) ? -1.0f : 1.0f;
                if (sign * contactData[i].manifold.normal.y > 0.9f) {
                    scene.lastCanJump = true;
                    break;
                }
            }
        }

        b2Vec2 platformPosition = b2Body_GetPosition(scene.movingPlatformId);
        if (platformPosition.x < -15.0f) {
            b2Body_SetLinearVelocity(scene.movingPlatformId, new b2Vec2(2.0f, 0.0f));
        } else if (platformPosition.x > 15.0f) {
            b2Body_SetLinearVelocity(scene.movingPlatformId, new b2Vec2(-2.0f, 0.0f));
        }
        if (scene.leftPressed) {
            b2Body_ApplyForceToCenter(scene.playerId, new b2Vec2(-scene.force, 0.0f), true);
        }
        if (scene.rightPressed) {
            b2Body_ApplyForceToCenter(scene.playerId, new b2Vec2(scene.force, 0.0f), true);
        }
        if (scene.jumpPressed) {
            if (scene.lastCanJump) {
                b2Body_ApplyLinearImpulseToCenter(scene.playerId, new b2Vec2(0.0f, scene.impulse), true);
                scene.jumpDelay = 0.5f;
                scene.jumping = true;
            }
        } else {
            scene.jumping = false;
        }
    }

    private static void afterStep(Scene scene) {
        scene.jumpDelay = Math.max(0.0f, scene.jumpDelay - 1.0f / 60.0f);
    }

    private static Scene createScene(b2WorldId worldId) {
        Scene scene = new Scene();

        b2World_SetPreSolveCallback(worldId, Platformer::preSolve, scene);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-20.0f, 0.0f), new b2Vec2(20.0f, 0.0f)));

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_staticBody;
        bodyDef.position = new b2Vec2(-6.0f, 6.0f);
        b2BodyId staticPlatformId = b2CreateBody(worldId, bodyDef);
        shapeDef = b2DefaultShapeDef();
        shapeDef.enablePreSolveEvents = true;
        b2Polygon box = b2MakeBox(2.0f, 0.5f);
        b2CreatePolygonShape(staticPlatformId, shapeDef, box);

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_kinematicBody;
        bodyDef.position = new b2Vec2(0.0f, 6.0f);
        bodyDef.linearVelocity = new b2Vec2(2.0f, 0.0f);
        scene.movingPlatformId = b2CreateBody(worldId, bodyDef);
        shapeDef = b2DefaultShapeDef();
        shapeDef.enablePreSolveEvents = true;
        box = b2MakeBox(3.0f, 0.5f);
        b2CreatePolygonShape(scene.movingPlatformId, shapeDef, box);

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.linearDamping = 0.5f;
        bodyDef.position = new b2Vec2(0.0f, 1.0f);
        scene.playerId = b2CreateBody(worldId, bodyDef);

        scene.radius = 0.5f;
        b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, 0.0f), new b2Vec2(0.0f, 1.0f), scene.radius);
        shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.1f;
        scene.playerShapeId = b2CreateCapsuleShape(scene.playerId, shapeDef, capsule);

        scene.force = 25.0f;
        scene.impulse = 25.0f;
        scene.jumpDelay = 0.25f;
        scene.jumping = false;
        return scene;
    }

    private static boolean preSolve(b2ShapeId shapeIdA, b2ShapeId shapeIdB, b2Manifold manifold, Object context) {
        Scene scene = (Scene) context;
        scene.preSolveCalls += 1;

        if (!b2Shape_IsValid(shapeIdA) || !b2Shape_IsValid(shapeIdB)) {
            return false;
        }

        float sign;
        if (B2_ID_EQUALS(shapeIdA, scene.playerShapeId)) {
            sign = -1.0f;
        } else if (B2_ID_EQUALS(shapeIdB, scene.playerShapeId)) {
            sign = 1.0f;
        } else {
            return true;
        }

        if (sign * manifold.normal.y > 0.95f) {
            return true;
        }

        float separation = 0.0f;
        for (int i = 0; i < manifold.pointCount; ++i) {
            b2ManifoldPoint point = manifold.points[i];
            separation = Math.min(separation, point.separation);
        }

        if (separation > 0.1f * scene.radius) {
            return true;
        }

        scene.preSolveDisabled += 1;
        return false;
    }

    private static ContactState contactState(b2BodyId bodyId, int maxCapacity) {
        int capacity = Math.min(b2Body_GetContactCapacity(bodyId), maxCapacity);
        b2ContactData[] contactData = new b2ContactData[Math.max(capacity, 1)];
        int count = b2Body_GetContactData(bodyId, contactData, capacity);
        int pointCount = 0;
        int normalChecksum = 0;
        for (int i = 0; i < count; ++i) {
            pointCount += contactData[i].manifold.pointCount;
            normalChecksum += Math.round(1000.0f * contactData[i].manifold.normal.y);
        }
        return new ContactState(capacity, count, pointCount, normalChecksum);
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x, velocity.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetShapeCount(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    private static final class Scene {
        b2BodyId playerId;
        b2ShapeId playerShapeId;
        b2BodyId movingPlatformId;
        boolean jumping;
        boolean lastCanJump;
        float radius;
        float force;
        float impulse;
        float jumpDelay;
        boolean leftPressed;
        boolean rightPressed;
        boolean jumpPressed;
        int preSolveCalls;
        int preSolveDisabled;
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final int preSolveCalls;
        public final int preSolveDisabled;
        public final boolean canJump;
        public final boolean jumping;
        public final float jumpDelay;
        public final ContactState movingContact;
        public final ContactState playerContact;
        public final BodyState playerBody;
        public final BodyState movingPlatformBody;
        public final boolean playerShapeValid;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount,
               int preSolveCalls, int preSolveDisabled, boolean canJump, boolean jumping, float jumpDelay,
               ContactState movingContact, ContactState playerContact, BodyState playerBody,
               BodyState movingPlatformBody, boolean playerShapeValid) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.preSolveCalls = preSolveCalls;
            this.preSolveDisabled = preSolveDisabled;
            this.canJump = canJump;
            this.jumping = jumping;
            this.jumpDelay = jumpDelay;
            this.movingContact = movingContact;
            this.playerContact = playerContact;
            this.playerBody = playerBody;
            this.movingPlatformBody = movingPlatformBody;
            this.playerShapeValid = playerShapeValid;
        }

        public String toLine() {
            return "platformer " + bodyCount + ' ' + shapeCount + ' ' + contactCount + ' ' + jointCount + ' '
                + awakeBodyCount + ' ' + preSolveCalls + ' ' + preSolveDisabled + ' ' + (canJump ? 1 : 0) + ' '
                + (jumping ? 1 : 0) + ' ' + formatFloat(jumpDelay) + movingContact.toLinePart()
                + playerContact.toLinePart() + playerBody.toLinePart() + movingPlatformBody.toLinePart() + ' '
                + (playerShapeValid ? 1 : 0);
        }
    }

    public static final class ContactState {
        public final int capacity;
        public final int count;
        public final int pointCount;
        public final int normalChecksum;

        ContactState(int capacity, int count, int pointCount, int normalChecksum) {
            this.capacity = capacity;
            this.count = count;
            this.pointCount = pointCount;
            this.normalChecksum = normalChecksum;
        }

        String toLinePart() {
            return " " + capacity + ' ' + count + ' ' + pointCount + ' ' + normalChecksum;
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

        BodyState(float x, float y, float cos, float sin, float velocityX, float velocityY, float angularVelocity,
                  int shapeCount, int contactCapacity) {
            this.x = x;
            this.y = y;
            this.cos = cos;
            this.sin = sin;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.shapeCount = shapeCount;
            this.contactCapacity = contactCapacity;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s %d %d", formatFloat(x), formatFloat(y),
                formatFloat(cos), formatFloat(sin), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), shapeCount, contactCapacity);
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
