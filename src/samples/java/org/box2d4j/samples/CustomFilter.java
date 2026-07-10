package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class CustomFilter {
    private static final int COUNT = 10;

    private CustomFilter() {
    }

    public static Result run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        int[] filterCalls = {0};
        b2World_SetCustomFilterCallback(worldId, (shapeIdA, shapeIdB, context) -> {
            filterCalls[0] += 1;
            return shouldCollide(shapeIdA, shapeIdB);
        }, null);

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);
            b2Segment segment = new b2Segment(new b2Vec2(-40.0f, 0.0f), new b2Vec2(40.0f, 0.0f));

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateSegmentShape(groundId, shapeDef, segment);
        }

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon box = b2MakeSquare(1.0f);
        b2BodyId[] bodies = new b2BodyId[COUNT];
        b2ShapeId[] shapes = new b2ShapeId[COUNT];
        float x = -COUNT;

        for (int i = 0; i < COUNT; ++i) {
            bodyDef.position = new b2Vec2(x, 5.0f);
            bodies[i] = b2CreateBody(worldId, bodyDef);

            shapeDef.userData = i + 1;
            shapes[i] = b2CreatePolygonShape(bodies[i], shapeDef, box);
            x += 2.0f;
        }

        for (int step = 0; step < 240; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        ShapeState[] shapeStates = new ShapeState[shapes.length];
        BodyState[] bodyStates = new BodyState[bodies.length];
        for (int i = 0; i < COUNT; ++i) {
            shapeStates[i] = shapeState(shapes[i]);
            bodyStates[i] = bodyState(bodies[i]);
        }
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

        b2DestroyWorld(worldId);
        return new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, awakeBodyCount, filterCalls[0],
            shapeStates, bodyStates);
    }

    private static boolean shouldCollide(b2ShapeId shapeIdA, b2ShapeId shapeIdB) {
        Object userDataA = b2Shape_GetUserData(shapeIdA);
        Object userDataB = b2Shape_GetUserData(shapeIdB);

        if (!(userDataA instanceof Number) || !(userDataB instanceof Number)) {
            return true;
        }

        int indexA = ((Number) userDataA).intValue();
        int indexB = ((Number) userDataB).intValue();
        return ((indexA & 1) + (indexB & 1)) != 1;
    }

    private static ShapeState shapeState(b2ShapeId shapeId) {
        Object userData = b2Shape_GetUserData(shapeId);
        return new ShapeState(userData instanceof Number ? ((Number) userData).intValue() : 0);
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(b2Body_GetContactCapacity(bodyId), p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            v.x, v.y);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int awakeBodyCount;
        public final int filterCalls;
        public final ShapeState[] shapeStates;
        public final BodyState[] bodyStates;

        public Result(int bodyCount, int shapeCount, int contactCount, int awakeBodyCount, int filterCalls,
            ShapeState[] shapeStates, BodyState[] bodyStates) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.filterCalls = filterCalls;
            this.shapeStates = shapeStates;
            this.bodyStates = bodyStates;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("customFilter ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(filterCalls).append(' ')
                .append(shapeStates.length).append(' ')
                .append(bodyStates.length);
            for (ShapeState state : shapeStates) {
                builder.append(state.toLinePart());
            }
            for (BodyState state : bodyStates) {
                builder.append(state.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class ShapeState {
        public final int userData;

        ShapeState(int userData) {
            this.userData = userData;
        }

        String toLinePart() {
            return " " + userData;
        }
    }

    public static final class BodyState {
        public final int contactCapacity;
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;

        BodyState(int contactCapacity, float x, float y, float angle, float velocityX, float velocityY) {
            this.contactCapacity = contactCapacity;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %d %s %s %s %s %s", contactCapacity, formatFloat(x), formatFloat(y),
                formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY));
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
