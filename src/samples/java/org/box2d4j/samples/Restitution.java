package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Restitution {
    private static final int DEFAULT_STEP_COUNT = 240;
    private static final int COUNT = 40;
    private static final int[] SAMPLE_INDICES = {0, 10, 20, 30, 39};

    private Restitution() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            float h = 1.0f * COUNT;
            b2Segment segment = new b2Segment(new b2Vec2(-h, 0.0f), new b2Vec2(h, 0.0f));
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateSegmentShape(groundId, shapeDef, segment);
        }

        b2BodyId[] bodies = new b2BodyId[COUNT];
        int[] shapeType = {0};
        createBodies(worldId, bodies, shapeType[0]);
        SampleRuntime.choice("restitution.shape", "Shape", shapeType[0], new String[] {"Circle", "Box"}, value -> {
            shapeType[0] = value;
            createBodies(worldId, bodies, shapeType[0]);
        });
        SampleRuntime.action("restitution.reset", "Reset", () -> createBodies(worldId, bodies, shapeType[0]));

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] states = new BodyState[SAMPLE_INDICES.length];
        for (int i = 0; i < SAMPLE_INDICES.length; ++i) {
            states[i] = bodyState(bodies[SAMPLE_INDICES[i]]);
        }
        b2Counters counters = b2World_GetCounters(worldId);
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

        b2DestroyWorld(worldId);
        return new Result(counters.contactCount, awakeBodyCount, states);
    }

    private static void createBodies(b2WorldId worldId, b2BodyId[] bodies, int shapeType) {
        for (b2BodyId body : bodies) {
            if (body != null && b2Body_IsValid(body)) {
                b2DestroyBody(body);
            }
        }
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        float dr = 1.0f / (COUNT - 1);
        float x = -1.0f * (COUNT - 1);
        for (int i = 0; i < COUNT; ++i) {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(x, 40.0f);
            bodies[i] = b2CreateBody(worldId, bodyDef);
            if (shapeType == 0) {
                b2CreateCircleShape(bodies[i], shapeDef, new b2Circle(new b2Vec2(), 0.5f));
            } else {
                b2CreatePolygonShape(bodies[i], shapeDef, b2MakeBox(0.5f, 0.5f));
            }
            shapeDef.material.restitution += dr;
            x += 2.0f;
        }
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
            builder.append("restitution ").append(contactCount).append(' ').append(awakeBodyCount);
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
