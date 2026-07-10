package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RayResult;
import org.box2d4j.b2Segment;
import org.box2d4j.b2SensorEvents;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class SensorTypes {
    private static final int DEFAULT_STEP_COUNT = 120;
    private static final long GROUND = 0x00000001L;
    private static final long SENSOR = 0x00000002L;
    private static final long DEFAULT = 0x00000004L;

    private SensorTypes() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        Scene scene = createScene(worldId);

        int beginTotal = 0;
        int endTotal = 0;
        b2RayResult ray = new b2RayResult();
        for (int step = 0; step < stepCount; ++step) {
            b2Vec2 position = b2Body_GetPosition(scene.kinematicBodyId);
            if (position.y < 0.0f) {
                b2Body_SetLinearVelocity(scene.kinematicBodyId, new b2Vec2(0.0f, 1.0f));
            } else if (position.y > 3.0f) {
                b2Body_SetLinearVelocity(scene.kinematicBodyId, new b2Vec2(0.0f, -1.0f));
            }

            b2World_Step(worldId, 1.0f / 60.0f, 4);
            b2SensorEvents sensorEvents = b2World_GetSensorEvents(worldId);
            beginTotal += sensorEvents.beginCount;
            endTotal += sensorEvents.endCount;
            ray = b2World_CastRayClosest(worldId, new b2Vec2(5.0f, 1.0f), new b2Vec2(-10.0f, 0.0f),
                b2DefaultQueryFilter());
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            counters.jointCount, b2World_GetAwakeBodyCount(worldId), beginTotal, endTotal,
            sensorState(scene.staticSensorId), sensorState(scene.kinematicSensorId), sensorState(scene.dynamicSensorId),
            bodyState(scene.kinematicBodyId), bodyState(scene.dynamicBodyId), bodyState(scene.ballBodyId),
            rayState(ray));
        b2DestroyWorld(worldId);
        return result;
    }

    private static Scene createScene(b2WorldId worldId) {
        Scene scene = new Scene();

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.name = "ground";
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.filter.categoryBits = GROUND;
        shapeDef.filter.maskBits = DEFAULT;
        shapeDef.enableSensorEvents = true;
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-6.0f, 0.0f), new b2Vec2(6.0f, 0.0f)));
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(-6.0f, 0.0f), new b2Vec2(-6.0f, 4.0f)));
        b2CreateSegmentShape(groundId, shapeDef,
            new b2Segment(new b2Vec2(6.0f, 0.0f), new b2Vec2(6.0f, 4.0f)));

        bodyDef = b2DefaultBodyDef();
        bodyDef.name = "static sensor";
        bodyDef.type = b2_staticBody;
        bodyDef.position = new b2Vec2(-3.0f, 0.8f);
        b2BodyId staticBodyId = b2CreateBody(worldId, bodyDef);

        shapeDef = b2DefaultShapeDef();
        shapeDef.filter.categoryBits = SENSOR;
        shapeDef.isSensor = true;
        shapeDef.enableSensorEvents = true;
        b2Polygon box = b2MakeSquare(1.0f);
        scene.staticSensorId = b2CreatePolygonShape(staticBodyId, shapeDef, box);

        bodyDef = b2DefaultBodyDef();
        bodyDef.name = "kinematic sensor";
        bodyDef.type = b2_kinematicBody;
        bodyDef.position = new b2Vec2(0.0f, 0.0f);
        bodyDef.linearVelocity = new b2Vec2(0.0f, 1.0f);
        scene.kinematicBodyId = b2CreateBody(worldId, bodyDef);

        shapeDef = b2DefaultShapeDef();
        shapeDef.filter.categoryBits = SENSOR;
        shapeDef.isSensor = true;
        shapeDef.enableSensorEvents = true;
        box = b2MakeSquare(1.0f);
        scene.kinematicSensorId = b2CreatePolygonShape(scene.kinematicBodyId, shapeDef, box);

        bodyDef = b2DefaultBodyDef();
        bodyDef.name = "dynamic sensor";
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(3.0f, 1.0f);
        scene.dynamicBodyId = b2CreateBody(worldId, bodyDef);

        shapeDef = b2DefaultShapeDef();
        shapeDef.filter.categoryBits = SENSOR;
        shapeDef.isSensor = true;
        shapeDef.enableSensorEvents = true;
        box = b2MakeSquare(1.0f);
        scene.dynamicSensorId = b2CreatePolygonShape(scene.dynamicBodyId, shapeDef, box);

        shapeDef.filter.categoryBits = DEFAULT;
        shapeDef.isSensor = false;
        shapeDef.enableSensorEvents = false;
        box = b2MakeSquare(0.8f);
        b2CreatePolygonShape(scene.dynamicBodyId, shapeDef, box);

        bodyDef = b2DefaultBodyDef();
        bodyDef.name = "ball_01";
        bodyDef.position = new b2Vec2(-5.0f, 1.0f);
        bodyDef.type = b2_dynamicBody;
        scene.ballBodyId = b2CreateBody(worldId, bodyDef);

        shapeDef = b2DefaultShapeDef();
        shapeDef.filter.categoryBits = DEFAULT;
        shapeDef.filter.maskBits = GROUND | DEFAULT | SENSOR;
        shapeDef.enableSensorEvents = true;
        b2CreateCircleShape(scene.ballBodyId, shapeDef, new b2Circle(new b2Vec2(0.0f, 0.0f), 0.5f));

        return scene;
    }

    private static SensorState sensorState(b2ShapeId sensorId) {
        int capacity = b2Shape_GetSensorCapacity(sensorId);
        b2ShapeId[] overlaps = new b2ShapeId[Math.max(capacity, 1)];
        int count = b2Shape_GetSensorOverlaps(sensorId, overlaps, capacity);
        int indexSum = 0;
        int generationSum = 0;
        for (int i = 0; i < count; ++i) {
            indexSum += overlaps[i].index1;
            generationSum += overlaps[i].generation;
        }
        return new SensorState(capacity, count, indexSum, generationSum);
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s, velocity.x, velocity.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetShapeCount(bodyId), b2Body_GetContactCapacity(bodyId));
    }

    private static RayState rayState(b2RayResult ray) {
        return new RayState(ray.hit, ray.fraction, ray.point.x, ray.point.y, ray.normal.x, ray.normal.y,
            ray.shapeId.index1, ray.shapeId.generation, ray.nodeVisits, ray.leafVisits);
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    private static final class Scene {
        b2ShapeId staticSensorId;
        b2ShapeId kinematicSensorId;
        b2ShapeId dynamicSensorId;
        b2BodyId kinematicBodyId;
        b2BodyId dynamicBodyId;
        b2BodyId ballBodyId;
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final int beginTotal;
        public final int endTotal;
        public final SensorState staticSensor;
        public final SensorState kinematicSensor;
        public final SensorState dynamicSensor;
        public final BodyState kinematicBody;
        public final BodyState dynamicBody;
        public final BodyState ballBody;
        public final RayState ray;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, int beginTotal,
               int endTotal, SensorState staticSensor, SensorState kinematicSensor, SensorState dynamicSensor,
               BodyState kinematicBody, BodyState dynamicBody, BodyState ballBody, RayState ray) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.beginTotal = beginTotal;
            this.endTotal = endTotal;
            this.staticSensor = staticSensor;
            this.kinematicSensor = kinematicSensor;
            this.dynamicSensor = dynamicSensor;
            this.kinematicBody = kinematicBody;
            this.dynamicBody = dynamicBody;
            this.ballBody = ballBody;
            this.ray = ray;
        }

        public String toLine() {
            return "sensorTypes " + bodyCount + ' ' + shapeCount + ' ' + contactCount + ' ' + jointCount + ' '
                + awakeBodyCount + ' ' + beginTotal + ' ' + endTotal + staticSensor.toLinePart()
                + kinematicSensor.toLinePart() + dynamicSensor.toLinePart() + kinematicBody.toLinePart()
                + dynamicBody.toLinePart() + ballBody.toLinePart() + ray.toLinePart();
        }
    }

    public static final class SensorState {
        public final int capacity;
        public final int count;
        public final int indexSum;
        public final int generationSum;

        SensorState(int capacity, int count, int indexSum, int generationSum) {
            this.capacity = capacity;
            this.count = count;
            this.indexSum = indexSum;
            this.generationSum = generationSum;
        }

        String toLinePart() {
            return " " + capacity + ' ' + count + ' ' + indexSum + ' ' + generationSum;
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

    public static final class RayState {
        public final boolean hit;
        public final float fraction;
        public final float pointX;
        public final float pointY;
        public final float normalX;
        public final float normalY;
        public final int shapeIndex;
        public final int shapeGeneration;
        public final int nodeVisits;
        public final int leafVisits;

        RayState(boolean hit, float fraction, float pointX, float pointY, float normalX, float normalY,
                 int shapeIndex, int shapeGeneration, int nodeVisits, int leafVisits) {
            this.hit = hit;
            this.fraction = fraction;
            this.pointX = pointX;
            this.pointY = pointY;
            this.normalX = normalX;
            this.normalY = normalY;
            this.shapeIndex = shapeIndex;
            this.shapeGeneration = shapeGeneration;
            this.nodeVisits = nodeVisits;
            this.leafVisits = leafVisits;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %d %s %s %s %s %s %d %d %d %d", hit ? 1 : 0,
                formatFloat(fraction), formatFloat(pointX), formatFloat(pointY), formatFloat(normalX),
                formatFloat(normalY), shapeIndex, shapeGeneration, nodeVisits, leafVisits);
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
