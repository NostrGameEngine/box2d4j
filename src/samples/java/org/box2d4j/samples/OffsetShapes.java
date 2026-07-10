package org.box2d4j.samples;

import org.box2d4j.b2AABB;
import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Counters;
import org.box2d4j.b2MassData;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class OffsetShapes {
    private OffsetShapes() {
    }

    public static Result run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId[] bodyIds = new b2BodyId[3];
        int bodyCount = 0;

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(-1.0f, 1.0f);
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);
            bodyIds[bodyCount++] = groundId;

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2Polygon box = b2MakeOffsetBox(1.0f, 1.0f, new b2Vec2(10.0f, -2.0f), b2MakeRot(0.5f * B2_PI));
            b2CreatePolygonShape(groundId, shapeDef, box);
        }

        {
            b2Capsule capsule = new b2Capsule(new b2Vec2(-5.0f, 1.0f), new b2Vec2(-4.0f, 1.0f), 0.25f);
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(13.5f, -0.75f);
            bodyDef.type = b2_dynamicBody;
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            bodyIds[bodyCount++] = bodyId;
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateCapsuleShape(bodyId, shapeDef, capsule);
        }

        {
            b2Polygon box = b2MakeOffsetBox(0.75f, 0.5f, new b2Vec2(9.0f, 2.0f), b2MakeRot(0.5f * B2_PI));
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(0.0f, 0.0f);
            bodyDef.type = b2_dynamicBody;
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            bodyIds[bodyCount++] = bodyId;
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreatePolygonShape(bodyId, shapeDef, box);
        }

        BodyState[] states = new BodyState[bodyCount];
        for (int i = 0; i < bodyCount; ++i) {
            states[i] = bodyState(bodyIds[i]);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, states);
        b2DestroyWorld(worldId);
        return result;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2MassData massData = b2Body_GetMassData(bodyId);
        b2ShapeId[] shapes = new b2ShapeId[1];
        int shapeCount = b2Body_GetShapes(bodyId, shapes, 1);
        b2MassData shapeMass = b2Shape_GetMassData(shapes[0]);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)), shapeCount,
            massData, b2Body_ComputeAABB(bodyId), b2Shape_GetType(shapes[0]), shapeMass, b2Shape_GetAABB(shapes[0]));
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final BodyState[] states;

        public Result(int bodyCount, int shapeCount, int contactCount, BodyState[] states) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.states = states;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("offsetShapes ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
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
        public final int shapeCount;
        public final b2MassData massData;
        public final b2AABB bodyAabb;
        public final int shapeType;
        public final b2MassData shapeMass;
        public final b2AABB shapeAabb;

        BodyState(float x, float y, float angle, int shapeCount, b2MassData massData, b2AABB bodyAabb,
                  int shapeType, b2MassData shapeMass, b2AABB shapeAabb) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.shapeCount = shapeCount;
            this.massData = massData;
            this.bodyAabb = bodyAabb;
            this.shapeType = shapeType;
            this.shapeMass = shapeMass;
            this.shapeAabb = shapeAabb;
        }

        String toLinePart() {
            StringBuilder builder = new StringBuilder();
            builder.append(' ')
                .append(formatFloat(x)).append(' ')
                .append(formatFloat(y)).append(' ')
                .append(formatFloat(angle)).append(' ')
                .append(shapeCount).append(' ')
                .append(formatFloat(massData.mass)).append(' ')
                .append(formatFloat(massData.center.x)).append(' ')
                .append(formatFloat(massData.center.y)).append(' ')
                .append(formatFloat(massData.rotationalInertia));
            appendAabb(builder, bodyAabb);
            builder.append(' ')
                .append(shapeType).append(' ')
                .append(formatFloat(shapeMass.mass)).append(' ')
                .append(formatFloat(shapeMass.center.x)).append(' ')
                .append(formatFloat(shapeMass.center.y)).append(' ')
                .append(formatFloat(shapeMass.rotationalInertia));
            appendAabb(builder, shapeAabb);
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
