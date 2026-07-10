package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class RollingResistance {
    private static final int COUNT = 20;
    private static final int[] SAMPLE_INDICES = {0, 5, 10, 15, 19};

    private RollingResistance() {
    }

    public static Result run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyId[] bodies = new b2BodyId[COUNT];
        b2ShapeId[] groundShapes = new b2ShapeId[COUNT];
        float lift = 0.0f;
        float resistScale = 0.02f;
        b2Circle circle = new b2Circle(b2Vec2_zero, 0.5f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();

        for (int i = 0; i < COUNT; ++i) {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            b2Segment segment = new b2Segment(new b2Vec2(-40.0f, 2.0f * i), new b2Vec2(40.0f, 2.0f * i + lift));
            groundShapes[i] = b2CreateSegmentShape(groundId, shapeDef, segment);

            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(-39.5f, 2.0f * i + 0.75f);
            bodyDef.angularVelocity = -10.0f;
            bodyDef.linearVelocity = new b2Vec2(5.0f, 0.0f);

            bodies[i] = b2CreateBody(worldId, bodyDef);
            shapeDef.material.rollingResistance = resistScale * i;
            b2CreateCircleShape(bodies[i], shapeDef, circle);
        }
        SampleRuntime.action("rolling.level", "Level", "1", () -> resetSlope(groundShapes, bodies, 0.0f));
        SampleRuntime.action("rolling.uphill", "Uphill", "2", () -> resetSlope(groundShapes, bodies, 5.0f));
        SampleRuntime.action("rolling.downhill", "Downhill", "3", () -> resetSlope(groundShapes, bodies, -5.0f));

        for (int step = 0; step < 240; ++step) {
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

    private static void resetSlope(b2ShapeId[] groundShapes, b2BodyId[] bodies, float lift) {
        for (int i = 0; i < COUNT; ++i) {
            b2Shape_SetSegment(groundShapes[i],
                new b2Segment(new b2Vec2(-40.0f, 2.0f * i), new b2Vec2(40.0f, 2.0f * i + lift)));
            b2Body_SetTransform(bodies[i], new b2Vec2(-39.5f, 2.0f * i + 0.75f), b2Rot_identity);
            b2Body_SetLinearVelocity(bodies[i], new b2Vec2(5.0f, 0.0f));
            b2Body_SetAngularVelocity(bodies[i], -10.0f);
        }
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
            builder.append("rollingResistance ").append(contactCount).append(' ').append(awakeBodyCount);
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
