package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;

import java.util.ArrayList;

import static org.box2d4j.B2.*;

public final class BenchmarkBarrel {
    private BenchmarkBarrel() {
    }

    public static BenchmarkSampleResult run() {
        BenchmarkScenes.Scene scene = BenchmarkScenes.createBarrel();
        if (SampleRuntime.isActive()) {
            Controller controller = new Controller(scene);
            controller.reset(3);
            int[] shapeType = {3};
            SampleRuntime.choice("benchmarkBarrel.shape", "Shape", shapeType[0],
                new String[] {"Circle", "Capsule", "Mix", "Compound", "Human"}, value -> {
                    shapeType[0] = value;
                    controller.reset(value);
                });
            SampleRuntime.action("benchmarkBarrel.reset", "Reset Scene", () -> controller.reset(shapeType[0]));
        }
        return BenchmarkSampleResult.simulate("benchmarkBarrel", scene, 1);
    }

    private static final class Controller {
        final BenchmarkScenes.Scene scene;
        final ArrayList<Ragdoll.Human> humans = new ArrayList<>();
        int randomSeed;

        Controller(BenchmarkScenes.Scene scene) {
            this.scene = scene;
        }

        void reset(int shapeType) {
            for (b2BodyId bodyId : scene.bodies) {
                if (bodyId != null && b2Body_IsValid(bodyId) && b2Body_GetType(bodyId) != b2_staticBody) {
                    b2DestroyBody(bodyId);
                }
            }
            for (Ragdoll.Human human : humans) {
                human.destroy();
            }
            humans.clear();
            randomSeed = 42;

            int columnCount = 10;
            int rowCount = shapeType == 4 ? 5 : 40;
            float radius = 0.5f;
            float shift = 1.15f;
            float centerX = shift * columnCount / 2.0f;
            float centerY = shift / 2.0f;
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.angularDamping = shapeType == 2 ? 0.3f : 0.0f;
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            shapeDef.material.friction = 0.5f;
            b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, -0.25f),
                new b2Vec2(0.0f, 0.25f), radius);
            b2Circle circle = new b2Circle(new b2Vec2(), radius);
            b2Vec2[] vertices = {new b2Vec2(-1.0f, 0.0f), new b2Vec2(0.5f, 1.0f),
                new b2Vec2(0.0f, 2.0f)};
            b2Polygon left = b2MakePolygon(b2ComputeHull(vertices, vertices.length), 0.0f);
            vertices = new b2Vec2[] {new b2Vec2(1.0f, 0.0f), new b2Vec2(-0.5f, 1.0f),
                new b2Vec2(0.0f, 2.0f)};
            b2Polygon right = b2MakePolygon(b2ComputeHull(vertices, vertices.length), 0.0f);
            float side = -0.1f;
            float extraY = 0.5f;
            if (shapeType == 3) {
                extraY = 0.25f;
                side = 0.25f;
                shift = 2.0f;
                centerX = shift * columnCount / 2.0f - 1.0f;
            } else if (shapeType == 4) {
                side = 0.55f;
                shift = 2.5f;
                centerX = shift * columnCount / 2.0f;
            }
            float yStart = shapeType == 4 ? 2.0f : 100.0f;
            int index = 0;
            for (int column = 0; column < columnCount; ++column) {
                float x = column * shift - centerX;
                for (int row = 0; row < rowCount; ++row) {
                    float y = row * (shift + extraY) + centerY + yStart;
                    bodyDef.position = new b2Vec2(x + side, y);
                    side = -side;
                    if (shapeType == 4) {
                        humans.add(Ragdoll.Human.create(scene.worldId, bodyDef.position, 3.5f,
                            0.05f, 5.0f, 0.5f, index + 1));
                    } else {
                        b2BodyId bodyId = scene.createBody(bodyDef);
                        if (shapeType == 0) {
                            circle.radius = randomRange(0.25f, 0.75f);
                            shapeDef.material.rollingResistance = 0.2f;
                            b2CreateCircleShape(bodyId, shapeDef, circle);
                        } else if (shapeType == 1) {
                            capsule.radius = randomRange(0.25f, 0.5f);
                            float length = randomRange(0.25f, 1.0f);
                            capsule.center1 = new b2Vec2(0.0f, -0.5f * length);
                            capsule.center2 = new b2Vec2(0.0f, 0.5f * length);
                            shapeDef.material.rollingResistance = 0.2f;
                            b2CreateCapsuleShape(bodyId, shapeDef, capsule);
                        } else if (shapeType == 2) {
                            int mod = index % 3;
                            if (mod == 0) {
                                circle.radius = randomRange(0.25f, 0.75f);
                                b2CreateCircleShape(bodyId, shapeDef, circle);
                            } else if (mod == 1) {
                                capsule.radius = randomRange(0.25f, 0.5f);
                                float length = randomRange(0.25f, 1.0f);
                                capsule.center1 = new b2Vec2(0.0f, -0.5f * length);
                                capsule.center2 = new b2Vec2(0.0f, 0.5f * length);
                                b2CreateCapsuleShape(bodyId, shapeDef, capsule);
                            } else {
                                float width = randomRange(0.1f, 0.5f);
                                float height = randomRange(0.5f, 0.75f);
                                b2Polygon box = b2MakeBox(width, height);
                                box.radius = 0.25f * Math.max(0.0f, randomRange(-1.0f, 1.0f));
                                b2CreatePolygonShape(bodyId, shapeDef, box);
                            }
                        } else {
                            b2CreatePolygonShape(bodyId, shapeDef, left);
                            b2CreatePolygonShape(bodyId, shapeDef, right);
                        }
                    }
                    index += 1;
                }
            }
        }

        float randomRange(float lo, float hi) {
            int value = randomSeed;
            value ^= value << 13;
            value ^= value >>> 17;
            value ^= value << 5;
            randomSeed = value;
            return lo + (hi - lo) * ((value & 32767) / 32767.0f);
        }
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
