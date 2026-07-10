package org.box2d4j.samples;

import org.box2d4j.b2AABB;
import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2MassData;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Rot;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ModifyGeometry {
    private ModifyGeometry() {
    }

    public static Result run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2Polygon box = b2MakeOffsetBox(10.0f, 1.0f, new b2Vec2(0.0f, -1.0f), b2Rot_identity);
            b2CreatePolygonShape(groundId, shapeDef, box);
        }

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(0.0f, 4.0f);
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2Polygon box = b2MakeBox(1.0f, 1.0f);
            b2CreatePolygonShape(bodyId, shapeDef, box);
        }

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_kinematicBody;
        bodyDef.position = new b2Vec2(0.0f, 1.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Circle circle = new b2Circle(new b2Vec2(0.0f, 0.0f), 0.5f);
        b2ShapeId shapeId = b2CreateCircleShape(bodyId, shapeDef, circle);

        State[] states;
        if (SampleRuntime.isActive()) {
            float[] scale = {1.0f};
            int[] shapeType = {b2_circleShape};
            Runnable updateShape = () -> updateShape(bodyId, shapeId, shapeType[0], scale[0]);
            SampleRuntime.choice("modify.shape", "Shape", 0,
                new String[] {"Circle", "Capsule", "Segment", "Polygon"}, value -> {
                    shapeType[0] = value;
                    updateShape.run();
                });
            SampleRuntime.slider("modify.scale", "Scale", scale[0], 0.1f, 10.0f, 0.1f, value -> {
                scale[0] = value;
                updateShape.run();
            });
            SampleRuntime.choice("modify.bodyType", "Body Type", b2_kinematicBody,
                new String[] {"Static", "Kinematic", "Dynamic"}, value -> b2Body_SetType(bodyId, value));
            states = new State[] {state("interactive", worldId, bodyId, shapeId)};
        } else {
            states = new State[5];
            states[0] = state("circle", worldId, bodyId, shapeId);

            b2Shape_SetCapsule(shapeId,
                new b2Capsule(new b2Vec2(-0.75f, 0.0f), new b2Vec2(0.0f, 0.75f), 0.75f));
            b2Body_ApplyMassFromShapes(bodyId);
            states[1] = state("capsule", worldId, bodyId, shapeId);

            b2Shape_SetSegment(shapeId, new b2Segment(new b2Vec2(-0.375f, 0.0f), new b2Vec2(0.5625f, 0.0f)));
            b2Body_ApplyMassFromShapes(bodyId);
            states[2] = state("segment", worldId, bodyId, shapeId);

            b2Shape_SetPolygon(shapeId, b2MakeBox(0.625f, 0.9375f));
            b2Body_SetType(bodyId, b2_dynamicBody);
            b2Body_ApplyMassFromShapes(bodyId);
            states[3] = state("polygonDynamic", worldId, bodyId, shapeId);

            b2Body_SetType(bodyId, b2_staticBody);
            b2Body_ApplyMassFromShapes(bodyId);
            states[4] = state("polygonStatic", worldId, bodyId, shapeId);
        }

        b2DestroyWorld(worldId);
        return new Result(states);
    }

    private static void updateShape(b2BodyId bodyId, b2ShapeId shapeId, int shapeType, float scale) {
        if (shapeType == b2_circleShape) {
            b2Shape_SetCircle(shapeId, new b2Circle(new b2Vec2(), 0.5f * scale));
        } else if (shapeType == b2_capsuleShape) {
            b2Shape_SetCapsule(shapeId, new b2Capsule(new b2Vec2(-0.75f * scale, 0.0f),
                new b2Vec2(0.0f, 0.75f * scale), 0.75f * scale));
        } else if (shapeType == b2_segmentShape) {
            b2Shape_SetSegment(shapeId, new b2Segment(new b2Vec2(-0.5f * scale, 0.0f),
                new b2Vec2(0.75f * scale, 0.0f)));
        } else {
            b2Shape_SetPolygon(shapeId, b2MakeBox(0.5f * scale, 0.75f * scale));
        }
        b2Body_ApplyMassFromShapes(bodyId);
    }

    private static State state(String label, b2WorldId worldId, b2BodyId bodyId, b2ShapeId shapeId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        b2MassData massData = b2Body_GetMassData(bodyId);
        b2AABB aabb = b2Shape_GetAABB(shapeId);
        b2Counters counters = b2World_GetCounters(worldId);
        return new State(label, b2Body_GetType(bodyId), b2Shape_GetType(shapeId),
            p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)), v.x, v.y, b2Body_GetAngularVelocity(bodyId),
            massData.mass, massData.center.x, massData.center.y, massData.rotationalInertia,
            aabb.lowerBound.x, aabb.lowerBound.y, aabb.upperBound.x, aabb.upperBound.y,
            counters.contactCount, b2World_GetAwakeBodyCount(worldId));
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final State[] states;

        public Result(State[] states) {
            this.states = states;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder("modifyGeometry");
            for (State state : states) {
                builder.append(state.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class State {
        public final String label;
        public final int bodyType;
        public final int shapeType;
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final float mass;
        public final float centerX;
        public final float centerY;
        public final float rotationalInertia;
        public final float lowerX;
        public final float lowerY;
        public final float upperX;
        public final float upperY;
        public final int contactCount;
        public final int awakeBodyCount;

        State(String label, int bodyType, int shapeType, float x, float y, float angle, float velocityX,
              float velocityY, float angularVelocity, float mass, float centerX, float centerY,
              float rotationalInertia, float lowerX, float lowerY, float upperX, float upperY,
              int contactCount, int awakeBodyCount) {
            this.label = label;
            this.bodyType = bodyType;
            this.shapeType = shapeType;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.mass = mass;
            this.centerX = centerX;
            this.centerY = centerY;
            this.rotationalInertia = rotationalInertia;
            this.lowerX = lowerX;
            this.lowerY = lowerY;
            this.upperX = upperX;
            this.upperY = upperY;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %d %d %s %s %s %s %s %s %s %s %s %s %s %s %s %s %d %d",
                label, bodyType, shapeType, formatFloat(x), formatFloat(y), formatFloat(angle),
                formatFloat(velocityX), formatFloat(velocityY), formatFloat(angularVelocity), formatFloat(mass),
                formatFloat(centerX), formatFloat(centerY), formatFloat(rotationalInertia), formatFloat(lowerX),
                formatFloat(lowerY), formatFloat(upperX), formatFloat(upperY), contactCount, awakeBodyCount);
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
