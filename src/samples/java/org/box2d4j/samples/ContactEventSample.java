package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2Circle;
import org.box2d4j.b2ContactBeginTouchEvent;
import org.box2d4j.b2ContactData;
import org.box2d4j.b2ContactEvents;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Manifold;
import org.box2d4j.b2ManifoldPoint;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Rot;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ContactEventSample {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int COUNT = 20;
    private static final int RAND_LIMIT = 32767;
    private static final int RAND_SEED = 12345;

    private ContactEventSample() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        Scene scene = createScene();
        boolean interactive = SampleRuntime.isActive();
        configureRuntime(scene);

        for (int step = 0; step < stepCount; ++step) {
            if (!interactive) {
                beforeStep(scene);
            }
            b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
            if (!interactive) {
                afterStep(scene);
            }
        }

        int activeDebrisCount = 0;
        long activeMask = 0L;
        for (int i = 0; i < COUNT; ++i) {
            if (!B2_IS_NULL(scene.debrisIds[i])) {
                activeDebrisCount += 1;
                activeMask |= 1L << i;
            }
        }

        BodyState[] debris = new BodyState[activeDebrisCount];
        int debrisIndex = 0;
        for (int i = 0; i < COUNT; ++i) {
            if (!B2_IS_NULL(scene.debrisIds[i])) {
                debris[debrisIndex++] = bodyState(i, scene.debrisIds[i]);
            }
        }

        b2Counters counters = b2World_GetCounters(scene.worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            counters.jointCount, b2World_GetAwakeBodyCount(scene.worldId), scene.spawnTotal, scene.attachTotal,
            scene.destroyShapeTotal, scene.beginTotal, scene.endTotal, scene.hitTotal, scene.contactPointTotal,
            scene.normalChecksum, scene.impulseChecksum, activeDebrisCount, activeMask, scene.wait,
            bodyState(-1, scene.playerId), debris);
        b2DestroyWorld(scene.worldId);
        return result;
    }

    private static void configureRuntime(Scene scene) {
        SampleRuntime.hold("contact.left", "Move Left", "A", value -> scene.leftPressed = value);
        SampleRuntime.hold("contact.right", "Move Right", "D", value -> scene.rightPressed = value);
        SampleRuntime.hold("contact.up", "Move Up", "W", value -> scene.upPressed = value);
        SampleRuntime.hold("contact.down", "Move Down", "S", value -> scene.downPressed = value);
        SampleRuntime.slider("contact.force", "Force", scene.force, 100.0f, 500.0f, 10.0f,
            value -> scene.force = value);
        SampleRuntime.beforeStep(() -> beforeStep(scene));
        SampleRuntime.afterStep(() -> afterStep(scene));
    }

    private static void beforeStep(Scene scene) {
        b2Vec2 force = new b2Vec2(
            (scene.rightPressed ? scene.force : 0.0f) - (scene.leftPressed ? scene.force : 0.0f),
            (scene.upPressed ? scene.force : 0.0f) - (scene.downPressed ? scene.force : 0.0f));
        if (force.x != 0.0f || force.y != 0.0f) {
            b2Body_ApplyForce(scene.playerId, force, b2Body_GetPosition(scene.playerId), true);
        }
    }

    private static void afterStep(Scene scene) {
        scene.processContactEvents();
        scene.wait -= 1.0f / 60.0f;
        if (scene.wait < 0.0f) {
            scene.spawnDebris();
            scene.wait += 0.5f;
        }
    }

    private static Scene createScene() {
        Scene scene = new Scene();
        scene.worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(scene.worldId, bodyDef);
        b2Vec2[] points = {
            new b2Vec2(40.0f, -40.0f),
            new b2Vec2(-40.0f, -40.0f),
            new b2Vec2(-40.0f, 40.0f),
            new b2Vec2(40.0f, 40.0f)
        };
        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.count = points.length;
        chainDef.points = points;
        chainDef.isLoop = true;
        b2CreateChain(groundId, chainDef);

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.gravityScale = 0.0f;
        bodyDef.linearDamping = 0.5f;
        bodyDef.angularDamping = 0.5f;
        bodyDef.isBullet = true;
        scene.playerId = b2CreateBody(scene.worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.enableContactEvents = true;
        scene.coreShapeId = b2CreateCircleShape(scene.playerId, shapeDef,
            new b2Circle(new b2Vec2(0.0f, 0.0f), 1.0f));

        for (int i = 0; i < COUNT; ++i) {
            scene.debrisIds[i] = b2_nullBodyId;
            scene.userData[i] = new BodyUserData(i);
        }
        scene.wait = 0.5f;
        scene.force = 200.0f;
        return scene;
    }

    private static BodyState bodyState(int index, b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(index, transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x,
            velocity.y, b2Body_GetAngularVelocity(bodyId), b2Body_GetShapeCount(bodyId),
            b2Body_GetContactCapacity(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    private static final class Scene {
        final b2BodyId[] debrisIds = new b2BodyId[COUNT];
        final BodyUserData[] userData = new BodyUserData[COUNT];
        final RandomState random = new RandomState(RAND_SEED);
        b2WorldId worldId;
        b2BodyId playerId;
        b2ShapeId coreShapeId;
        float force;
        float wait;
        int spawnTotal;
        int attachTotal;
        int destroyShapeTotal;
        int beginTotal;
        int endTotal;
        int hitTotal;
        int contactPointTotal;
        int normalChecksum;
        int impulseChecksum;
        boolean leftPressed;
        boolean rightPressed;
        boolean upPressed;
        boolean downPressed;

        void spawnDebris() {
            int index = -1;
            for (int i = 0; i < COUNT; ++i) {
                if (B2_IS_NULL(debrisIds[i])) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                return;
            }

            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(random.range(-38.0f, 38.0f), random.range(-38.0f, 38.0f));
            bodyDef.rotation = b2MakeRot(random.range(-B2_PI, B2_PI));
            bodyDef.linearVelocity = new b2Vec2(random.range(-5.0f, 5.0f), random.range(-5.0f, 5.0f));
            bodyDef.angularVelocity = random.range(-1.0f, 1.0f);
            bodyDef.gravityScale = 0.0f;
            bodyDef.userData = userData[index];
            debrisIds[index] = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.material.restitution = 0.8f;
            shapeDef.enableContactEvents = false;
            if ((index + 1) % 3 == 0) {
                b2CreateCircleShape(debrisIds[index], shapeDef, new b2Circle(new b2Vec2(0.0f, 0.0f), 0.5f));
            } else if ((index + 1) % 2 == 0) {
                b2CreateCapsuleShape(debrisIds[index], shapeDef,
                    new b2Capsule(new b2Vec2(0.0f, -0.25f), new b2Vec2(0.0f, 0.25f), 0.25f));
            } else {
                b2CreatePolygonShape(debrisIds[index], shapeDef, b2MakeBox(0.4f, 0.6f));
            }
            spawnTotal += 1;
        }

        void processContactEvents() {
            int[] debrisToAttach = new int[COUNT];
            b2ShapeId[] shapesToDestroy = new b2ShapeId[COUNT];
            int attachCount = 0;
            int destroyCount = 0;

            b2ContactEvents events = b2World_GetContactEvents(worldId);
            beginTotal += events.beginCount;
            endTotal += events.endCount;
            hitTotal += events.hitCount;

            for (b2ContactBeginTouchEvent event : events.beginEvents) {
                b2BodyId bodyIdA = b2Shape_GetBody(event.shapeIdA);
                b2BodyId bodyIdB = b2Shape_GetBody(event.shapeIdB);
                accumulateContactData(event);

                if (B2_ID_EQUALS(bodyIdA, playerId)) {
                    BodyUserData userDataB = (BodyUserData) b2Body_GetUserData(bodyIdB);
                    if (userDataB == null) {
                        if (!B2_ID_EQUALS(event.shapeIdA, coreShapeId) && destroyCount < COUNT
                            && !containsShape(shapesToDestroy, destroyCount, event.shapeIdA)) {
                            shapesToDestroy[destroyCount++] = event.shapeIdA;
                        }
                    } else if (attachCount < COUNT) {
                        debrisToAttach[attachCount++] = userDataB.index;
                    }
                } else {
                    BodyUserData userDataA = (BodyUserData) b2Body_GetUserData(bodyIdA);
                    if (userDataA == null) {
                        if (!B2_ID_EQUALS(event.shapeIdB, coreShapeId) && destroyCount < COUNT
                            && !containsShape(shapesToDestroy, destroyCount, event.shapeIdB)) {
                            shapesToDestroy[destroyCount++] = event.shapeIdB;
                        }
                    } else if (attachCount < COUNT) {
                        debrisToAttach[attachCount++] = userDataA.index;
                    }
                }
            }

            for (int i = 0; i < attachCount; ++i) {
                attachDebris(debrisToAttach[i]);
            }

            for (int i = 0; i < destroyCount; ++i) {
                b2DestroyShape(shapesToDestroy[i], false);
                destroyShapeTotal += 1;
            }
            if (destroyCount > 0) {
                b2Body_ApplyMassFromShapes(playerId);
            }
        }

        private void accumulateContactData(b2ContactBeginTouchEvent event) {
            int capacityA = b2Shape_GetContactCapacity(event.shapeIdA);
            int capacityB = b2Shape_GetContactCapacity(event.shapeIdB);
            b2ShapeId queryShape = capacityA < capacityB ? event.shapeIdA : event.shapeIdB;
            b2ShapeId otherShape = capacityA < capacityB ? event.shapeIdB : event.shapeIdA;
            int capacity = Math.min(capacityA, capacityB);
            b2ContactData[] contactData = new b2ContactData[Math.max(capacity, 1)];
            int count = b2Shape_GetContactData(queryShape, contactData, capacity);
            for (int i = 0; i < count; ++i) {
                b2ContactData data = contactData[i];
                if (B2_ID_EQUALS(data.shapeIdA, otherShape) || B2_ID_EQUALS(data.shapeIdB, otherShape)) {
                    b2Manifold manifold = data.manifold;
                    contactPointTotal += manifold.pointCount;
                    normalChecksum += Math.round(1000.0f * manifold.normal.x)
                        + Math.round(1000.0f * manifold.normal.y);
                    for (int k = 0; k < manifold.pointCount; ++k) {
                        b2ManifoldPoint point = manifold.points[k];
                        impulseChecksum += Math.round(1000.0f * point.totalNormalImpulse);
                    }
                }
            }
        }

        private void attachDebris(int index) {
            b2BodyId debrisId = debrisIds[index];
            if (B2_IS_NULL(debrisId)) {
                return;
            }

            b2Transform playerTransform = b2Body_GetTransform(playerId);
            b2Transform debrisTransform = b2Body_GetTransform(debrisId);
            b2Transform relativeTransform = b2InvMulTransforms(playerTransform, debrisTransform);

            if (b2Body_GetShapeCount(debrisId) == 0) {
                return;
            }

            b2ShapeId[] shapeIds = new b2ShapeId[1];
            b2Body_GetShapes(debrisId, shapeIds, 1);
            b2ShapeId shapeId = shapeIds[0];

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.enableContactEvents = true;
            int type = b2Shape_GetType(shapeId);
            if (type == b2_circleShape) {
                b2Circle circle = b2Shape_GetCircle(shapeId);
                circle.center = b2TransformPoint(relativeTransform, circle.center);
                b2CreateCircleShape(playerId, shapeDef, circle);
            } else if (type == b2_capsuleShape) {
                b2Capsule capsule = b2Shape_GetCapsule(shapeId);
                capsule.center1 = b2TransformPoint(relativeTransform, capsule.center1);
                capsule.center2 = b2TransformPoint(relativeTransform, capsule.center2);
                b2CreateCapsuleShape(playerId, shapeDef, capsule);
            } else if (type == b2_polygonShape) {
                b2CreatePolygonShape(playerId, shapeDef, b2TransformPolygon(relativeTransform, b2Shape_GetPolygon(shapeId)));
            }

            b2DestroyBody(debrisId);
            debrisIds[index] = b2_nullBodyId;
            attachTotal += 1;
        }
    }

    private static boolean containsShape(b2ShapeId[] shapeIds, int count, b2ShapeId shapeId) {
        for (int i = 0; i < count; ++i) {
            if (B2_ID_EQUALS(shapeIds[i], shapeId)) {
                return true;
            }
        }
        return false;
    }

    private static final class BodyUserData {
        final int index;

        BodyUserData(int index) {
            this.index = index;
        }
    }

    private static final class RandomState {
        private int seed;

        RandomState(int seed) {
            this.seed = seed;
        }

        float range(float lo, float hi) {
            float r = randomInt() & RAND_LIMIT;
            r /= RAND_LIMIT;
            return (hi - lo) * r + lo;
        }

        private int randomInt() {
            int x = seed;
            x ^= x << 13;
            x ^= x >>> 17;
            x ^= x << 5;
            seed = x;
            return (x & 0x7FFFFFFF) % (RAND_LIMIT + 1);
        }
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final int spawnTotal;
        public final int attachTotal;
        public final int destroyShapeTotal;
        public final int beginTotal;
        public final int endTotal;
        public final int hitTotal;
        public final int contactPointTotal;
        public final int normalChecksum;
        public final int impulseChecksum;
        public final int activeDebrisCount;
        public final long activeMask;
        public final float wait;
        public final BodyState player;
        public final BodyState[] debris;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, int spawnTotal,
               int attachTotal, int destroyShapeTotal, int beginTotal, int endTotal, int hitTotal,
               int contactPointTotal, int normalChecksum, int impulseChecksum, int activeDebrisCount, long activeMask,
               float wait, BodyState player, BodyState[] debris) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.spawnTotal = spawnTotal;
            this.attachTotal = attachTotal;
            this.destroyShapeTotal = destroyShapeTotal;
            this.beginTotal = beginTotal;
            this.endTotal = endTotal;
            this.hitTotal = hitTotal;
            this.contactPointTotal = contactPointTotal;
            this.normalChecksum = normalChecksum;
            this.impulseChecksum = impulseChecksum;
            this.activeDebrisCount = activeDebrisCount;
            this.activeMask = activeMask;
            this.wait = wait;
            this.player = player;
            this.debris = debris;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("contactEvent ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(jointCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(spawnTotal).append(' ')
                .append(attachTotal).append(' ')
                .append(destroyShapeTotal).append(' ')
                .append(beginTotal).append(' ')
                .append(endTotal).append(' ')
                .append(hitTotal).append(' ')
                .append(contactPointTotal).append(' ')
                .append(normalChecksum).append(' ')
                .append(impulseChecksum).append(' ')
                .append(activeDebrisCount).append(' ')
                .append(activeMask).append(' ')
                .append(formatFloat(wait))
                .append(player.toLinePart()).append(' ')
                .append(debris.length);
            for (BodyState body : debris) {
                builder.append(body.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class BodyState {
        public final int index;
        public final float x;
        public final float y;
        public final float cos;
        public final float sin;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final int shapeCount;
        public final int contactCapacity;

        BodyState(int index, float x, float y, float cos, float sin, float velocityX, float velocityY,
                  float angularVelocity, int shapeCount, int contactCapacity) {
            this.index = index;
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
            return String.format(Locale.ROOT, " %d %s %s %s %s %s %s %s %d %d", index, formatFloat(x),
                formatFloat(y), formatFloat(cos), formatFloat(sin), formatFloat(velocityX), formatFloat(velocityY),
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
