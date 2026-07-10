package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
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

public final class SensorBookend {
    private static final int DEFAULT_STEP_COUNT = 120;

    private SensorBookend() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        Scene scene = new Scene();
        scene.worldId = b2CreateWorld(b2DefaultWorldDef());
        scene.createGround();
        scene.createSensor1();
        scene.createSensor2();
        scene.createVisitor();

        boolean[] eventsEnabled = {true, true, true};
        boolean[] bodiesEnabled = {true, true, true};
        SampleRuntime.action("sensorBookend.visitor", "Create / Destroy Visitor",
            () -> scene.toggleVisitor(eventsEnabled[0], bodiesEnabled[0]));
        SampleRuntime.toggle("sensorBookend.visitorEvents", "Visitor Events", true, value -> {
            eventsEnabled[0] = value;
            scene.setEventsEnabled(0, value);
        });
        SampleRuntime.toggle("sensorBookend.visitorBody", "Enable Visitor Body", true, value -> {
            bodiesEnabled[0] = value;
            scene.setBodyEnabled(0, value);
        });
        SampleRuntime.action("sensorBookend.sensor1", "Create / Destroy Sensor 1",
            () -> scene.toggleSensor1(eventsEnabled[1], bodiesEnabled[1]));
        SampleRuntime.toggle("sensorBookend.sensor1Events", "Sensor 1 Events", true, value -> {
            eventsEnabled[1] = value;
            scene.setEventsEnabled(1, value);
        });
        SampleRuntime.toggle("sensorBookend.sensor1Body", "Enable Sensor 1 Body", true, value -> {
            bodiesEnabled[1] = value;
            scene.setBodyEnabled(1, value);
        });
        SampleRuntime.action("sensorBookend.sensor2", "Create / Destroy Sensor 2",
            () -> scene.toggleSensor2(eventsEnabled[2], bodiesEnabled[2]));
        SampleRuntime.toggle("sensorBookend.sensor2Events", "Sensor 2 Events", true, value -> {
            eventsEnabled[2] = value;
            scene.setEventsEnabled(2, value);
        });
        SampleRuntime.toggle("sensorBookend.sensor2Body", "Enable Sensor 2 Body", true, value -> {
            bodiesEnabled[2] = value;
            scene.setBodyEnabled(2, value);
        });
        int[] runtimeTotals = {0, 0};
        SampleRuntime.afterStep(() -> scene.processCurrentSensorEvents(runtimeTotals));
        boolean interactive = SampleRuntime.isActive();

        int beginTotal = 0;
        int endTotal = 0;
        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
            if (!interactive) {
                b2SensorEvents sensorEvents = b2World_GetSensorEvents(scene.worldId);
                beginTotal += sensorEvents.beginCount;
                endTotal += sensorEvents.endCount;
                scene.processSensorEvents(sensorEvents);
            }
        }

        if (interactive) {
            beginTotal = runtimeTotals[0];
            endTotal = runtimeTotals[1];
        }

        b2ShapeId[] overlaps = new b2ShapeId[8];
        int sensor1Capacity = b2Shape_GetSensorCapacity(scene.sensorShapeId1);
        int sensor1OverlapCount = b2Shape_GetSensorOverlaps(scene.sensorShapeId1, overlaps, overlaps.length);
        int sensor2Capacity = b2Shape_GetSensorCapacity(scene.sensorShapeId2);
        int sensor2OverlapCount = b2Shape_GetSensorOverlaps(scene.sensorShapeId2, overlaps, overlaps.length);
        b2Counters counters = b2World_GetCounters(scene.worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, counters.jointCount,
            b2World_GetAwakeBodyCount(scene.worldId), beginTotal, endTotal, scene.isVisiting1, scene.isVisiting2,
            scene.sensorsOverlapCount, b2Shape_IsValid(scene.sensorShapeId1), b2Shape_IsValid(scene.sensorShapeId2),
            b2Shape_IsValid(scene.visitorShapeId), sensor1Capacity, sensor1OverlapCount, sensor2Capacity,
            sensor2OverlapCount, bodyState(scene.sensorBodyId2), bodyState(scene.visitorBodyId));
        b2DestroyWorld(scene.worldId);
        return result;
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

    private static final class Scene {
        b2WorldId worldId;
        b2BodyId sensorBodyId1;
        b2ShapeId sensorShapeId1;
        b2BodyId sensorBodyId2;
        b2ShapeId sensorShapeId2;
        b2BodyId visitorBodyId;
        b2ShapeId visitorShapeId;
        boolean isVisiting1;
        boolean isVisiting2;
        int sensorsOverlapCount;

        void createGround() {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);
            b2ShapeDef shapeDef = b2DefaultShapeDef();

            b2CreateSegmentShape(groundId, shapeDef,
                new b2Segment(new b2Vec2(-10.0f, 0.0f), new b2Vec2(10.0f, 0.0f)));
            b2CreateSegmentShape(groundId, shapeDef,
                new b2Segment(new b2Vec2(-10.0f, 0.0f), new b2Vec2(-10.0f, 10.0f)));
            b2CreateSegmentShape(groundId, shapeDef,
                new b2Segment(new b2Vec2(10.0f, 0.0f), new b2Vec2(10.0f, 10.0f)));
        }

        void createSensor1() {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(-2.0f, 1.0f);
            sensorBodyId1 = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.isSensor = true;
            shapeDef.enableSensorEvents = true;
            b2Polygon box = b2MakeSquare(1.0f);
            sensorShapeId1 = b2CreatePolygonShape(sensorBodyId1, shapeDef, box);
        }

        void createSensor2() {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(2.0f, 1.0f);
            sensorBodyId2 = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.isSensor = true;
            shapeDef.enableSensorEvents = true;
            b2Polygon box = b2MakeRoundedBox(0.5f, 0.5f, 0.5f);
            sensorShapeId2 = b2CreatePolygonShape(sensorBodyId2, shapeDef, box);

            shapeDef.isSensor = false;
            shapeDef.enableSensorEvents = false;
            box = b2MakeSquare(0.5f);
            b2CreatePolygonShape(sensorBodyId2, shapeDef, box);
        }

        void createVisitor() {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(-4.0f, 1.0f);
            bodyDef.type = b2_dynamicBody;
            visitorBodyId = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.enableSensorEvents = true;
            b2Circle circle = new b2Circle(new b2Vec2(0.0f, 0.0f), 0.5f);
            visitorShapeId = b2CreateCircleShape(visitorBodyId, shapeDef, circle);
        }

        void toggleVisitor(boolean enableEvents, boolean enableBody) {
            if (isBodyValid(visitorBodyId)) {
                b2DestroyBody(visitorBodyId);
                visitorBodyId = b2_nullBodyId;
                return;
            }
            createVisitor();
            b2Shape_EnableSensorEvents(visitorShapeId, enableEvents);
            setBodyEnabled(visitorBodyId, enableBody);
        }

        void toggleSensor1(boolean enableEvents, boolean enableBody) {
            if (isBodyValid(sensorBodyId1)) {
                b2DestroyBody(sensorBodyId1);
                sensorBodyId1 = b2_nullBodyId;
                return;
            }
            createSensor1();
            b2Shape_EnableSensorEvents(sensorShapeId1, enableEvents);
            setBodyEnabled(sensorBodyId1, enableBody);
        }

        void toggleSensor2(boolean enableEvents, boolean enableBody) {
            if (isBodyValid(sensorBodyId2)) {
                b2DestroyBody(sensorBodyId2);
                sensorBodyId2 = b2_nullBodyId;
                return;
            }
            createSensor2();
            b2Shape_EnableSensorEvents(sensorShapeId2, enableEvents);
            setBodyEnabled(sensorBodyId2, enableBody);
        }

        void setEventsEnabled(int index, boolean enabled) {
            b2ShapeId shapeId = index == 0 ? visitorShapeId : index == 1 ? sensorShapeId1 : sensorShapeId2;
            if (isShapeValid(shapeId)) {
                b2Shape_EnableSensorEvents(shapeId, enabled);
            }
        }

        void setBodyEnabled(int index, boolean enabled) {
            b2BodyId bodyId = index == 0 ? visitorBodyId : index == 1 ? sensorBodyId1 : sensorBodyId2;
            if (isBodyValid(bodyId)) {
                setBodyEnabled(bodyId, enabled);
            }
        }

        void processCurrentSensorEvents(int[] totals) {
            b2SensorEvents events = b2World_GetSensorEvents(worldId);
            totals[0] += events.beginCount;
            totals[1] += events.endCount;
            processSensorEvents(events);
            if (!isShapeValid(visitorShapeId)) {
                visitorShapeId = b2_nullShapeId;
            }
            if (!isShapeValid(sensorShapeId1)) {
                sensorShapeId1 = b2_nullShapeId;
            }
            if (!isShapeValid(sensorShapeId2)) {
                sensorShapeId2 = b2_nullShapeId;
            }
        }

        void processSensorEvents(b2SensorEvents sensorEvents) {
            for (b2SensorBeginTouchEvent event : sensorEvents.beginEvents) {
                if (B2_ID_EQUALS(event.sensorShapeId, sensorShapeId1)) {
                    if (B2_ID_EQUALS(event.visitorShapeId, visitorShapeId)) {
                        isVisiting1 = true;
                    } else {
                        sensorsOverlapCount += 1;
                    }
                } else if (B2_ID_EQUALS(event.sensorShapeId, sensorShapeId2)) {
                    if (B2_ID_EQUALS(event.visitorShapeId, visitorShapeId)) {
                        isVisiting2 = true;
                    } else {
                        sensorsOverlapCount += 1;
                    }
                }
            }

            for (b2SensorEndTouchEvent event : sensorEvents.endEvents) {
                if (B2_ID_EQUALS(event.sensorShapeId, sensorShapeId1)) {
                    if (B2_ID_EQUALS(event.visitorShapeId, visitorShapeId)) {
                        isVisiting1 = false;
                    } else {
                        sensorsOverlapCount -= 1;
                    }
                } else if (B2_ID_EQUALS(event.sensorShapeId, sensorShapeId2)) {
                    if (B2_ID_EQUALS(event.visitorShapeId, visitorShapeId)) {
                        isVisiting2 = false;
                    } else {
                        sensorsOverlapCount -= 1;
                    }
                }
            }
        }

        private static void setBodyEnabled(b2BodyId bodyId, boolean enabled) {
            if (enabled) {
                b2Body_Enable(bodyId);
            } else {
                b2Body_Disable(bodyId);
            }
        }

        private static boolean isBodyValid(b2BodyId bodyId) {
            return bodyId != null && !B2_IS_NULL(bodyId) && b2Body_IsValid(bodyId);
        }

        private static boolean isShapeValid(b2ShapeId shapeId) {
            return shapeId != null && !B2_IS_NULL(shapeId) && b2Shape_IsValid(shapeId);
        }
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final int beginTotal;
        public final int endTotal;
        public final boolean isVisiting1;
        public final boolean isVisiting2;
        public final int sensorsOverlapCount;
        public final boolean sensor1Valid;
        public final boolean sensor2Valid;
        public final boolean visitorValid;
        public final int sensor1Capacity;
        public final int sensor1OverlapCount;
        public final int sensor2Capacity;
        public final int sensor2OverlapCount;
        public final BodyState sensor2;
        public final BodyState visitor;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, int beginTotal,
               int endTotal, boolean isVisiting1, boolean isVisiting2, int sensorsOverlapCount,
               boolean sensor1Valid, boolean sensor2Valid, boolean visitorValid, int sensor1Capacity,
               int sensor1OverlapCount, int sensor2Capacity, int sensor2OverlapCount, BodyState sensor2,
               BodyState visitor) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.beginTotal = beginTotal;
            this.endTotal = endTotal;
            this.isVisiting1 = isVisiting1;
            this.isVisiting2 = isVisiting2;
            this.sensorsOverlapCount = sensorsOverlapCount;
            this.sensor1Valid = sensor1Valid;
            this.sensor2Valid = sensor2Valid;
            this.visitorValid = visitorValid;
            this.sensor1Capacity = sensor1Capacity;
            this.sensor1OverlapCount = sensor1OverlapCount;
            this.sensor2Capacity = sensor2Capacity;
            this.sensor2OverlapCount = sensor2OverlapCount;
            this.sensor2 = sensor2;
            this.visitor = visitor;
        }

        public String toLine() {
            return "sensorBookend " + bodyCount + ' ' + shapeCount + ' ' + contactCount + ' ' + jointCount + ' '
                + awakeBodyCount + ' ' + beginTotal + ' ' + endTotal + ' ' + boolInt(isVisiting1) + ' '
                + boolInt(isVisiting2) + ' ' + sensorsOverlapCount + ' ' + boolInt(sensor1Valid) + ' '
                + boolInt(sensor2Valid) + ' ' + boolInt(visitorValid) + ' ' + sensor1Capacity + ' '
                + sensor1OverlapCount + ' ' + sensor2Capacity + ' ' + sensor2OverlapCount
                + sensor2.toLinePart() + visitor.toLinePart();
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

    private static int boolInt(boolean value) {
        return value ? 1 : 0;
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
