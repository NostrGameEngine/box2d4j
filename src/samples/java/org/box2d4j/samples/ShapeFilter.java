package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Filter;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ShapeFilter {
    private static final int DEFAULT_STEP_COUNT = 120;

    private static final long GROUND = 0x00000001L;
    private static final long TEAM1 = 0x00000002L;
    private static final long TEAM2 = 0x00000004L;
    private static final long TEAM3 = 0x00000008L;
    private static final long ALL_BITS = 0xffffffffL;

    private ShapeFilter() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int phaseStepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);
            b2Segment segment = new b2Segment(new b2Vec2(-20.0f, 0.0f), new b2Vec2(20.0f, 0.0f));

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.filter.categoryBits = GROUND;
            shapeDef.filter.maskBits = ALL_BITS;

            b2CreateSegmentShape(groundId, shapeDef, segment);
        }

        b2BodyId[] bodies = new b2BodyId[3];
        b2ShapeId[] shapes = new b2ShapeId[3];
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;

            bodyDef.position = new b2Vec2(0.0f, 2.0f);
            bodies[0] = b2CreateBody(worldId, bodyDef);

            bodyDef.position = new b2Vec2(0.0f, 5.0f);
            bodies[1] = b2CreateBody(worldId, bodyDef);

            bodyDef.position = new b2Vec2(0.0f, 8.0f);
            bodies[2] = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeBox(2.0f, 1.0f);

            b2ShapeDef shapeDef = b2DefaultShapeDef();

            shapeDef.filter.categoryBits = TEAM1;
            shapeDef.filter.maskBits = GROUND | TEAM2 | TEAM3;
            shapes[0] = b2CreatePolygonShape(bodies[0], shapeDef, box);

            shapeDef.filter.categoryBits = TEAM2;
            shapeDef.filter.maskBits = GROUND | TEAM1 | TEAM3;
            shapes[1] = b2CreatePolygonShape(bodies[1], shapeDef, box);

            shapeDef.filter.categoryBits = TEAM3;
            shapeDef.filter.maskBits = GROUND | TEAM1 | TEAM2;
            shapes[2] = b2CreatePolygonShape(bodies[2], shapeDef, box);
        }
        boolean interactive = SampleRuntime.isActive();
        bindFilter("filter.team2For1", "Team 2 for Player 1", shapes[0], TEAM2);
        bindFilter("filter.team3For1", "Team 3 for Player 1", shapes[0], TEAM3);
        bindFilter("filter.team1For2", "Team 1 for Player 2", shapes[1], TEAM1);
        bindFilter("filter.team3For2", "Team 3 for Player 2", shapes[1], TEAM3);
        bindFilter("filter.team1For3", "Team 1 for Player 3", shapes[2], TEAM1);
        bindFilter("filter.team2For3", "Team 2 for Player 3", shapes[2], TEAM2);

        for (int step = 0; step < phaseStepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        if (!interactive) {
            setMaskBit(shapes[0], TEAM2, false);
            setMaskBit(shapes[1], TEAM1, false);
            setMaskBit(shapes[1], TEAM3, false);
            setMaskBit(shapes[2], TEAM2, false);

            for (int step = 0; step < phaseStepCount; ++step) {
                b2World_Step(worldId, 1.0f / 60.0f, 4);
            }
        }

        b2Counters counters = b2World_GetCounters(worldId);
        ShapeState[] shapeStates = new ShapeState[shapes.length];
        BodyState[] bodyStates = new BodyState[bodies.length];
        for (int i = 0; i < shapes.length; ++i) {
            shapeStates[i] = shapeState(shapes[i]);
            bodyStates[i] = bodyState(bodies[i]);
        }
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

        b2DestroyWorld(worldId);
        return new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, awakeBodyCount, shapeStates,
            bodyStates);
    }

    private static void bindFilter(String id, String label, b2ShapeId shapeId, long bit) {
        SampleRuntime.toggle(id, label, true, value -> setMaskBit(shapeId, bit, value));
    }

    private static void setMaskBit(b2ShapeId shapeId, long bit, boolean enabled) {
        b2Filter filter = b2Shape_GetFilter(shapeId);
        filter.maskBits = enabled ? filter.maskBits | bit : filter.maskBits & ~bit;
        b2Shape_SetFilter(shapeId, filter);
    }

    private static ShapeState shapeState(b2ShapeId shapeId) {
        b2Filter filter = b2Shape_GetFilter(shapeId);
        return new ShapeState(filter.categoryBits, filter.maskBits, filter.groupIndex);
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(b2Body_GetContactCapacity(bodyId), p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            v.x, v.y);
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int awakeBodyCount;
        public final ShapeState[] shapeStates;
        public final BodyState[] bodyStates;

        public Result(int bodyCount, int shapeCount, int contactCount, int awakeBodyCount, ShapeState[] shapeStates,
            BodyState[] bodyStates) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.shapeStates = shapeStates;
            this.bodyStates = bodyStates;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("shapeFilter ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(awakeBodyCount).append(' ')
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
        public final long categoryBits;
        public final long maskBits;
        public final int groupIndex;

        ShapeState(long categoryBits, long maskBits, int groupIndex) {
            this.categoryBits = categoryBits;
            this.maskBits = maskBits;
            this.groupIndex = groupIndex;
        }

        String toLinePart() {
            return " " + Long.toUnsignedString(categoryBits) + " " + Long.toUnsignedString(maskBits) + " " + groupIndex;
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
