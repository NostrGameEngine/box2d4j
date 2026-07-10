package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2Circle;
import org.box2d4j.b2DistanceInput;
import org.box2d4j.b2DistanceOutput;
import org.box2d4j.b2ExplosionDef;
import org.box2d4j.b2Hull;
import org.box2d4j.b2JointId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2QueryFilter;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2Rot;
import org.box2d4j.b2Segment;
import org.box2d4j.b2SensorBeginTouchEvent;
import org.box2d4j.b2SensorEndTouchEvent;
import org.box2d4j.b2SensorEvents;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2ShapeProxy;
import org.box2d4j.b2SimplexCache;
import org.box2d4j.b2SurfaceMaterial;
import org.box2d4j.b2Transform;
import org.box2d4j.b2TreeStats;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WeldJointDef;
import org.box2d4j.b2WheelJointDef;
import org.box2d4j.b2WorldId;

import java.util.ArrayList;
import java.util.TreeMap;

import static org.box2d4j.B2.*;

final class BenchmarkScenes {
    private BenchmarkScenes() {
    }

    static final class Scene {
        final b2WorldId worldId;
        final ArrayList<b2BodyId> bodies = new ArrayList<>();

        Scene() {
            worldId = b2CreateWorld(b2DefaultWorldDef());
        }

        b2BodyId createBody(b2BodyDef bodyDef) {
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            trackBody(bodyId);
            return bodyId;
        }

        void trackBody(b2BodyId bodyId) {
            int bodyIndex = bodyId.index1 - 1;
            while (bodies.size() <= bodyIndex) {
                bodies.add(null);
            }
            bodies.set(bodyIndex, bodyId);
        }
    }

    static Scene createBarrel() {
        Scene scene = new Scene();
        b2BodyId groundId = scene.createBody(b2DefaultBodyDef());
        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        float x = -40.0f;
        for (int i = 0; i < 81; ++i) {
            b2CreatePolygonShape(groundId, groundShapeDef,
                b2MakeOffsetBox(0.5f, 0.5f, new b2Vec2(x, 0.0f), b2Rot_identity));
            x += 1.0f;
        }
        float y = 1.0f;
        for (int i = 0; i < 100; ++i) {
            b2CreatePolygonShape(groundId, groundShapeDef,
                b2MakeOffsetBox(0.5f, 0.5f, new b2Vec2(-40.0f, y), b2Rot_identity));
            y += 1.0f;
        }
        y = 1.0f;
        for (int i = 0; i < 100; ++i) {
            b2CreatePolygonShape(groundId, groundShapeDef,
                b2MakeOffsetBox(0.5f, 0.5f, new b2Vec2(40.0f, y), b2Rot_identity));
            y += 1.0f;
        }
        b2CreateSegmentShape(groundId, groundShapeDef,
            new b2Segment(new b2Vec2(-800.0f, -80.0f), new b2Vec2(800.0f, -80.0f)));

        b2Vec2[] vertices = {
            new b2Vec2(-1.0f, 0.0f), new b2Vec2(0.5f, 1.0f), new b2Vec2(0.0f, 2.0f)
        };
        b2Hull hull = b2ComputeHull(vertices, vertices.length);
        b2Polygon left = b2MakePolygon(hull, 0.0f);
        vertices = new b2Vec2[] {
            new b2Vec2(1.0f, 0.0f), new b2Vec2(-0.5f, 1.0f), new b2Vec2(0.0f, 2.0f)
        };
        hull = b2ComputeHull(vertices, vertices.length);
        b2Polygon right = b2MakePolygon(hull, 0.0f);

        int columnCount = 20;
        int rowCount = 150;
        float shift = 2.0f;
        float centerX = shift * columnCount / 2.0f - 1.0f;
        float centerY = 1.15f / 2.0f;
        float side = 0.25f;
        float extraY = 0.25f;
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.5f;
        for (int i = 0; i < columnCount; ++i) {
            x = i * shift - centerX;
            for (int j = 0; j < rowCount; ++j) {
                y = j * (shift + extraY) + centerY + 100.0f;
                bodyDef.position = new b2Vec2(x + side, y);
                side = -side;
                b2BodyId bodyId = scene.createBody(bodyDef);
                b2CreatePolygonShape(bodyId, shapeDef, left);
                b2CreatePolygonShape(bodyId, shapeDef, right);
            }
        }
        return scene;
    }

    static Scene createTumbler() {
        Scene scene = new Scene();
        b2BodyId groundId = scene.createBody(b2DefaultBodyDef());
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 10.0f);
        b2BodyId tumblerId = scene.createBody(bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 50.0f;
        b2CreatePolygonShape(tumblerId, shapeDef,
            b2MakeOffsetBox(0.5f, 10.0f, new b2Vec2(10.0f, 0.0f), b2Rot_identity));
        b2CreatePolygonShape(tumblerId, shapeDef,
            b2MakeOffsetBox(0.5f, 10.0f, new b2Vec2(-10.0f, 0.0f), b2Rot_identity));
        b2CreatePolygonShape(tumblerId, shapeDef,
            b2MakeOffsetBox(10.0f, 0.5f, new b2Vec2(0.0f, 10.0f), b2Rot_identity));
        b2CreatePolygonShape(tumblerId, shapeDef,
            b2MakeOffsetBox(10.0f, 0.5f, new b2Vec2(0.0f, -10.0f), b2Rot_identity));

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = groundId;
        jointDef.bodyIdB = tumblerId;
        jointDef.localAnchorA = new b2Vec2(0.0f, 10.0f);
        jointDef.motorSpeed = (B2_PI / 180.0f) * 25.0f;
        jointDef.maxMotorTorque = 1.0e8f;
        jointDef.enableMotor = true;
        b2CreateRevoluteJoint(scene.worldId, jointDef);

        b2Polygon box = b2MakeBox(0.125f, 0.125f);
        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        shapeDef = b2DefaultShapeDef();
        int gridCount = 20;
        float y = -0.2f * gridCount + 10.0f;
        for (int i = 0; i < gridCount; ++i) {
            float x = -0.2f * gridCount;
            for (int j = 0; j < gridCount; ++j) {
                bodyDef.position = new b2Vec2(x, y);
                b2BodyId bodyId = scene.createBody(bodyDef);
                b2CreatePolygonShape(bodyId, shapeDef, box);
                x += 0.4f;
            }
            y += 0.4f;
        }
        return scene;
    }

    static Scene createLargePyramid() {
        Scene scene = new Scene();
        b2World_EnableSleeping(scene.worldId, false);
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(0.0f, -1.0f);
        b2BodyId groundId = scene.createBody(bodyDef);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(100.0f, 1.0f));

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        float h = 0.5f;
        b2Polygon box = b2MakeSquare(h);
        float shift = h;
        int baseCount = 20;
        for (int i = 0; i < baseCount; ++i) {
            float y = (2.0f * i + 1.0f) * shift;
            for (int j = i; j < baseCount; ++j) {
                float x = (i + 1.0f) * shift + 2.0f * (j - i) * shift - h * baseCount;
                bodyDef.position = new b2Vec2(x, y);
                b2BodyId bodyId = scene.createBody(bodyDef);
                b2CreatePolygonShape(bodyId, shapeDef, box);
            }
        }
        return scene;
    }

    static Scene createManyPyramids() {
        Scene scene = new Scene();
        b2World_EnableSleeping(scene.worldId, false);
        int baseCount = 10;
        float extent = 0.5f;
        int rowCount = 5;
        int columnCount = 5;
        b2BodyId groundId = scene.createBody(b2DefaultBodyDef());
        float groundDeltaY = 2.0f * extent * (baseCount + 1.0f);
        float groundWidth = 2.0f * extent * columnCount * (baseCount + 1.0f);
        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        float groundY = 0.0f;
        for (int i = 0; i < rowCount; ++i) {
            b2CreateSegmentShape(groundId, groundShapeDef,
                new b2Segment(new b2Vec2(-groundWidth, groundY), new b2Vec2(groundWidth, groundY)));
            groundY += groundDeltaY;
        }

        float baseWidth = 2.0f * extent * baseCount;
        float baseY = 0.0f;
        for (int i = 0; i < rowCount; ++i) {
            for (int j = 0; j < columnCount; ++j) {
                float centerX = -0.5f * groundWidth + j * (baseWidth + 2.0f * extent) + extent;
                createSmallPyramid(scene, baseCount, extent, centerX, baseY);
            }
            baseY += groundDeltaY;
        }
        return scene;
    }

    private static void createSmallPyramid(Scene scene, int baseCount, float extent, float centerX, float baseY) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon box = b2MakeSquare(extent);
        for (int i = 0; i < baseCount; ++i) {
            float y = (2.0f * i + 1.0f) * extent + baseY;
            for (int j = i; j < baseCount; ++j) {
                float x = (i + 1.0f) * extent + 2.0f * (j - i) * extent + centerX - 0.5f;
                bodyDef.position = new b2Vec2(x, y);
                b2BodyId bodyId = scene.createBody(bodyDef);
                b2CreatePolygonShape(bodyId, shapeDef, box);
            }
        }
    }

    static Scene createJointGrid() {
        Scene scene = new Scene();
        b2World_EnableSleeping(scene.worldId, false);
        int count = 10;
        b2BodyId[] bodies = new b2BodyId[count * count];
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.filter.categoryBits = 2;
        shapeDef.filter.maskBits = 0xFFFFFFFDL;
        b2Circle circle = new b2Circle(new b2Vec2(), 0.4f);
        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        b2BodyDef bodyDef = b2DefaultBodyDef();
        int index = 0;
        for (int column = 0; column < count; ++column) {
            for (int row = 0; row < count; ++row) {
                bodyDef.type = column >= count / 2 - 3 && column <= count / 2 + 3 && row == 0
                    ? b2_staticBody : b2_dynamicBody;
                bodyDef.position = new b2Vec2((float) column, -(float) row);
                b2BodyId bodyId = scene.createBody(bodyDef);
                b2CreateCircleShape(bodyId, shapeDef, circle);
                if (row > 0) {
                    jointDef.bodyIdA = bodies[index - 1];
                    jointDef.bodyIdB = bodyId;
                    jointDef.localAnchorA = new b2Vec2(0.0f, -0.5f);
                    jointDef.localAnchorB = new b2Vec2(0.0f, 0.5f);
                    b2CreateRevoluteJoint(scene.worldId, jointDef);
                }
                if (column > 0) {
                    jointDef.bodyIdA = bodies[index - count];
                    jointDef.bodyIdB = bodyId;
                    jointDef.localAnchorA = new b2Vec2(0.5f, 0.0f);
                    jointDef.localAnchorB = new b2Vec2(-0.5f, 0.0f);
                    b2CreateRevoluteJoint(scene.worldId, jointDef);
                }
                bodies[index++] = bodyId;
            }
        }
        return scene;
    }

    static Scene createSmash() {
        Scene scene = new Scene();
        b2World_SetGravity(scene.worldId, b2Vec2_zero);
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(-20.0f, 0.0f);
        bodyDef.linearVelocity = new b2Vec2(40.0f, 0.0f);
        b2BodyId projectileId = scene.createBody(bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 8.0f;
        b2CreatePolygonShape(projectileId, shapeDef, b2MakeBox(4.0f, 4.0f));

        float spacing = 0.4f;
        b2Polygon box = b2MakeSquare(0.5f * spacing);
        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.isAwake = false;
        shapeDef = b2DefaultShapeDef();
        int columns = 20;
        int rows = 10;
        for (int i = 0; i < columns; ++i) {
            for (int j = 0; j < rows; ++j) {
                bodyDef.position = new b2Vec2(i * spacing + 30.0f, (j - rows / 2.0f) * spacing);
                b2BodyId bodyId = scene.createBody(bodyDef);
                b2CreatePolygonShape(bodyId, shapeDef, box);
            }
        }
        return scene;
    }

    static Scene createSpinner() {
        Scene scene = new Scene();
        b2BodyId groundId = scene.createBody(b2DefaultBodyDef());
        int pointCount = 360;
        b2Vec2[] points = new b2Vec2[pointCount];
        b2Rot rotation = b2MakeRot(-2.0f * B2_PI / pointCount);
        b2Vec2 point = new b2Vec2(40.0f, 0.0f);
        for (int i = 0; i < pointCount; ++i) {
            points[i] = new b2Vec2(point.x, point.y + 32.0f);
            point = b2RotateVector(rotation, point);
        }
        b2SurfaceMaterial material = new b2SurfaceMaterial();
        material.friction = 0.1f;
        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = points;
        chainDef.count = pointCount;
        chainDef.isLoop = true;
        chainDef.materials = new b2SurfaceMaterial[] {material};
        chainDef.materialCount = 1;
        b2CreateChain(groundId, chainDef);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 12.0f);
        bodyDef.enableSleep = false;
        b2BodyId spinnerId = scene.createBody(bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.0f;
        b2CreatePolygonShape(spinnerId, shapeDef, b2MakeRoundedBox(0.4f, 20.0f, 0.2f));
        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = groundId;
        jointDef.bodyIdB = spinnerId;
        jointDef.localAnchorA = bodyDef.position.copy();
        jointDef.enableMotor = true;
        jointDef.motorSpeed = 5.0f;
        jointDef.maxMotorTorque = 40000.0f;
        b2JointId spinnerJointId = b2CreateRevoluteJoint(scene.worldId, jointDef);
        SampleRuntime.slider("spinner.motorSpeed", "Motor speed", jointDef.motorSpeed,
            -10.0f, 10.0f, 0.5f, value -> b2RevoluteJoint_SetMotorSpeed(spinnerJointId, value));

        b2Capsule capsule = new b2Capsule(new b2Vec2(-0.25f, 0.0f), new b2Vec2(0.25f, 0.0f), 0.25f);
        b2Circle circle = new b2Circle(new b2Vec2(), 0.35f);
        b2Polygon square = b2MakeSquare(0.35f);
        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.1f;
        shapeDef.material.restitution = 0.1f;
        shapeDef.density = 0.25f;
        float x = -24.0f;
        float y = 2.0f;
        for (int i = 0; i < 499; ++i) {
            bodyDef.position = new b2Vec2(x, y);
            b2BodyId bodyId = scene.createBody(bodyDef);
            int remainder = i % 3;
            if (remainder == 0) {
                b2CreateCapsuleShape(bodyId, shapeDef, capsule);
            } else if (remainder == 1) {
                b2CreateCircleShape(bodyId, shapeDef, circle);
            } else {
                b2CreatePolygonShape(bodyId, shapeDef, square);
            }
            x += 1.0f;
            if (x > 24.0f) {
                x = -24.0f;
                y += 1.0f;
            }
        }
        return scene;
    }

    static ManyTumblersScene createManyTumblers() {
        return new ManyTumblersScene();
    }

    static final class ManyTumblersScene {
        final Scene scene = new Scene();
        final ArrayList<b2Vec2> positions = new ArrayList<>();
        final ArrayList<b2BodyId> tumblerIds = new ArrayList<>();
        final b2BodyId groundId;
        int rowCount = 2;
        int columnCount = 2;
        int bodiesPerTumbler = 8;
        int bodyIndex;
        int bodyLimit;
        float angularSpeed = 25.0f;

        ManyTumblersScene() {
            groundId = scene.createBody(b2DefaultBodyDef());
            createScene();
        }

        void createScene() {
            for (b2BodyId bodyId : scene.bodies) {
                if (bodyId != null && !B2_ID_EQUALS(bodyId, groundId) && b2Body_IsValid(bodyId)) {
                    b2DestroyBody(bodyId);
                }
            }
            positions.clear();
            tumblerIds.clear();
            int index = 0;
            float x = -4.0f * rowCount;
            for (int i = 0; i < rowCount; ++i) {
                float y = -4.0f * columnCount;
                for (int j = 0; j < columnCount; ++j) {
                    b2Vec2 position = new b2Vec2(x, y);
                    positions.add(position);
                    createTumbler(position);
                    index += 1;
                    y += 8.0f;
                }
                x += 8.0f;
            }
            bodyIndex = 0;
            bodyLimit = bodiesPerTumbler * positions.size();
        }

        private void createTumbler(b2Vec2 position) {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_kinematicBody;
            bodyDef.position = position.copy();
            bodyDef.angularVelocity = (B2_PI / 180.0f) * angularSpeed;
            b2BodyId bodyId = scene.createBody(bodyDef);
            tumblerIds.add(bodyId);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 50.0f;
            b2CreatePolygonShape(bodyId, shapeDef,
                b2MakeOffsetBox(0.25f, 2.0f, new b2Vec2(2.0f, 0.0f), b2Rot_identity));
            b2CreatePolygonShape(bodyId, shapeDef,
                b2MakeOffsetBox(0.25f, 2.0f, new b2Vec2(-2.0f, 0.0f), b2Rot_identity));
            b2CreatePolygonShape(bodyId, shapeDef,
                b2MakeOffsetBox(2.0f, 0.25f, new b2Vec2(0.0f, 2.0f), b2Rot_identity));
            b2CreatePolygonShape(bodyId, shapeDef,
                b2MakeOffsetBox(2.0f, 0.25f, new b2Vec2(0.0f, -2.0f), b2Rot_identity));
        }

        void afterStep(int stepCount) {
            if (bodyIndex >= bodyLimit || (stepCount & 0x7) != 0) {
                return;
            }
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2Capsule capsule = new b2Capsule(new b2Vec2(-0.1f, 0.0f), new b2Vec2(0.1f, 0.0f), 0.075f);
            for (b2Vec2 position : positions) {
                b2BodyDef bodyDef = b2DefaultBodyDef();
                bodyDef.type = b2_dynamicBody;
                bodyDef.position = position.copy();
                b2BodyId bodyId = scene.createBody(bodyDef);
                b2CreateCapsuleShape(bodyId, shapeDef, capsule);
                bodyIndex += 1;
            }
        }

        void setRowCount(int value) {
            rowCount = value;
            createScene();
        }

        void setColumnCount(int value) {
            columnCount = value;
            createScene();
        }

        void setAngularSpeed(float value) {
            angularSpeed = value;
            for (b2BodyId tumblerId : tumblerIds) {
                b2Body_SetAngularVelocity(tumblerId, (B2_PI / 180.0f) * value);
                b2Body_SetAwake(tumblerId, true);
            }
        }
    }

    static CreateDestroyScene createCreateDestroy() {
        return new CreateDestroyScene();
    }

    static final class CreateDestroyScene {
        final Scene scene = new Scene();
        final b2BodyId[] bodies = new b2BodyId[5050];
        int bodyCount;

        CreateDestroyScene() {
            b2BodyId groundId = scene.createBody(b2DefaultBodyDef());
            b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(100.0f, 1.0f));
        }

        void beforeStep(int step) {
            for (int i = 0; i < bodyCount; ++i) {
                b2DestroyBody(bodies[i]);
            }

            int count = 40;
            float shift = 1.0f;
            float centerX = shift * count / 2.0f;
            float centerY = shift / 2.0f + 1.0f;
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            shapeDef.material.friction = 0.5f;
            b2Polygon box = b2MakeRoundedBox(0.5f, 0.5f, 0.0f);
            int index = 0;
            for (int i = 0; i < count; ++i) {
                float y = i * shift + centerY;
                for (int j = i; j < count; ++j) {
                    float x = 0.5f * i * shift + (j - i) * shift - centerX;
                    bodyDef.position = new b2Vec2(x, y);
                    bodies[index] = scene.createBody(bodyDef);
                    b2CreatePolygonShape(bodies[index], shapeDef, box);
                    index += 1;
                }
            }
            bodyCount = index;
            b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
        }
    }

    static SleepScene createSleep() {
        return new SleepScene();
    }

    static final class SleepScene {
        final Scene scene = new Scene();
        final b2BodyId[] bodies = new b2BodyId[5050];
        boolean awake;

        SleepScene() {
            b2BodyId groundId = scene.createBody(b2DefaultBodyDef());
            b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(100.0f, 1.0f));
            int count = 40;
            float shift = 1.0f;
            float centerX = shift * count / 2.0f;
            float centerY = shift / 2.0f + 1.0f;
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            shapeDef.material.friction = 0.5f;
            b2Polygon box = b2MakeRoundedBox(0.5f, 0.5f, 0.0f);
            int index = 0;
            for (int i = 0; i < count; ++i) {
                float y = i * shift + centerY;
                for (int j = i; j < count; ++j) {
                    float x = 0.5f * i * shift + (j - i) * shift - centerX;
                    bodyDef.position = new b2Vec2(x, y);
                    bodies[index] = scene.createBody(bodyDef);
                    b2CreatePolygonShape(bodies[index], shapeDef, box);
                    index += 1;
                }
            }
        }

        void beforeStep(int step) {
            b2Body_SetAwake(bodies[0], awake);
            awake = !awake;
        }
    }

    static Scene createCompound() {
        Scene scene = new Scene();
        float grid = 1.0f;
        int height = 100;
        int width = 100;
        b2BodyId groundId = scene.createBody(b2DefaultBodyDef());
        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        for (int i = 0; i < height; ++i) {
            float y = grid * i;
            for (int j = i; j < width; ++j) {
                float x = grid * j;
                b2CreatePolygonShape(groundId, groundShapeDef,
                    b2MakeOffsetBox(0.5f * grid, 0.5f * grid, new b2Vec2(x, y), b2Rot_identity));
            }
        }
        for (int i = 0; i < height; ++i) {
            float y = grid * i;
            for (int j = i; j < width; ++j) {
                float x = -grid * j;
                b2CreatePolygonShape(groundId, groundShapeDef,
                    b2MakeOffsetBox(0.5f * grid, 0.5f * grid, new b2Vec2(x, y), b2Rot_identity));
            }
        }

        int span = 5;
        int count = 5;
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.updateBodyMass = false;
        for (int m = 0; m < count; ++m) {
            float bodyY = (100.0f + m * span) * grid;
            for (int n = 0; n < count; ++n) {
                float bodyX = -0.5f * grid * count * span + n * span * grid;
                bodyDef.position = new b2Vec2(bodyX, bodyY);
                b2BodyId bodyId = scene.createBody(bodyDef);
                for (int i = 0; i < span; ++i) {
                    float y = i * grid;
                    for (int j = 0; j < span; ++j) {
                        float x = j * grid;
                        b2CreatePolygonShape(bodyId, shapeDef,
                            b2MakeOffsetBox(0.5f * grid, 0.5f * grid, new b2Vec2(x, y), b2Rot_identity));
                    }
                }
                b2Body_ApplyMassFromShapes(bodyId);
            }
        }
        return scene;
    }

    static Scene createKinematic() {
        Scene scene = new Scene();
        float grid = 1.0f;
        int span = 20;
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_kinematicBody;
        bodyDef.angularVelocity = 1.0f;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.filter.categoryBits = 1;
        shapeDef.filter.maskBits = 2;
        shapeDef.updateBodyMass = false;
        b2BodyId bodyId = scene.createBody(bodyDef);
        for (int i = -span; i < span; ++i) {
            float y = i * grid;
            for (int j = -span; j < span; ++j) {
                float x = j * grid;
                b2CreatePolygonShape(bodyId, shapeDef,
                    b2MakeOffsetBox(0.5f * grid, 0.5f * grid, new b2Vec2(x, y), b2Rot_identity));
            }
        }
        b2Body_ApplyMassFromShapes(bodyId);
        return scene;
    }

    static CastScene createCast() {
        return new CastScene();
    }

    static final class CastScene {
        final Scene scene = new Scene();
        final b2Vec2[] origins = new b2Vec2[100];
        final b2Vec2[] translations = new b2Vec2[100];
        final BenchmarkHash hash = new BenchmarkHash();
        int queryType = 1;
        int rowCount = 100;
        int columnCount = 100;
        int drawIndex;
        float radius = 0.1f;
        float fill = 0.1f;
        float ratio = 5.0f;
        float grid = 1.0f;
        boolean topDown;

        CastScene() {
            RandomState random = new RandomState(1234);
            float extent = rowCount * grid;
            for (int i = 0; i < origins.length; ++i) {
                b2Vec2 start = random.vector(0.0f, extent);
                b2Vec2 end = random.vector(0.0f, extent);
                origins[i] = start;
                translations[i] = b2Sub(end, start);
            }

            buildScene();
        }

        void buildScene() {
            for (b2BodyId bodyId : scene.bodies) {
                if (bodyId != null && b2Body_IsValid(bodyId)) {
                    b2DestroyBody(bodyId);
                }
            }
            RandomState random = new RandomState(1234);
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            float y = 0.0f;
            for (int i = 0; i < rowCount; ++i) {
                float x = 0.0f;
                for (int j = 0; j < columnCount; ++j) {
                    if (random.range(0.0f, 1.0f) <= fill) {
                        bodyDef.position = new b2Vec2(x, y);
                        b2BodyId bodyId = scene.createBody(bodyDef);
                        float shapeRatio = random.range(1.0f, ratio);
                        float halfWidth = random.range(0.05f, 0.25f);
                        b2Polygon box = random.signed() > 0.0f
                            ? b2MakeBox(shapeRatio * halfWidth, halfWidth)
                            : b2MakeBox(halfWidth, shapeRatio * halfWidth);
                        int category = random.intRange(0, 2);
                        shapeDef.filter.categoryBits = 1L << category;
                        shapeDef.material.customColor = category == 0 ? b2_colorBox2DBlue
                            : category == 1 ? b2_colorBox2DYellow : b2_colorBox2DGreen;
                        b2CreatePolygonShape(bodyId, shapeDef, box);
                    }
                    x += grid;
                }
                y += grid;
            }
            if (topDown) {
                b2World_RebuildStaticTree(scene.worldId);
            }
        }

        void afterStep(int step) {
            b2QueryFilter filter = b2DefaultQueryFilter();
            filter.maskBits = 1;
            for (int i = 0; i < origins.length; ++i) {
                if (queryType == 0) {
                    org.box2d4j.b2RayResult result = b2World_CastRayClosest(scene.worldId, origins[i],
                        translations[i], filter);
                    hash.mix(result.hit ? 1 : 0);
                    hash.mix(result.nodeVisits);
                    hash.mix(result.leafVisits);
                    if (result.hit) {
                        hash.mix(Float.floatToRawIntBits(result.point.x));
                        hash.mix(Float.floatToRawIntBits(result.point.y));
                        hash.mix(Float.floatToRawIntBits(result.fraction));
                    }
                    continue;
                }
                if (queryType == 2) {
                    b2Vec2 extent = new b2Vec2(radius, radius);
                    int[] count = {0};
                    b2TreeStats stats = b2World_OverlapAABB(scene.worldId,
                        new org.box2d4j.b2AABB(b2Sub(origins[i], extent), b2Add(origins[i], extent)), filter,
                        shapeId -> {
                            count[0] += 1;
                            return true;
                        });
                    hash.mix(count[0]);
                    hash.mix(stats.nodeVisits);
                    hash.mix(stats.leafVisits);
                    continue;
                }
                b2ShapeProxy proxy = b2MakeProxy(new b2Vec2[] {origins[i]}, 1, 0.1f);
                CastResult result = new CastResult();
                b2TreeStats stats = b2World_CastShape(scene.worldId, proxy, translations[i], filter,
                    (shapeId, point, normal, fraction) -> {
                        result.point = point.copy();
                        result.fraction = fraction;
                        result.hit = true;
                        return fraction;
                    });
                hash.mix(result.hit ? 1 : 0);
                hash.mix(stats.nodeVisits);
                hash.mix(stats.leafVisits);
                if (result.hit) {
                    hash.mix(Float.floatToRawIntBits(result.point.x));
                    hash.mix(Float.floatToRawIntBits(result.point.y));
                    hash.mix(Float.floatToRawIntBits(result.fraction));
                }
            }
        }
    }

    private static final class CastResult {
        b2Vec2 point = new b2Vec2();
        float fraction;
        boolean hit;
    }

    static ShapeDistanceScene createShapeDistance() {
        return new ShapeDistanceScene();
    }

    static final class ShapeDistanceScene {
        final Scene scene = new Scene();
        final b2Polygon polygonA;
        final b2Polygon polygonB;
        final b2Transform[] transformsA = new b2Transform[100];
        final b2Transform[] transformsB = new b2Transform[100];
        final BenchmarkHash hash = new BenchmarkHash();

        ShapeDistanceScene() {
            polygonA = makeOctagon(0.0f);
            polygonB = makeOctagon(0.1f);
            RandomState random = new RandomState(42);
            for (int i = 0; i < transformsA.length; ++i) {
                transformsA[i] = new b2Transform(random.vector(-0.1f, 0.1f), random.rotation());
                transformsB[i] = new b2Transform(random.vector(0.25f, 2.0f), random.rotation());
            }
        }

        void beforeStep(int step) {
            b2DistanceInput input = new b2DistanceInput();
            input.proxyA = b2MakeProxy(polygonA.vertices, polygonA.count, polygonA.radius);
            input.proxyB = b2MakeProxy(polygonB.vertices, polygonB.count, polygonB.radius);
            input.useRadii = true;
            for (int i = 0; i < transformsA.length; ++i) {
                input.transformA = transformsA[i];
                input.transformB = transformsB[i];
                b2DistanceOutput output = b2ShapeDistance(input, new b2SimplexCache(), null, 0);
                hash.mix(Float.floatToRawIntBits(output.pointA.x));
                hash.mix(Float.floatToRawIntBits(output.pointA.y));
                hash.mix(Float.floatToRawIntBits(output.pointB.x));
                hash.mix(Float.floatToRawIntBits(output.pointB.y));
                hash.mix(Float.floatToRawIntBits(output.normal.x));
                hash.mix(Float.floatToRawIntBits(output.normal.y));
                hash.mix(Float.floatToRawIntBits(output.distance));
                hash.mix(output.iterations);
                hash.mix(output.simplexCount);
            }
        }

        private static b2Polygon makeOctagon(float radius) {
            b2Vec2[] points = b2Vec2.array(8);
            b2Rot rotation = b2MakeRot(2.0f * B2_PI / 8.0f);
            points[0] = new b2Vec2(0.5f, 0.0f);
            for (int i = 1; i < points.length; ++i) {
                points[i] = b2RotateVector(rotation, points[i - 1]);
            }
            return b2MakePolygon(b2ComputeHull(points, points.length), radius);
        }
    }

    static RainScene createRain() {
        return new RainScene();
    }

    static final class RainScene {
        final Scene scene = new Scene();
        final Ragdoll.Human[][] groups = new Ragdoll.Human[30][2];
        int columnCount;
        int columnIndex;

        RainScene() {
            b2BodyId groundId = scene.createBody(b2DefaultBodyDef());
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            float y = 0.0f;
            for (int i = 0; i < 3; ++i) {
                float x = -50.0f;
                for (int j = 0; j <= 200; ++j) {
                    b2CreatePolygonShape(groundId, shapeDef,
                        b2MakeOffsetBox(0.25f, 0.25f, new b2Vec2(x, y), b2Rot_identity));
                    x += 0.5f;
                }
                y += 45.0f;
            }
        }

        void beforeStep(int stepCount) {
            if ((stepCount & 0x1F) != 0) {
                return;
            }
            if (columnCount < 10) {
                for (int row = 0; row < 3; ++row) {
                    createGroup(row, columnCount);
                }
                columnCount += 1;
            } else {
                for (int row = 0; row < 3; ++row) {
                    destroyGroup(row, columnIndex);
                    createGroup(row, columnIndex);
                }
                columnIndex = (columnIndex + 1) % 10;
            }
        }

        private void createGroup(int row, int column) {
            int groupIndex = row * 10 + column;
            b2Vec2 position = new b2Vec2(-50.0f + 10.0f * (column + 0.5f), 40.0f + 45.0f * row);
            for (int i = 0; i < 2; ++i) {
                Ragdoll.Human human = Ragdoll.Human.create(scene.worldId, position, 1.0f, 0.05f, 5.0f, 0.5f, i + 1);
                groups[groupIndex][i] = human;
                for (b2BodyId bodyId : human.bodies) {
                    scene.trackBody(bodyId);
                }
                position = new b2Vec2(position.x + 0.5f, position.y);
            }
        }

        private void destroyGroup(int row, int column) {
            int groupIndex = row * 10 + column;
            for (int i = 0; i < 2; ++i) {
                Ragdoll.Human human = groups[groupIndex][i];
                if (human != null) {
                    for (b2BodyId bodyId : human.bodies) {
                        b2DestroyBody(bodyId);
                    }
                    groups[groupIndex][i] = null;
                }
            }
        }
    }

    static SensorScene createSensor() {
        return new SensorScene();
    }

    static final class SensorScene {
        final Scene scene = new Scene();
        final ShapeUserData passiveSensor = new ShapeUserData(false);
        final ShapeUserData activeSensor = new ShapeUserData(true);
        final BenchmarkHash hash = new BenchmarkHash();
        int lastStepCount;

        SensorScene() {
            b2BodyId groundId = scene.createBody(b2DefaultBodyDef());
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.isSensor = true;
            shapeDef.enableSensorEvents = true;
            shapeDef.userData = activeSensor;
            float x = -120.0f;
            for (int i = 0; i < 81; ++i) {
                b2CreatePolygonShape(groundId, shapeDef,
                    b2MakeOffsetBox(1.5f, 1.5f, new b2Vec2(x, 0.0f), b2Rot_identity));
                x += 3.0f;
            }

            RandomState random = new RandomState(42);
            shapeDef = b2DefaultShapeDef();
            shapeDef.isSensor = true;
            shapeDef.enableSensorEvents = true;
            shapeDef.userData = passiveSensor;
            float shift = 5.0f;
            float centerX = 0.5f * shift * 40;
            for (int row = 0; row < 40; ++row) {
                float y = row * shift + 10.0f;
                for (int column = 0; column < 40; ++column) {
                    x = column * shift - centerX;
                    float yOffset = random.range(-1.0f, 1.0f);
                    b2CreatePolygonShape(groundId, shapeDef,
                        b2MakeOffsetRoundedBox(0.5f, 0.5f, new b2Vec2(x, y + yOffset), random.rotation(), 0.1f));
                }
            }
        }

        void afterStep(int stepCount) {
            if (stepCount == lastStepCount) {
                return;
            }
            b2SensorEvents events = b2World_GetSensorEvents(scene.worldId);
            hash.mix(stepCount);
            hash.mix(events.beginCount);
            hash.mix(events.endCount);
            TreeMap<Long, b2BodyId> zombies = new TreeMap<>(Long::compareUnsigned);
            for (b2SensorBeginTouchEvent event : events.beginEvents) {
                hash.mix(b2StoreShapeId(event.sensorShapeId));
                hash.mix(b2StoreShapeId(event.visitorShapeId));
                ShapeUserData userData = (ShapeUserData) b2Shape_GetUserData(event.sensorShapeId);
                if (userData.shouldDestroyVisitors) {
                    b2BodyId bodyId = b2Shape_GetBody(event.visitorShapeId);
                    zombies.put(b2StoreBodyId(bodyId), bodyId);
                } else {
                    b2SurfaceMaterial material = b2Shape_GetSurfaceMaterial(event.visitorShapeId);
                    material.customColor = b2_colorLime;
                    b2Shape_SetSurfaceMaterial(event.visitorShapeId, material);
                }
            }
            for (b2SensorEndTouchEvent event : events.endEvents) {
                hash.mix(b2StoreShapeId(event.sensorShapeId));
                hash.mix(b2StoreShapeId(event.visitorShapeId));
                if (b2Shape_IsValid(event.visitorShapeId)) {
                    b2SurfaceMaterial material = b2Shape_GetSurfaceMaterial(event.visitorShapeId);
                    material.customColor = 0;
                    b2Shape_SetSurfaceMaterial(event.visitorShapeId, material);
                }
            }
            for (b2BodyId bodyId : zombies.values()) {
                b2DestroyBody(bodyId);
            }
            if ((stepCount & 0x1F) == 0) {
                createRow(210.0f);
            }
            lastStepCount = stepCount;
        }

        private void createRow(float y) {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.gravityScale = 0.0f;
            bodyDef.linearVelocity = new b2Vec2(0.0f, -5.0f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.enableSensorEvents = true;
            b2Circle circle = new b2Circle(new b2Vec2(), 0.5f);
            for (int i = 0; i < 40; ++i) {
                bodyDef.position = new b2Vec2(5.0f * i - 100.0f, y);
                b2BodyId bodyId = scene.createBody(bodyDef);
                b2CreateCircleShape(bodyId, shapeDef, circle);
            }
        }
    }

    private static final class ShapeUserData {
        final boolean shouldDestroyVisitors;

        ShapeUserData(boolean shouldDestroyVisitors) {
            this.shouldDestroyVisitors = shouldDestroyVisitors;
        }
    }

    private static final class RandomState {
        private int seed;

        RandomState(int seed) {
            this.seed = seed;
        }

        private int nextInt() {
            int value = seed;
            value ^= value << 13;
            value ^= value >>> 17;
            value ^= value << 5;
            seed = value;
            return value & 32767;
        }

        int intRange(int lo, int hi) {
            return lo + nextInt() % (hi - lo + 1);
        }

        float signed() {
            return 2.0f * (nextInt() / 32767.0f) - 1.0f;
        }

        float range(float lo, float hi) {
            return (hi - lo) * (nextInt() / 32767.0f) + lo;
        }

        b2Vec2 vector(float lo, float hi) {
            return new b2Vec2(range(lo, hi), range(lo, hi));
        }

        b2Rot rotation() {
            return b2MakeRot(range(-B2_PI, B2_PI));
        }
    }

    static LargeWorldScene createLargeWorld() {
        return new LargeWorldScene();
    }

    static final class LargeWorldScene {
        final Scene scene = new Scene();
        final int cycleCount = 10;
        final float period = 40.0f;
        final float span = 0.5f * period * cycleCount;
        int cycleIndex;
        int runtimeStep;
        float speed;
        boolean explode = true;
        boolean followCar;
        boolean leftPressed;
        boolean brakePressed;
        boolean rightPressed;
        b2Vec2 viewPosition = new b2Vec2();
        b2BodyId carChassisId;
        b2JointId carRearAxleId;
        b2JointId carFrontAxleId;

        LargeWorldScene() {
            float omega = (float) (2.0 * B2_PI / period);
            int gridCount = (int) (cycleCount * period);
            float xStart = -0.5f * cycleCount * period;
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.invokeContactCreation = false;
            float xBody = xStart;
            float xShape = xStart;
            b2BodyId groundId = b2_nullBodyId;
            for (int i = 0; i < gridCount; ++i) {
                if (i % 10 == 0) {
                    bodyDef.position = new b2Vec2(xBody, 0.0f);
                    groundId = scene.createBody(bodyDef);
                    xShape = 0.0f;
                }
                int yCount = cRound(4.0f * (float) Math.cos(omega * xBody)) + 12;
                float y = 0.0f;
                for (int j = 0; j < yCount; ++j) {
                    b2Polygon square = b2MakeOffsetBox(0.4f, 0.4f, new b2Vec2(xShape, y), b2Rot_identity);
                    square.radius = 0.1f;
                    b2CreatePolygonShape(groundId, shapeDef, square);
                    y += 1.0f;
                }
                xBody += 1.0f;
                xShape += 1.0f;
            }

            int humanIndex = 0;
            for (int cycle = 0; cycle < cycleCount; ++cycle) {
                float baseX = (0.5f + cycle) * period + xStart;
                int remainder = cycle % 3;
                if (remainder == 0) {
                    createBoxGroup(baseX);
                } else if (remainder == 1) {
                    b2Vec2 position = new b2Vec2(baseX - 2.0f, 10.0f);
                    for (int i = 0; i < 5; ++i) {
                        Ragdoll.Human human = Ragdoll.Human.create(scene.worldId, position, 1.5f, 0.05f,
                            0.0f, 0.0f, humanIndex + 1);
                        humanIndex += 1;
                        for (b2BodyId bodyId : human.bodies) {
                            scene.trackBody(bodyId);
                        }
                        position = new b2Vec2(position.x + 1.0f, position.y);
                    }
                } else {
                    b2Vec2 position = new b2Vec2(baseX - 4.0f, 12.0f);
                    for (int i = 0; i < 5; ++i) {
                        createDonut(position, 0.75f);
                        position = new b2Vec2(position.x + 2.0f, position.y);
                    }
                }
            }
            createCar(new b2Vec2(xStart + 20.0f, 40.0f), 10.0f, 2.0f, 0.7f, 2000.0f);
        }

        void configureRuntime() {
            SampleRuntime.Binding speedBinding = SampleRuntime.slider("largeWorld.speed", "View Speed", speed,
                -400.0f, 400.0f, 10.0f, value -> speed = value);
            SampleRuntime.action("largeWorld.stop", "Stop View", () -> {
                speed = 0.0f;
                if (speedBinding != null) {
                    speedBinding.setFloatValue(0.0f);
                }
            });
            SampleRuntime.toggle("largeWorld.explode", "Explode", true, value -> explode = value);
            SampleRuntime.toggle("largeWorld.followCar", "Follow Car", false, value -> followCar = value);
            SampleRuntime.hold("largeWorld.left", "Drive Left", "A", value -> leftPressed = value);
            SampleRuntime.hold("largeWorld.brake", "Brake", "S", value -> brakePressed = value);
            SampleRuntime.hold("largeWorld.right", "Drive Right", "D", value -> rightPressed = value);
            SampleRuntime.camera(() -> {
                if (followCar) {
                    b2Vec2 position = b2Body_GetPosition(carChassisId);
                    return new SampleRuntime.CameraPosition(position.x, position.y, 4.0f);
                }
                return speed != 0.0f
                    ? new SampleRuntime.CameraPosition(viewPosition.x, viewPosition.y, 4.0f) : null;
            });
            SampleRuntime.beforeStep(() -> beforeStep(runtimeStep++));
        }

        void beforeStep(int stepCount) {
            viewPosition.x = Math.max(-span, Math.min(span, viewPosition.x + speed / 60.0f));
            if ((stepCount & 1) == 1 && explode) {
                b2ExplosionDef explosionDef = b2DefaultExplosionDef();
                explosionDef.position = new b2Vec2((0.5f + cycleIndex) * period - span, 7.0f);
                explosionDef.radius = 2.0f;
                explosionDef.falloff = 0.1f;
                explosionDef.impulsePerLength = 1.0f;
                b2World_Explode(scene.worldId, explosionDef);
                cycleIndex = (cycleIndex + 1) % cycleCount;
            }
            if (leftPressed) {
                setCarSpeed(20.0f);
            }
            if (brakePressed) {
                setCarSpeed(0.0f);
            }
            if (rightPressed) {
                setCarSpeed(-5.0f);
            }
        }

        private void setCarSpeed(float speed) {
            b2WheelJoint_SetMotorSpeed(carRearAxleId, speed);
            b2WheelJoint_SetMotorSpeed(carFrontAxleId, speed);
            b2Joint_WakeBodies(carRearAxleId);
        }

        private void createBoxGroup(float baseX) {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(baseX - 3.0f, 10.0f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2Polygon box = b2MakeBox(0.3f, 0.2f);
            for (int i = 0; i < 10; ++i) {
                bodyDef.position.y = 10.0f;
                for (int j = 0; j < 5; ++j) {
                    b2BodyId bodyId = scene.createBody(bodyDef);
                    b2CreatePolygonShape(bodyId, shapeDef, box);
                    bodyDef.position.y += 0.5f;
                }
                bodyDef.position.x += 0.6f;
            }
        }

        private void createDonut(b2Vec2 position, float scale) {
            int sides = 7;
            b2BodyId[] bodies = new b2BodyId[sides];
            float radius = scale;
            float deltaAngle = 2.0f * B2_PI / sides;
            float length = 2.0f * B2_PI * radius / sides;
            b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, -0.5f * length),
                new b2Vec2(0.0f, 0.5f * length), 0.25f * scale);
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.material.friction = 0.3f;
            float angle = 0.0f;
            for (int i = 0; i < sides; ++i) {
                bodyDef.position = new b2Vec2(radius * (float) Math.cos(angle) + position.x,
                    radius * (float) Math.sin(angle) + position.y);
                bodyDef.rotation = b2MakeRot(angle);
                bodies[i] = scene.createBody(bodyDef);
                b2CreateCapsuleShape(bodies[i], shapeDef, capsule);
                angle += deltaAngle;
            }
            b2WeldJointDef weldDef = b2DefaultWeldJointDef();
            weldDef.angularHertz = 5.0f;
            weldDef.angularDampingRatio = 0.0f;
            weldDef.localAnchorA = new b2Vec2(0.0f, 0.5f * length);
            weldDef.localAnchorB = new b2Vec2(0.0f, -0.5f * length);
            b2BodyId previous = bodies[sides - 1];
            for (b2BodyId body : bodies) {
                weldDef.bodyIdA = previous;
                weldDef.bodyIdB = body;
                weldDef.referenceAngle = b2RelativeAngle(b2Body_GetRotation(body), b2Body_GetRotation(previous));
                b2CreateWeldJoint(scene.worldId, weldDef);
                previous = body;
            }
        }

        private void createCar(b2Vec2 position, float scale, float hertz, float dampingRatio, float torque) {
            b2Vec2[] vertices = {
                new b2Vec2(-1.5f, -0.5f), new b2Vec2(1.5f, -0.5f), new b2Vec2(1.5f, 0.0f),
                new b2Vec2(0.0f, 0.9f), new b2Vec2(-1.15f, 0.9f), new b2Vec2(-1.5f, 0.2f)
            };
            for (b2Vec2 vertex : vertices) {
                vertex.x *= 0.85f * scale;
                vertex.y *= 0.85f * scale;
            }
            b2Polygon chassis = b2MakePolygon(b2ComputeHull(vertices, vertices.length), 0.15f * scale);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f / scale;
            shapeDef.material.friction = 0.2f;
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = b2Add(new b2Vec2(0.0f, scale), position);
            carChassisId = scene.createBody(bodyDef);
            b2CreatePolygonShape(carChassisId, shapeDef, chassis);

            shapeDef.density = 2.0f / scale;
            shapeDef.material.friction = 1.5f;
            shapeDef.material.rollingResistance = 0.1f;
            b2Circle circle = new b2Circle(new b2Vec2(), 0.4f * scale);
            bodyDef.position = b2Add(new b2Vec2(-scale, 0.35f * scale), position);
            bodyDef.allowFastRotation = true;
            b2BodyId rearWheelId = scene.createBody(bodyDef);
            b2CreateCircleShape(rearWheelId, shapeDef, circle);
            bodyDef.position = b2Add(new b2Vec2(scale, 0.4f * scale), position);
            b2BodyId frontWheelId = scene.createBody(bodyDef);
            b2CreateCircleShape(frontWheelId, shapeDef, circle);

            b2Vec2 axis = new b2Vec2(0.0f, 1.0f);
            b2WheelJointDef jointDef = b2DefaultWheelJointDef();
            jointDef.bodyIdA = carChassisId;
            jointDef.bodyIdB = rearWheelId;
            jointDef.localAxisA = b2Body_GetLocalVector(carChassisId, axis);
            jointDef.localAnchorA = b2Body_GetLocalPoint(carChassisId, b2Body_GetPosition(rearWheelId));
            jointDef.localAnchorB = b2Body_GetLocalPoint(rearWheelId, b2Body_GetPosition(rearWheelId));
            jointDef.maxMotorTorque = torque;
            jointDef.enableMotor = true;
            jointDef.hertz = hertz;
            jointDef.dampingRatio = dampingRatio;
            jointDef.lowerTranslation = -0.25f * scale;
            jointDef.upperTranslation = 0.25f * scale;
            jointDef.enableLimit = true;
            carRearAxleId = b2CreateWheelJoint(scene.worldId, jointDef);

            jointDef.bodyIdB = frontWheelId;
            jointDef.localAxisA = b2Body_GetLocalVector(carChassisId, axis);
            jointDef.localAnchorA = b2Body_GetLocalPoint(carChassisId, b2Body_GetPosition(frontWheelId));
            jointDef.localAnchorB = b2Body_GetLocalPoint(frontWheelId, b2Body_GetPosition(frontWheelId));
            carFrontAxleId = b2CreateWheelJoint(scene.worldId, jointDef);
        }

        private static int cRound(float value) {
            return value >= 0.0f ? (int) Math.floor(value + 0.5f) : (int) Math.ceil(value - 0.5f);
        }
    }
}
