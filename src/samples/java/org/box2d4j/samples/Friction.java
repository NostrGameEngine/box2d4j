package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Friction {
    private static final int DEFAULT_STEP_COUNT = 240;

    private Friction() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.material.friction = 0.2f;

            b2Segment segment = new b2Segment(new b2Vec2(-40.0f, 0.0f), new b2Vec2(40.0f, 0.0f));
            b2CreateSegmentShape(groundId, shapeDef, segment);

            b2Polygon box = b2MakeOffsetBox(13.0f, 0.25f, new b2Vec2(-4.0f, 22.0f), b2MakeRot(-0.25f));
            b2CreatePolygonShape(groundId, shapeDef, box);

            box = b2MakeOffsetBox(0.25f, 1.0f, new b2Vec2(10.5f, 19.0f), b2Rot_identity);
            b2CreatePolygonShape(groundId, shapeDef, box);

            box = b2MakeOffsetBox(13.0f, 0.25f, new b2Vec2(4.0f, 14.0f), b2MakeRot(0.25f));
            b2CreatePolygonShape(groundId, shapeDef, box);

            box = b2MakeOffsetBox(0.25f, 1.0f, new b2Vec2(-10.5f, 11.0f), b2Rot_identity);
            b2CreatePolygonShape(groundId, shapeDef, box);

            box = b2MakeOffsetBox(13.0f, 0.25f, new b2Vec2(-4.0f, 6.0f), b2MakeRot(-0.25f));
            b2CreatePolygonShape(groundId, shapeDef, box);
        }

        b2BodyId[] bodies = new b2BodyId[5];
        b2Polygon box = b2MakeBox(0.5f, 0.5f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 25.0f;
        float[] friction = {0.75f, 0.5f, 0.35f, 0.1f, 0.0f};

        for (int i = 0; i < 5; ++i) {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(-15.0f + 4.0f * i, 28.0f);
            bodies[i] = b2CreateBody(worldId, bodyDef);

            shapeDef.material.friction = friction[i];
            b2CreatePolygonShape(bodies[i], shapeDef, box);
        }

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        BodyState[] states = new BodyState[bodies.length];
        for (int i = 0; i < bodies.length; ++i) {
            states[i] = bodyState(bodies[i]);
        }
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

        b2DestroyWorld(worldId);
        return new Result(counters.contactCount, awakeBodyCount, states);
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)), v.x, v.y);
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final int contactCount;
        public final int awakeBodyCount;
        public final BodyState[] states;

        public Result(int contactCount, int awakeBodyCount, BodyState[] states) {
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.states = states;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("friction ").append(contactCount).append(' ').append(awakeBodyCount);
            for (BodyState state : states) {
                builder.append(state.toLinePart());
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

        BodyState(float x, float y, float angle, float velocityX, float velocityY) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY));
        }
    }

    private static String formatFloat(float value) {
        if (value == 0.0f) {
            return "0";
        }
        String text = String.format(Locale.ROOT, "%.9g", value);
        if (text.indexOf('e') < 0 && text.indexOf('E') < 0 && text.indexOf('.') >= 0) {
            while (text.endsWith("0")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text;
    }
}
