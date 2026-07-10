package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Confined {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final int GRID_COUNT = 25;
    private static final int BODY_COUNT = GRID_COUNT * GRID_COUNT;
    private static final int[] SAMPLE_INDICES = {0, 12, 24, 300, 312, 324, 600, 612, 624};

    private Confined() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId[] bodies = new b2BodyId[BODY_COUNT];

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateCapsuleShape(groundId, shapeDef,
            new b2Capsule(new b2Vec2(-10.5f, 0.0f), new b2Vec2(10.5f, 0.0f), 0.5f));
        b2CreateCapsuleShape(groundId, shapeDef,
            new b2Capsule(new b2Vec2(-10.5f, 0.0f), new b2Vec2(-10.5f, 20.5f), 0.5f));
        b2CreateCapsuleShape(groundId, shapeDef,
            new b2Capsule(new b2Vec2(10.5f, 0.0f), new b2Vec2(10.5f, 20.5f), 0.5f));
        b2CreateCapsuleShape(groundId, shapeDef,
            new b2Capsule(new b2Vec2(-10.5f, 20.5f), new b2Vec2(10.5f, 20.5f), 0.5f));

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.gravityScale = 0.0f;

        shapeDef = b2DefaultShapeDef();
        b2Circle circle = new b2Circle(new b2Vec2(0.0f, 0.0f), 0.5f);

        int row = 0;
        int column = 0;
        int count = 0;
        while (count < BODY_COUNT) {
            row = 0;
            for (int i = 0; i < GRID_COUNT; ++i) {
                float x = -8.75f + column * 18.0f / GRID_COUNT;
                float y = 1.5f + row * 18.0f / GRID_COUNT;

                bodyDef.position = new b2Vec2(x, y);
                b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
                b2CreateCircleShape(bodyId, shapeDef, circle);

                bodies[count] = bodyId;
                count += 1;
                row += 1;
            }
            column += 1;
        }

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] samples = new BodyState[SAMPLE_INDICES.length];
        for (int i = 0; i < SAMPLE_INDICES.length; ++i) {
            samples[i] = bodyState(bodies[SAMPLE_INDICES[i]]);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            counters.jointCount, b2World_GetAwakeBodyCount(worldId), BODY_COUNT, samples);
        b2DestroyWorld(worldId);
        return result;
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

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final int dynamicBodyCount;
        public final BodyState[] samples;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount,
               int dynamicBodyCount, BodyState[] samples) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.dynamicBodyCount = dynamicBodyCount;
            this.samples = samples;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("confined ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(jointCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(dynamicBodyCount).append(' ')
                .append(samples.length);
            for (BodyState sample : samples) {
                builder.append(sample.toLinePart());
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
