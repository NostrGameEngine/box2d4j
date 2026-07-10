package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Cliff {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final boolean DEFAULT_FLIP = true;
    private static final int BODY_COUNT = 9;

    private Cliff() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT, DEFAULT_FLIP);
    }

    public static Result run(int stepCount) {
        return run(stepCount, DEFAULT_FLIP);
    }

    public static Result run(int stepCount, boolean flip) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        createGround(worldId);
        boolean interactive = SampleRuntime.isActive();
        boolean[] currentFlip = {interactive ? false : flip};
        b2BodyId[][] bodies = {createBodies(worldId, currentFlip[0])};
        SampleRuntime.action("cliff.flip", "Flip", () -> {
            for (b2BodyId body : bodies[0]) {
                if (b2Body_IsValid(body)) {
                    b2DestroyBody(body);
                }
            }
            currentFlip[0] = !currentFlip[0];
            bodies[0] = createBodies(worldId, currentFlip[0]);
        });

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] states = new BodyState[BODY_COUNT];
        for (int i = 0; i < BODY_COUNT; ++i) {
            states[i] = bodyState(bodies[0][i]);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            counters.jointCount, b2World_GetAwakeBodyCount(worldId), currentFlip[0], states);
        b2DestroyWorld(worldId);
        return result;
    }

    private static void createGround(b2WorldId worldId) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(0.0f, 0.0f);
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon box = b2MakeOffsetBox(100.0f, 1.0f, new b2Vec2(0.0f, -1.0f), b2Rot_identity);
        b2CreatePolygonShape(groundId, shapeDef, box);

        b2Segment segment = new b2Segment(new b2Vec2(-14.0f, 4.0f), new b2Vec2(-8.0f, 4.0f));
        b2CreateSegmentShape(groundId, shapeDef, segment);

        box = b2MakeOffsetBox(3.0f, 0.5f, new b2Vec2(0.0f, 4.0f), b2Rot_identity);
        b2CreatePolygonShape(groundId, shapeDef, box);

        b2Capsule capsule = new b2Capsule(new b2Vec2(8.5f, 4.0f), new b2Vec2(13.5f, 4.0f), 0.5f);
        b2CreateCapsuleShape(groundId, shapeDef, capsule);
    }

    private static b2BodyId[] createBodies(b2WorldId worldId, boolean flip) {
        b2BodyId[] bodyIds = new b2BodyId[BODY_COUNT];
        float sign = flip ? -1.0f : 1.0f;

        b2Capsule capsule = new b2Capsule(new b2Vec2(-0.25f, 0.0f), new b2Vec2(0.25f, 0.0f), 0.25f);
        b2Circle circle = new b2Circle(new b2Vec2(0.0f, 0.0f), 0.5f);
        b2Polygon square = b2MakeSquare(0.5f);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.01f;
        bodyDef.linearVelocity = new b2Vec2(2.0f * sign, 0.0f);
        float capsuleOffset = flip ? -4.0f : 0.0f;

        bodyDef.position = new b2Vec2(-9.0f + capsuleOffset, 4.25f);
        bodyIds[0] = b2CreateBody(worldId, bodyDef);
        b2CreateCapsuleShape(bodyIds[0], shapeDef, capsule);

        bodyDef.position = new b2Vec2(2.0f + capsuleOffset, 4.75f);
        bodyIds[1] = b2CreateBody(worldId, bodyDef);
        b2CreateCapsuleShape(bodyIds[1], shapeDef, capsule);

        bodyDef.position = new b2Vec2(13.0f + capsuleOffset, 4.75f);
        bodyIds[2] = b2CreateBody(worldId, bodyDef);
        b2CreateCapsuleShape(bodyIds[2], shapeDef, capsule);

        shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.01f;
        bodyDef.linearVelocity = new b2Vec2(2.5f * sign, 0.0f);

        bodyDef.position = new b2Vec2(-11.0f, 4.5f);
        bodyIds[3] = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyIds[3], shapeDef, square);

        bodyDef.position = new b2Vec2(0.0f, 5.0f);
        bodyIds[4] = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyIds[4], shapeDef, square);

        bodyDef.position = new b2Vec2(11.0f, 5.0f);
        bodyIds[5] = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyIds[5], shapeDef, square);

        shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.2f;
        bodyDef.linearVelocity = new b2Vec2(1.5f * sign, 0.0f);
        float circleOffset = flip ? 4.0f : 0.0f;

        bodyDef.position = new b2Vec2(-13.0f + circleOffset, 4.5f);
        bodyIds[6] = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(bodyIds[6], shapeDef, circle);

        bodyDef.position = new b2Vec2(-2.0f + circleOffset, 5.0f);
        bodyIds[7] = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(bodyIds[7], shapeDef, circle);

        bodyDef.position = new b2Vec2(9.0f + circleOffset, 5.0f);
        bodyIds[8] = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(bodyIds[8], shapeDef, circle);

        return bodyIds;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x, velocity.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetShapeCount(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        boolean flip = args.length < 2 || Boolean.parseBoolean(args[1]);
        System.out.println(run(stepCount, flip).toLine());
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final boolean flip;
        public final BodyState[] bodies;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, boolean flip,
               BodyState[] bodies) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.flip = flip;
            this.bodies = bodies;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("cliff ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(jointCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(flip ? 1 : 0).append(' ')
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
