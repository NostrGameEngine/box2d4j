package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class RoundedShapes {
    private static final int RAND_LIMIT = 32767;
    private static final int RAND_SEED = 12345;
    private static final int STEP_COUNT = 240;
    private static final int X_COUNT = 10;
    private static final int Y_COUNT = 10;
    private static final int[] SAMPLE_INDICES = {0, 1, 9, 10, 44, 55, 90, 99};

    private RoundedShapes() {
    }

    public static Result run() {
        return run(STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon box = b2MakeOffsetBox(20.0f, 1.0f, new b2Vec2(0.0f, -1.0f), b2Rot_identity);
        b2CreatePolygonShape(groundId, shapeDef, box);

        box = b2MakeOffsetBox(1.0f, 5.0f, new b2Vec2(19.0f, 5.0f), b2Rot_identity);
        b2CreatePolygonShape(groundId, shapeDef, box);

        box = b2MakeOffsetBox(1.0f, 5.0f, new b2Vec2(-19.0f, 5.0f), b2Rot_identity);
        b2CreatePolygonShape(groundId, shapeDef, box);

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        shapeDef = b2DefaultShapeDef();
        shapeDef.material.rollingResistance = 0.3f;

        RandomState random = new RandomState(RAND_SEED);
        b2BodyId[] bodies = new b2BodyId[X_COUNT * Y_COUNT];
        ShapeState[] shapes = new ShapeState[bodies.length];
        int bodyIndex = 0;
        float y = 2.0f;
        for (int i = 0; i < Y_COUNT; ++i) {
            float x = -5.0f;
            for (int j = 0; j < X_COUNT; ++j) {
                bodyDef.position = new b2Vec2(x, y);
                b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

                b2Polygon poly = random.randomPolygon(0.5f);
                poly.radius = random.randomFloatRange(0.05f, 0.25f);
                b2CreatePolygonShape(bodyId, shapeDef, poly);

                bodies[bodyIndex] = bodyId;
                shapes[bodyIndex] = ShapeState.from(poly);
                bodyIndex += 1;
                x += 1.0f;
            }
            y += 1.0f;
        }

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] bodyStates = new BodyState[SAMPLE_INDICES.length];
        ShapeState[] shapeStates = new ShapeState[SAMPLE_INDICES.length];
        for (int i = 0; i < SAMPLE_INDICES.length; ++i) {
            int index = SAMPLE_INDICES[i];
            bodyStates[i] = bodyState(bodies[index]);
            shapeStates[i] = shapes[index];
        }
        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            b2World_GetAwakeBodyCount(worldId), bodyStates, shapeStates);
        b2DestroyWorld(worldId);
        return result;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            v.x, v.y, b2Body_GetAngularVelocity(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length > 0 ? Integer.parseInt(args[0]) : STEP_COUNT;
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int awakeBodyCount;
        public final BodyState[] bodies;
        public final ShapeState[] shapes;

        public Result(int bodyCount, int shapeCount, int contactCount, int awakeBodyCount,
                      BodyState[] bodies, ShapeState[] shapes) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.bodies = bodies;
            this.shapes = shapes;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("roundedShapes ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(bodies.length);
            for (int i = 0; i < bodies.length; ++i) {
                builder.append(bodies[i].toLinePart());
                builder.append(shapes[i].toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class BodyState {
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;

        BodyState(float x, float y, float angle, float velocityX, float velocityY, float angularVelocity) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity));
        }
    }

    public static final class ShapeState {
        public final int count;
        public final float radius;
        public final b2Vec2[] vertices;

        private ShapeState(int count, float radius, b2Vec2[] vertices) {
            this.count = count;
            this.radius = radius;
            this.vertices = vertices;
        }

        static ShapeState from(b2Polygon polygon) {
            b2Vec2[] vertices = b2Vec2.array(polygon.count);
            for (int i = 0; i < polygon.count; ++i) {
                vertices[i].set(polygon.vertices[i]);
            }
            return new ShapeState(polygon.count, polygon.radius, vertices);
        }

        String toLinePart() {
            StringBuilder builder = new StringBuilder();
            builder.append(' ').append(count).append(' ').append(formatFloat(radius));
            for (int i = 0; i < count; ++i) {
                builder.append(' ').append(formatFloat(vertices[i].x)).append(' ').append(formatFloat(vertices[i].y));
            }
            return builder.toString();
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
