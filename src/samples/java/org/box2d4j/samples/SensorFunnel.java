package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2Counters;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2SensorBeginTouchEvent;
import org.box2d4j.b2SensorEvents;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2SurfaceMaterial;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;
import org.box2d4j.b2WeldJointDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.box2d4j.B2.*;

public final class SensorFunnel {
    private static final int DEFAULT_STEP_COUNT = 88;
    private static final int DONUT = 1;
    private static final int HUMAN = 2;
    private static final int MAX_COUNT = 32;
    private static final int DONUT_SIDES = 10;

    private SensorFunnel() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        Scene scene = createScene(worldId);
        scene.createElement();
        SampleRuntime.choice("sensorFunnel.type", "Element", HUMAN - 1, new String[] {"Donut", "Human"},
            index -> scene.setType(index + 1));
        SampleRuntime.afterStep(scene::afterStep);
        boolean interactive = SampleRuntime.isActive();

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (!interactive) {
                scene.afterStep();
            }
        }

        List<BodyState> bodyStates = new ArrayList<>();
        long activeMask = 0L;
        int activeCount = 0;
        for (int i = 0; i < MAX_COUNT; ++i) {
            if (scene.spawned[i]) {
                activeCount += 1;
                activeMask |= 1L << i;
                b2BodyId[] bodies = scene.elementTypes[i] == DONUT
                    ? scene.donuts[i].bodyIds : scene.humans[i].bodies;
                for (int bodyIndex = 0; bodyIndex < bodies.length; ++bodyIndex) {
                    bodyStates.add(bodyState(i, bodyIndex, bodies[bodyIndex]));
                }
            }
        }

        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            counters.jointCount, b2World_GetAwakeBodyCount(worldId), scene.type, scene.createdTotal,
            scene.destroyedTotal, activeCount, activeMask, scene.beginTotal, scene.endTotal, scene.lastBeginCount,
            scene.wait, scene.side, bodyStates.toArray(new BodyState[0]));
        b2DestroyWorld(worldId);
        return result;
    }

    private static Scene createScene(b2WorldId worldId) {
        Scene scene = new Scene();
        scene.worldId = worldId;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);

        b2Vec2[] points = {
            new b2Vec2(-16.8672504f, 31.088623f),
            new b2Vec2(16.8672485f, 31.088623f),
            new b2Vec2(16.8672485f, 17.1978741f),
            new b2Vec2(8.26824951f, 11.906374f),
            new b2Vec2(16.8672485f, 11.906374f),
            new b2Vec2(16.8672485f, -0.661376953f),
            new b2Vec2(8.26824951f, -5.953125f),
            new b2Vec2(16.8672485f, -5.953125f),
            new b2Vec2(16.8672485f, -13.229126f),
            new b2Vec2(3.63799858f, -23.151123f),
            new b2Vec2(3.63799858f, -31.088623f),
            new b2Vec2(-3.63800049f, -31.088623f),
            new b2Vec2(-3.63800049f, -23.151123f),
            new b2Vec2(-16.8672504f, -13.229126f),
            new b2Vec2(-16.8672504f, -5.953125f),
            new b2Vec2(-8.26825142f, -5.953125f),
            new b2Vec2(-16.8672504f, -0.661376953f),
            new b2Vec2(-16.8672504f, 11.906374f),
            new b2Vec2(-8.26825142f, 11.906374f),
            new b2Vec2(-16.8672504f, 17.1978741f)
        };

        b2SurfaceMaterial material = new b2SurfaceMaterial();
        material.friction = 0.2f;
        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = points;
        chainDef.count = points.length;
        chainDef.isLoop = true;
        chainDef.materials = new b2SurfaceMaterial[] {material};
        chainDef.materialCount = 1;
        b2CreateChain(groundId, chainDef);

        float sign = 1.0f;
        float y = 14.0f;
        for (int i = 0; i < 3; ++i) {
            bodyDef.position = new b2Vec2(0.0f, y);
            bodyDef.type = b2_dynamicBody;
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

            b2Polygon box = b2MakeBox(6.0f, 0.5f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.material.friction = 0.1f;
            shapeDef.material.restitution = 1.0f;
            shapeDef.density = 1.0f;
            b2CreatePolygonShape(bodyId, shapeDef, box);

            b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
            revoluteDef.bodyIdA = groundId;
            revoluteDef.bodyIdB = bodyId;
            revoluteDef.localAnchorA = bodyDef.position;
            revoluteDef.localAnchorB = new b2Vec2();
            revoluteDef.maxMotorTorque = 200.0f;
            revoluteDef.motorSpeed = 2.0f * sign;
            revoluteDef.enableMotor = true;
            b2CreateRevoluteJoint(worldId, revoluteDef);

            y -= 14.0f;
            sign = -sign;
        }

        b2Polygon box = b2MakeOffsetBox(4.0f, 1.0f, new b2Vec2(0.0f, -30.5f), b2Rot_identity);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.isSensor = true;
        shapeDef.enableSensorEvents = true;
        b2CreatePolygonShape(groundId, shapeDef, box);

        scene.wait = 0.5f;
        scene.side = -15.0f;
        scene.type = HUMAN;
        return scene;
    }

    private static BodyState bodyState(int elementIndex, int boneIndex, b2BodyId bodyId) {
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(elementIndex, boneIndex, transform.p.x, transform.p.y, transform.q.c, transform.q.s,
            velocity.x, velocity.y, b2Body_GetAngularVelocity(bodyId), b2Body_GetShapeCount(bodyId),
            b2Body_GetContactCapacity(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    private static final class Scene {
        final Ragdoll.Human[] humans = new Ragdoll.Human[MAX_COUNT];
        final Donut[] donuts = new Donut[MAX_COUNT];
        final boolean[] spawned = new boolean[MAX_COUNT];
        final int[] elementTypes = new int[MAX_COUNT];
        b2WorldId worldId;
        int type;
        float wait;
        float side;
        int createdTotal;
        int destroyedTotal;
        int beginTotal;
        int endTotal;
        int lastBeginCount;

        void createElement() {
            int index = -1;
            for (int i = 0; i < MAX_COUNT; ++i) {
                if (!spawned[i]) {
                    index = i;
                    break;
                }
            }

            if (index == -1) {
                return;
            }

            Element element = new Element(index);
            if (type == DONUT) {
                Donut donut = Donut.create(worldId, new b2Vec2(side, 29.5f), element);
                donuts[index] = donut;
            } else {
                Ragdoll.Human human = Ragdoll.Human.create(worldId, new b2Vec2(side, 29.5f), 2.0f, 0.05f, 6.0f,
                    0.5f, index + 1);
                for (b2BodyId bodyId : human.bodies) {
                    b2Body_SetUserData(bodyId, element);
                    b2ShapeId[] shapeIds = new b2ShapeId[2];
                    int shapeCount = b2Body_GetShapes(bodyId, shapeIds, shapeIds.length);
                    for (int shapeIndex = 0; shapeIndex < shapeCount; ++shapeIndex) {
                        b2Shape_EnableSensorEvents(shapeIds[shapeIndex], true);
                    }
                }
                humans[index] = human;
            }

            spawned[index] = true;
            elementTypes[index] = type;
            createdTotal += 1;
            side = -side;
        }

        void destroyElement(int index) {
            if (!spawned[index]) {
                return;
            }

            if (elementTypes[index] == DONUT) {
                donuts[index].destroy();
                donuts[index] = null;
            } else {
                humans[index].destroy();
                humans[index] = null;
            }
            spawned[index] = false;
            elementTypes[index] = 0;
            destroyedTotal += 1;
        }

        void clear() {
            for (int i = 0; i < MAX_COUNT; ++i) {
                destroyElement(i);
            }
        }

        void setType(int nextType) {
            if (type != nextType) {
                clear();
                type = nextType;
                wait = 0.0f;
            }
        }

        void afterStep() {
            boolean[] deferredDestruction = new boolean[MAX_COUNT];
            b2SensorEvents sensorEvents = b2World_GetSensorEvents(worldId);
            lastBeginCount = sensorEvents.beginCount;
            beginTotal += sensorEvents.beginCount;
            endTotal += sensorEvents.endCount;
            for (b2SensorBeginTouchEvent event : sensorEvents.beginEvents) {
                b2BodyId bodyId = b2Shape_GetBody(event.visitorShapeId);
                Object userData = b2Body_GetUserData(bodyId);
                if (userData instanceof Element) {
                    deferredDestruction[((Element) userData).index] = true;
                }
            }

            for (int i = 0; i < MAX_COUNT; ++i) {
                if (deferredDestruction[i]) {
                    destroyElement(i);
                }
            }

            wait -= 1.0f / 60.0f;
            if (wait < 0.0f) {
                createElement();
                wait += 0.5f;
            }
        }
    }

    private static final class Donut {
        final b2BodyId[] bodyIds = new b2BodyId[DONUT_SIDES];
        final b2JointId[] jointIds = new b2JointId[DONUT_SIDES];

        static Donut create(b2WorldId worldId, b2Vec2 position, Element element) {
            Donut donut = new Donut();
            float radius = 1.0f;
            float deltaAngle = 2.0f * B2_PI / DONUT_SIDES;
            float length = 2.0f * B2_PI * radius / DONUT_SIDES;
            b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, -0.5f * length),
                new b2Vec2(0.0f, 0.5f * length), 0.25f);
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.userData = element;
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.enableSensorEvents = true;
            shapeDef.material.friction = 0.3f;

            float angle = 0.0f;
            for (int i = 0; i < DONUT_SIDES; ++i) {
                bodyDef.position = new b2Vec2(radius * (float) Math.cos(angle) + position.x,
                    radius * (float) Math.sin(angle) + position.y);
                bodyDef.rotation = b2MakeRot(angle);
                donut.bodyIds[i] = b2CreateBody(worldId, bodyDef);
                b2CreateCapsuleShape(donut.bodyIds[i], shapeDef, capsule);
                angle += deltaAngle;
            }

            b2WeldJointDef weldDef = b2DefaultWeldJointDef();
            weldDef.angularHertz = 5.0f;
            weldDef.angularDampingRatio = 0.0f;
            weldDef.localAnchorA = new b2Vec2(0.0f, 0.5f * length);
            weldDef.localAnchorB = new b2Vec2(0.0f, -0.5f * length);
            b2BodyId previous = donut.bodyIds[DONUT_SIDES - 1];
            for (int i = 0; i < DONUT_SIDES; ++i) {
                weldDef.bodyIdA = previous;
                weldDef.bodyIdB = donut.bodyIds[i];
                weldDef.referenceAngle = b2RelativeAngle(b2Body_GetRotation(weldDef.bodyIdB),
                    b2Body_GetRotation(weldDef.bodyIdA));
                donut.jointIds[i] = b2CreateWeldJoint(worldId, weldDef);
                previous = weldDef.bodyIdB;
            }
            return donut;
        }

        void destroy() {
            for (b2BodyId bodyId : bodyIds) {
                if (b2Body_IsValid(bodyId)) {
                    b2DestroyBody(bodyId);
                }
            }
        }
    }

    private static final class Element {
        final int index;

        Element(int index) {
            this.index = index;
        }
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int awakeBodyCount;
        public final int type;
        public final int createdTotal;
        public final int destroyedTotal;
        public final int activeCount;
        public final long activeMask;
        public final int beginTotal;
        public final int endTotal;
        public final int lastBeginCount;
        public final float wait;
        public final float side;
        public final BodyState[] bodies;

        Result(int bodyCount, int shapeCount, int contactCount, int jointCount, int awakeBodyCount, int type,
               int createdTotal, int destroyedTotal, int activeCount, long activeMask, int beginTotal, int endTotal,
               int lastBeginCount, float wait, float side, BodyState[] bodies) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.awakeBodyCount = awakeBodyCount;
            this.type = type;
            this.createdTotal = createdTotal;
            this.destroyedTotal = destroyedTotal;
            this.activeCount = activeCount;
            this.activeMask = activeMask;
            this.beginTotal = beginTotal;
            this.endTotal = endTotal;
            this.lastBeginCount = lastBeginCount;
            this.wait = wait;
            this.side = side;
            this.bodies = bodies;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("sensorFunnel ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(jointCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(type).append(' ')
                .append(createdTotal).append(' ')
                .append(destroyedTotal).append(' ')
                .append(activeCount).append(' ')
                .append(activeMask).append(' ')
                .append(beginTotal).append(' ')
                .append(endTotal).append(' ')
                .append(lastBeginCount).append(' ')
                .append(formatFloat(wait)).append(' ')
                .append(formatFloat(side)).append(' ')
                .append(bodies.length);
            for (BodyState body : bodies) {
                builder.append(body.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class BodyState {
        public final int elementIndex;
        public final int boneIndex;
        public final float x;
        public final float y;
        public final float cos;
        public final float sin;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final int shapeCount;
        public final int contactCapacity;

        BodyState(int elementIndex, int boneIndex, float x, float y, float cos, float sin, float velocityX,
                  float velocityY, float angularVelocity, int shapeCount, int contactCapacity) {
            this.elementIndex = elementIndex;
            this.boneIndex = boneIndex;
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
            return String.format(Locale.ROOT, " %d %d %s %s %s %s %s %s %s %d %d", elementIndex, boneIndex,
                formatFloat(x), formatFloat(y), formatFloat(cos), formatFloat(sin), formatFloat(velocityX),
                formatFloat(velocityY), formatFloat(angularVelocity), shapeCount, contactCapacity);
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
