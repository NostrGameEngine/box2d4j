package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2Segment;
import org.box2d4j.b2SensorBeginTouchEvent;
import org.box2d4j.b2SensorEndTouchEvent;
import org.box2d4j.b2SensorEvents;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class Sleep {
    private static final int DEFAULT_STEP_COUNT = 20;
    private static final int CREATE_INVOKER_STEP = 10;
    private static final int DESTROY_INVOKER_STEP = 15;

    private Sleep() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        Scene scene = createScene(worldId);
        int[] eventCounts = {0, 0};
        boolean interactive = SampleRuntime.isActive();
        b2BodyId pendulumId = scene.bodies[5];
        SampleRuntime.slider("sleep.velocity", "Sleep Velocity", b2Body_GetSleepThreshold(pendulumId),
            0.0f, 1.0f, 0.01f, value -> {
                b2Body_SetSleepThreshold(pendulumId, value);
                b2Body_SetAwake(pendulumId, true);
            });
        SampleRuntime.slider("sleep.angularDamping", "Angular Damping", b2Body_GetAngularDamping(pendulumId),
            0.0f, 2.0f, 0.01f, value -> b2Body_SetAngularDamping(pendulumId, value));
        SampleRuntime.action("sleep.invoker", "Create / Destroy Invoker", () -> scene.toggleInvoker(worldId));
        SampleRuntime.afterStep(() -> processEvents(worldId, scene, eventCounts));

        for (int step = 0; step < stepCount; ++step) {
            if (!interactive && step == CREATE_INVOKER_STEP) {
                scene.toggleInvoker(worldId);
            } else if (!interactive && step == DESTROY_INVOKER_STEP) {
                scene.toggleInvoker(worldId);
            }

            b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (!interactive) {
                processEvents(worldId, scene, eventCounts);
            }
        }

        b2Counters counters = b2World_GetCounters(worldId);
        BodyState[] states = new BodyState[scene.bodies.length];
        for (int i = 0; i < scene.bodies.length; ++i) {
            states[i] = bodyState(scene.bodies[i]);
        }
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            b2World_GetAwakeBodyCount(worldId), eventCounts[0], eventCounts[1], scene.sensorTouching[0],
            scene.sensorTouching[1], !B2_IS_NULL(scene.staticBodyId), states);

        b2DestroyWorld(worldId);
        return result;
    }

    private static void processEvents(b2WorldId worldId, Scene scene, int[] eventCounts) {
        b2SensorEvents sensorEvents = b2World_GetSensorEvents(worldId);
        eventCounts[0] += sensorEvents.beginCount;
        eventCounts[1] += sensorEvents.endCount;
        scene.updateSensorTouching(sensorEvents);
    }

    private static Scene createScene(b2WorldId worldId) {
        b2BodyId groundId;
        b2ShapeId groundShapeId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            groundId = b2CreateBody(worldId, bodyDef);

            b2Segment segment = new b2Segment(new b2Vec2(-40.0f, 0.0f), new b2Vec2(40.0f, 0.0f));
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.enableSensorEvents = true;
            groundShapeId = b2CreateSegmentShape(groundId, shapeDef, segment);
        }

        b2BodyId[] bodies = new b2BodyId[7];
        b2ShapeId[] sensorIds = new b2ShapeId[2];
        for (int i = 0; i < 2; ++i) {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(-4.0f, 3.0f + 2.0f * i);
            bodyDef.isAwake = false;
            bodyDef.enableSleep = true;
            bodies[i] = b2CreateBody(worldId, bodyDef);

            b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, 1.0f), new b2Vec2(1.0f, 1.0f), 0.75f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateCapsuleShape(bodies[i], shapeDef, capsule);

            shapeDef.isSensor = true;
            shapeDef.enableSensorEvents = true;
            capsule.radius = 1.0f;
            sensorIds[i] = b2CreateCapsuleShape(bodies[i], shapeDef, capsule);
        }

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(0.0f, 3.0f);
            bodyDef.isAwake = false;
            bodyDef.enableSleep = false;
            bodies[2] = b2CreateBody(worldId, bodyDef);

            b2Circle circle = new b2Circle(new b2Vec2(1.0f, 1.0f), 1.0f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateCircleShape(bodies[2], shapeDef, circle);
        }

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(5.0f, 3.0f);
            bodyDef.isAwake = true;
            bodyDef.enableSleep = false;
            bodies[3] = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeOffsetBox(1.0f, 1.0f, new b2Vec2(0.0f, 1.0f), b2MakeRot(0.25f * B2_PI));
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreatePolygonShape(bodies[3], shapeDef, box);
        }

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(5.0f, 1.0f);
            bodyDef.isAwake = false;
            bodyDef.enableSleep = true;
            bodies[4] = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeSquare(1.0f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreatePolygonShape(bodies[4], shapeDef, box);
        }

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(0.0f, 100.0f);
            bodyDef.angularDamping = 0.5f;
            bodyDef.sleepThreshold = 0.05f;
            bodies[5] = b2CreateBody(worldId, bodyDef);

            b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, 0.0f), new b2Vec2(90.0f, 0.0f), 0.25f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateCapsuleShape(bodies[5], shapeDef, capsule);

            b2Vec2 pivot = bodyDef.position;
            b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
            jointDef.bodyIdA = groundId;
            jointDef.bodyIdB = bodies[5];
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            b2CreateRevoluteJoint(worldId, jointDef);
        }

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(-10.0f, 1.0f);
            bodyDef.isAwake = false;
            bodyDef.enableSleep = true;
            bodies[6] = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeSquare(1.0f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreatePolygonShape(bodies[6], shapeDef, box);
        }

        return new Scene(groundShapeId, sensorIds, bodies);
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(b2Body_IsAwake(bodyId), b2Body_IsSleepEnabled(bodyId), b2Body_IsEnabled(bodyId),
            b2Body_GetContactCapacity(bodyId), p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)), v.x, v.y,
            b2Body_GetAngularVelocity(bodyId), b2Body_GetSleepThreshold(bodyId), b2Body_GetAngularDamping(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    private static final class Scene {
        final b2ShapeId groundShapeId;
        final b2ShapeId[] sensorIds;
        final b2BodyId[] bodies;
        final boolean[] sensorTouching = new boolean[2];
        b2BodyId staticBodyId = b2_nullBodyId;

        Scene(b2ShapeId groundShapeId, b2ShapeId[] sensorIds, b2BodyId[] bodies) {
            this.groundShapeId = groundShapeId;
            this.sensorIds = sensorIds;
            this.bodies = bodies;
        }

        void toggleInvoker(b2WorldId worldId) {
            if (B2_IS_NULL(staticBodyId)) {
                b2BodyDef bodyDef = b2DefaultBodyDef();
                bodyDef.position = new b2Vec2(-10.5f, 3.0f);
                staticBodyId = b2CreateBody(worldId, bodyDef);

                b2Polygon box = b2MakeOffsetBox(2.0f, 0.1f, new b2Vec2(0.0f, 0.0f), b2MakeRot(0.25f * B2_PI));
                b2ShapeDef shapeDef = b2DefaultShapeDef();
                shapeDef.invokeContactCreation = true;
                b2CreatePolygonShape(staticBodyId, shapeDef, box);
            } else {
                b2DestroyBody(staticBodyId);
                staticBodyId = b2_nullBodyId;
            }
        }

        void updateSensorTouching(b2SensorEvents sensorEvents) {
            for (b2SensorBeginTouchEvent event : sensorEvents.beginEvents) {
                if (B2_ID_EQUALS(event.visitorShapeId, groundShapeId)) {
                    for (int i = 0; i < sensorIds.length; ++i) {
                        if (B2_ID_EQUALS(event.sensorShapeId, sensorIds[i])) {
                            sensorTouching[i] = true;
                        }
                    }
                }
            }
            for (b2SensorEndTouchEvent event : sensorEvents.endEvents) {
                if (B2_ID_EQUALS(event.visitorShapeId, groundShapeId)) {
                    for (int i = 0; i < sensorIds.length; ++i) {
                        if (B2_ID_EQUALS(event.sensorShapeId, sensorIds[i])) {
                            sensorTouching[i] = false;
                        }
                    }
                }
            }
        }
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int awakeBodyCount;
        public final int sensorBeginCount;
        public final int sensorEndCount;
        public final boolean sensor0Touching;
        public final boolean sensor1Touching;
        public final boolean staticBodyAlive;
        public final BodyState[] states;

        public Result(int bodyCount, int shapeCount, int contactCount, int awakeBodyCount, int sensorBeginCount,
            int sensorEndCount, boolean sensor0Touching, boolean sensor1Touching, boolean staticBodyAlive,
            BodyState[] states) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.sensorBeginCount = sensorBeginCount;
            this.sensorEndCount = sensorEndCount;
            this.sensor0Touching = sensor0Touching;
            this.sensor1Touching = sensor1Touching;
            this.staticBodyAlive = staticBodyAlive;
            this.states = states;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("sleep ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(sensorBeginCount).append(' ')
                .append(sensorEndCount).append(' ')
                .append(sensor0Touching ? 1 : 0).append(' ')
                .append(sensor1Touching ? 1 : 0).append(' ')
                .append(staticBodyAlive ? 1 : 0).append(' ')
                .append(states.length);
            for (BodyState state : states) {
                builder.append(state.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class BodyState {
        public final boolean awake;
        public final boolean sleepEnabled;
        public final boolean enabled;
        public final int contactCapacity;
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final float sleepThreshold;
        public final float angularDamping;

        BodyState(boolean awake, boolean sleepEnabled, boolean enabled, int contactCapacity, float x, float y,
            float angle, float velocityX, float velocityY, float angularVelocity, float sleepThreshold,
            float angularDamping) {
            this.awake = awake;
            this.sleepEnabled = sleepEnabled;
            this.enabled = enabled;
            this.contactCapacity = contactCapacity;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.sleepThreshold = sleepThreshold;
            this.angularDamping = angularDamping;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %d %d %d %d %s %s %s %s %s %s %s %s",
                awake ? 1 : 0, sleepEnabled ? 1 : 0, enabled ? 1 : 0, contactCapacity, formatFloat(x), formatFloat(y),
                formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY), formatFloat(angularVelocity),
                formatFloat(sleepThreshold), formatFloat(angularDamping));
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
