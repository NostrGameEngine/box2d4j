package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class TiltedStack {
    private static final int ROWS = 10;
    private static final int COLUMNS = 10;
    private static final int[] SAMPLE_INDICES = {0, 9, 44, 90, 99};

    private TiltedStack() {
    }

    public static Result run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(0.0f, -1.0f);
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeBox(1000.0f, 1.0f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreatePolygonShape(groundId, shapeDef, box);
        }

        b2BodyId[] bodies = new b2BodyId[ROWS * COLUMNS];
        b2Polygon box = b2MakeRoundedBox(0.45f, 0.45f, 0.05f);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.3f;

        float offset = 0.2f;
        float dx = 5.0f;
        float xroot = -0.5f * dx * (COLUMNS - 1.0f);

        for (int j = 0; j < COLUMNS; ++j) {
            float x = xroot + j * dx;

            for (int i = 0; i < ROWS; ++i) {
                b2BodyDef bodyDef = b2DefaultBodyDef();
                bodyDef.type = b2_dynamicBody;

                int n = j * ROWS + i;
                bodyDef.position = new b2Vec2(x + offset * i, 0.5f + 1.0f * i);
                bodies[n] = b2CreateBody(worldId, bodyDef);

                b2CreatePolygonShape(bodies[n], shapeDef, box);
            }
        }

        for (int step = 0; step < 180; ++step) {
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

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            v.x, v.y, b2Body_GetAngularVelocity(bodyId));
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
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
            builder.append("tiltedStack ").append(contactCount).append(' ').append(awakeBodyCount);
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
