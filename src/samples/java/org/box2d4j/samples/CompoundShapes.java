package org.box2d4j.samples;

import org.box2d4j.b2AABB;
import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Hull;
import org.box2d4j.b2MassData;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class CompoundShapes {
    private static final int STEP_COUNT = 10;

    private CompoundShapes() {
    }

    public static Result run() {
        return run(STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, shapeDef, new b2Segment(new b2Vec2(50.0f, 0.0f), new b2Vec2(-50.0f, 0.0f)));

        b2BodyId table1Id = createTable(worldId, -15.0f, 1.0f, 1.5f);
        b2BodyId table2Id = createTable(worldId, -5.0f, 1.0f, 2.0f);
        b2BodyId ship1Id = createShip1(worldId);
        b2BodyId ship2Id = createShip2(worldId);

        b2BodyId[] bodies = new b2BodyId[8];
        bodies[0] = table1Id;
        bodies[1] = table2Id;
        bodies[2] = ship1Id;
        bodies[3] = ship2Id;
        boolean interactive = SampleRuntime.isActive();
        if (!interactive) {
            spawn(worldId, bodies);
        }
        SampleRuntime.action("compoundShapes.intrude", "Intrude", () -> spawn(worldId,
            new b2BodyId[] {table1Id, table2Id, ship1Id, ship2Id, null, null, null, null}));
        SampleRuntime.toggle("compoundShapes.bodyAabbs", "Body AABBs", false, SampleRuntime::drawBounds);

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        int stateCount = interactive ? 4 : bodies.length;
        BodyState[] states = new BodyState[stateCount];
        for (int i = 0; i < stateCount; ++i) {
            states[i] = bodyState(bodies[i]);
        }
        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            b2World_GetAwakeBodyCount(worldId), states);
        b2DestroyWorld(worldId);
        return result;
    }

    private static b2BodyId createTable(b2WorldId worldId, float x, float y, float legHeight) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(x, y);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeOffsetBox(3.0f, 0.5f, new b2Vec2(0.0f, 3.5f), b2Rot_identity));
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeOffsetBox(0.5f, legHeight, new b2Vec2(-2.5f, legHeight), b2Rot_identity));
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeOffsetBox(0.5f, legHeight, new b2Vec2(2.5f, legHeight), b2Rot_identity));
        return bodyId;
    }

    private static b2BodyId createShip1(b2WorldId worldId) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(5.0f, 1.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(bodyId, shapeDef, makeTriangle(new b2Vec2(-2.0f, 0.0f), new b2Vec2(0.0f, 4.0f / 3.0f), new b2Vec2(0.0f, 4.0f)));
        b2CreatePolygonShape(bodyId, shapeDef, makeTriangle(new b2Vec2(2.0f, 0.0f), new b2Vec2(0.0f, 4.0f / 3.0f), new b2Vec2(0.0f, 4.0f)));
        return bodyId;
    }

    private static b2BodyId createShip2(b2WorldId worldId) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(15.0f, 1.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(bodyId, shapeDef, makeTriangle(new b2Vec2(-2.0f, 0.0f), new b2Vec2(1.0f, 2.0f), new b2Vec2(0.0f, 4.0f)));
        b2CreatePolygonShape(bodyId, shapeDef, makeTriangle(new b2Vec2(2.0f, 0.0f), new b2Vec2(-1.0f, 2.0f), new b2Vec2(0.0f, 4.0f)));
        return bodyId;
    }

    private static b2Polygon makeTriangle(b2Vec2 a, b2Vec2 b, b2Vec2 c) {
        b2Vec2[] vertices = {a, b, c};
        b2Hull hull = b2ComputeHull(vertices, 3);
        return b2MakePolygon(hull, 0.0f);
    }

    private static void spawn(b2WorldId worldId, b2BodyId[] bodies) {
        bodies[4] = createTableObstruction(worldId, bodies[0]);
        bodies[5] = createTableObstruction(worldId, bodies[1]);
        bodies[6] = createShipObstruction(worldId, bodies[2]);
        bodies[7] = createShipObstruction(worldId, bodies[3]);
    }

    private static b2BodyId createTableObstruction(b2WorldId worldId, b2BodyId sourceId) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = b2Body_GetPosition(sourceId);
        bodyDef.rotation = b2Body_GetRotation(sourceId);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeOffsetBox(4.0f, 0.1f, new b2Vec2(0.0f, 3.0f), b2Rot_identity));
        return bodyId;
    }

    private static b2BodyId createShipObstruction(b2WorldId worldId, b2BodyId sourceId) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = b2Body_GetPosition(sourceId);
        bodyDef.rotation = b2Body_GetRotation(sourceId);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(0.0f, 2.0f), 0.5f));
        return bodyId;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            v.x, v.y, b2Body_GetAngularVelocity(bodyId), b2Body_GetShapeCount(bodyId),
            b2Body_GetMassData(bodyId), b2Body_ComputeAABB(bodyId));
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
        public final BodyState[] states;

        public Result(int bodyCount, int shapeCount, int contactCount, int awakeBodyCount, BodyState[] states) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.states = states;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("compoundShapes ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(states.length);
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
        public final int shapeCount;
        public final b2MassData massData;
        public final b2AABB bodyAabb;

        BodyState(float x, float y, float angle, float velocityX, float velocityY, float angularVelocity,
                  int shapeCount, b2MassData massData, b2AABB bodyAabb) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.shapeCount = shapeCount;
            this.massData = massData;
            this.bodyAabb = bodyAabb;
        }

        String toLinePart() {
            StringBuilder builder = new StringBuilder();
            builder.append(' ')
                .append(formatFloat(x)).append(' ')
                .append(formatFloat(y)).append(' ')
                .append(formatFloat(angle)).append(' ')
                .append(formatFloat(velocityX)).append(' ')
                .append(formatFloat(velocityY)).append(' ')
                .append(formatFloat(angularVelocity)).append(' ')
                .append(shapeCount).append(' ')
                .append(formatFloat(massData.mass)).append(' ')
                .append(formatFloat(massData.center.x)).append(' ')
                .append(formatFloat(massData.center.y)).append(' ')
                .append(formatFloat(massData.rotationalInertia));
            appendAabb(builder, bodyAabb);
            return builder.toString();
        }
    }

    private static void appendAabb(StringBuilder builder, b2AABB aabb) {
        builder.append(' ')
            .append(formatFloat(aabb.lowerBound.x)).append(' ')
            .append(formatFloat(aabb.lowerBound.y)).append(' ')
            .append(formatFloat(aabb.upperBound.x)).append(' ')
            .append(formatFloat(aabb.upperBound.y));
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
