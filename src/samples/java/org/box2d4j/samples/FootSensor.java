package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2SensorBeginTouchEvent;
import org.box2d4j.b2SensorEndTouchEvent;
import org.box2d4j.b2SensorEvents;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class FootSensor {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final long GROUND = 0x00000001L;
    private static final long PLAYER = 0x00000002L;
    private static final long FOOT = 0x00000004L;

    private FootSensor() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2Vec2[] points = new b2Vec2[20];
        float x = 10.0f;
        for (int i = 0; i < points.length; ++i) {
            points[i] = new b2Vec2(x, 0.0f);
            x -= 1.0f;
        }

        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = points;
        chainDef.count = points.length;
        chainDef.filter.categoryBits = GROUND;
        chainDef.filter.maskBits = FOOT | PLAYER;
        chainDef.isLoop = false;
        chainDef.enableSensorEvents = true;
        b2CreateChain(groundId, chainDef);

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.position = new b2Vec2(0.0f, 1.0f);
        b2BodyId playerId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.filter.categoryBits = PLAYER;
        shapeDef.filter.maskBits = GROUND;
        shapeDef.material.friction = 0.3f;
        b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, -0.5f), new b2Vec2(0.0f, 0.5f), 0.5f);
        b2CreateCapsuleShape(playerId, shapeDef, capsule);

        b2Polygon box = b2MakeOffsetBox(0.5f, 0.25f, new b2Vec2(0.0f, -1.0f), b2Rot_identity);
        shapeDef.filter.categoryBits = FOOT;
        shapeDef.filter.maskBits = GROUND;
        shapeDef.isSensor = true;
        shapeDef.enableSensorEvents = true;
        b2ShapeId sensorId = b2CreatePolygonShape(playerId, shapeDef, box);

        RuntimeState state = new RuntimeState();
        boolean interactive = SampleRuntime.isActive();
        SampleRuntime.hold("footSensor.left", "Move Left", "A", value -> state.leftPressed = value);
        SampleRuntime.hold("footSensor.right", "Move Right", "D", value -> state.rightPressed = value);
        SampleRuntime.beforeStep(() -> applyMovement(playerId, state));
        SampleRuntime.afterStep(() -> processSensorEvents(worldId, sensorId, state));
        for (int step = 0; step < stepCount; ++step) {
            if (!interactive) {
                applyMovement(playerId, state);
            }
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (!interactive) {
                processSensorEvents(worldId, sensorId, state);
            }
        }

        b2ShapeId[] overlaps = new b2ShapeId[8];
        int sensorCapacity = b2Shape_GetSensorCapacity(sensorId);
        int sensorOverlapCount = b2Shape_GetSensorOverlaps(sensorId, overlaps, overlaps.length);
        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(worldId), state.beginTotal, state.endTotal, state.overlapCount, sensorCapacity,
            sensorOverlapCount, bodyState(playerId));
        b2DestroyWorld(worldId);
        return result;
    }

    private static void applyMovement(b2BodyId playerId, RuntimeState state) {
        if (state.leftPressed) {
            b2Body_ApplyForceToCenter(playerId, new b2Vec2(-50.0f, 0.0f), true);
        }
        if (state.rightPressed) {
            b2Body_ApplyForceToCenter(playerId, new b2Vec2(50.0f, 0.0f), true);
        }
    }

    private static void processSensorEvents(b2WorldId worldId, b2ShapeId sensorId, RuntimeState state) {
        b2SensorEvents sensorEvents = b2World_GetSensorEvents(worldId);
        state.beginTotal += sensorEvents.beginCount;
        state.endTotal += sensorEvents.endCount;
        for (b2SensorBeginTouchEvent event : sensorEvents.beginEvents) {
            if (B2_ID_EQUALS(event.sensorShapeId, sensorId)) {
                state.overlapCount += 1;
            }
        }
        for (b2SensorEndTouchEvent event : sensorEvents.endEvents) {
            if (B2_ID_EQUALS(event.sensorShapeId, sensorId)) {
                state.overlapCount -= 1;
            }
        }
    }

    private static final class RuntimeState {
        int overlapCount;
        int beginTotal;
        int endTotal;
        boolean leftPressed;
        boolean rightPressed;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x, velocity.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetShapeCount(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final int beginTotal;
        public final int endTotal;
        public final int overlapCount;
        public final int sensorCapacity;
        public final int sensorOverlapCount;
        public final BodyState player;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, int beginTotal,
               int endTotal, int overlapCount, int sensorCapacity, int sensorOverlapCount, BodyState player) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.beginTotal = beginTotal;
            this.endTotal = endTotal;
            this.overlapCount = overlapCount;
            this.sensorCapacity = sensorCapacity;
            this.sensorOverlapCount = sensorOverlapCount;
            this.player = player;
        }

        public String toLine() {
            return "footSensor " + bodyCount + ' ' + shapeCount + ' ' + contactCount + ' ' + jointCount + ' '
                + awakeBodyCount + ' ' + beginTotal + ' ' + endTotal + ' ' + overlapCount + ' ' + sensorCapacity
                + ' ' + sensorOverlapCount + player.toLinePart();
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
