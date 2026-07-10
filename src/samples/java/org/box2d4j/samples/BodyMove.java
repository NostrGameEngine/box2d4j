package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyEvents;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2BodyMoveEvent;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2ExplosionDef;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class BodyMove {
    private static final int DEFAULT_STEP_COUNT = 108;
    private static final int MAX_COUNT = 50;
    private static final int RAND_LIMIT = 32767;
    private static final int RAND_SEED = 12345;

    private BodyMove() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        Scene scene = new Scene();
        scene.worldId = b2CreateWorld(b2DefaultWorldDef());
        scene.createGround();

        float[] explosionMagnitude = {10.0f};
        Runnable explode = () -> {
            b2ExplosionDef def = b2DefaultExplosionDef();
            def.position = new b2Vec2(0.0f, -5.0f);
            def.radius = 10.0f;
            def.falloff = 0.1f;
            def.impulsePerLength = explosionMagnitude[0];
            b2World_Explode(scene.worldId, def);
        };
        SampleRuntime.action("bodyMove.explode", "Explode", explode);
        SampleRuntime.slider("bodyMove.magnitude", "Magnitude", explosionMagnitude[0], -20.0f, 20.0f, 0.1f,
            value -> explosionMagnitude[0] = value);
        int[] runtimeStep = {0};
        SampleRuntime.beforeStep(() -> {
            if ((runtimeStep[0] & 15) == 15 && scene.count < MAX_COUNT) {
                scene.createBodies();
            }
            runtimeStep[0] += 1;
        });
        SampleRuntime.afterStep(scene::processBodyEvents);
        boolean interactive = SampleRuntime.isActive();

        for (int step = 0; step < stepCount; ++step) {
            if (!interactive && (step & 15) == 15 && scene.count < MAX_COUNT) {
                scene.createBodies();
            }

            b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
            if (!interactive) {
                scene.processBodyEvents();
            }
        }

        BodyState[] states = new BodyState[scene.count];
        for (int i = 0; i < scene.count; ++i) {
            states[i] = bodyState(scene.bodyIds[i], scene.sleeping[i]);
        }

        b2Counters counters = b2World_GetCounters(scene.worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(scene.worldId), scene.count, scene.sleepCount, scene.lastMoveCount,
            scene.totalMoveCount, scene.totalFellAsleep, states);
        b2DestroyWorld(scene.worldId);
        return result;
    }

    private static BodyState bodyState(b2BodyId bodyId, boolean sleeping) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x, velocity.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetShapeCount(bodyId), b2Body_GetContactCapacity(bodyId),
            sleeping);
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    private static final class Scene {
        final b2BodyId[] bodyIds = new b2BodyId[MAX_COUNT];
        final boolean[] sleeping = new boolean[MAX_COUNT];
        final RandomState random = new RandomState(RAND_SEED);
        b2WorldId worldId;
        int count;
        int sleepCount;
        int lastMoveCount;
        int totalMoveCount;
        int totalFellAsleep;

        void processBodyEvents() {
            b2BodyEvents events = b2World_GetBodyEvents(worldId);
            lastMoveCount = events.moveCount;
            totalMoveCount += events.moveCount;
            for (b2BodyMoveEvent event : events.moveEvents) {
                int index = (Integer) event.userData;
                if (event.fellAsleep) {
                    if (!sleeping[index]) {
                        sleeping[index] = true;
                        sleepCount += 1;
                    }
                    totalFellAsleep += 1;
                } else if (sleeping[index]) {
                    sleeping[index] = false;
                    sleepCount -= 1;
                }
            }
        }

        void createGround() {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.material.friction = 0.1f;

            b2Polygon box = b2MakeOffsetBox(12.0f, 0.1f, new b2Vec2(-10.0f, -0.1f),
                b2MakeRot(-0.15f * B2_PI));
            b2CreatePolygonShape(groundId, shapeDef, box);

            box = b2MakeOffsetBox(12.0f, 0.1f, new b2Vec2(10.0f, -0.1f), b2MakeRot(0.15f * B2_PI));
            b2CreatePolygonShape(groundId, shapeDef, box);

            shapeDef.material.restitution = 0.8f;

            box = b2MakeOffsetBox(0.1f, 10.0f, new b2Vec2(19.9f, 10.0f), b2Rot_identity);
            b2CreatePolygonShape(groundId, shapeDef, box);

            box = b2MakeOffsetBox(0.1f, 10.0f, new b2Vec2(-19.9f, 10.0f), b2Rot_identity);
            b2CreatePolygonShape(groundId, shapeDef, box);

            box = b2MakeOffsetBox(20.0f, 0.1f, new b2Vec2(0.0f, 20.1f), b2Rot_identity);
            b2CreatePolygonShape(groundId, shapeDef, box);
        }

        void createBodies() {
            b2Capsule capsule = new b2Capsule(new b2Vec2(-0.25f, 0.0f), new b2Vec2(0.25f, 0.0f), 0.25f);
            b2Circle circle = new b2Circle(new b2Vec2(0.0f, 0.0f), 0.35f);
            b2Polygon square = b2MakeSquare(0.35f);

            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            b2ShapeDef shapeDef = b2DefaultShapeDef();

            float x = -5.0f;
            float y = 10.0f;
            for (int i = 0; i < 10 && count < MAX_COUNT; ++i) {
                bodyDef.position = new b2Vec2(x, y);
                bodyDef.isBullet = count % 12 == 0;
                bodyDef.userData = count;
                bodyIds[count] = b2CreateBody(worldId, bodyDef);
                sleeping[count] = false;

                int remainder = count % 4;
                if (remainder == 0) {
                    b2CreateCapsuleShape(bodyIds[count], shapeDef, capsule);
                } else if (remainder == 1) {
                    b2CreateCircleShape(bodyIds[count], shapeDef, circle);
                } else if (remainder == 2) {
                    b2CreatePolygonShape(bodyIds[count], shapeDef, square);
                } else {
                    b2Polygon poly = random.randomPolygon(0.75f);
                    poly.radius = 0.1f;
                    b2CreatePolygonShape(bodyIds[count], shapeDef, poly);
                }

                count += 1;
                x += 1.0f;
            }
        }
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final int dynamicCount;
        public final int sleepCount;
        public final int lastMoveCount;
        public final int totalMoveCount;
        public final int totalFellAsleep;
        public final BodyState[] bodies;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount,
               int dynamicCount, int sleepCount, int lastMoveCount, int totalMoveCount, int totalFellAsleep,
               BodyState[] bodies) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.dynamicCount = dynamicCount;
            this.sleepCount = sleepCount;
            this.lastMoveCount = lastMoveCount;
            this.totalMoveCount = totalMoveCount;
            this.totalFellAsleep = totalFellAsleep;
            this.bodies = bodies;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("bodyMove ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(jointCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(dynamicCount).append(' ')
                .append(sleepCount).append(' ')
                .append(lastMoveCount).append(' ')
                .append(totalMoveCount).append(' ')
                .append(totalFellAsleep).append(' ')
                .append(bodies.length);
            for (BodyState body : bodies) {
                builder.append(body.toLinePart());
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
        public final boolean sleeping;

        BodyState(float x, float y, float cos, float sin, float velocityX, float velocityY, float angularVelocity,
                  int shapeCount, int contactCapacity, boolean sleeping) {
            this.x = x;
            this.y = y;
            this.cos = cos;
            this.sin = sin;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.shapeCount = shapeCount;
            this.contactCapacity = contactCapacity;
            this.sleeping = sleeping;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s %d %d %d", formatFloat(x), formatFloat(y),
                formatFloat(cos), formatFloat(sin), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), shapeCount, contactCapacity, sleeping ? 1 : 0);
        }
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

        private float randomFloatRange(float lo, float hi) {
            float r = randomInt() & RAND_LIMIT;
            r /= RAND_LIMIT;
            return (hi - lo) * r + lo;
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
            return (x & 0x7FFFFFFF) % (RAND_LIMIT + 1);
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
