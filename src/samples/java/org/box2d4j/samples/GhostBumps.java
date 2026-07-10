package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2SurfaceMaterial;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.*;

public final class GhostBumps {
    private GhostBumps() {
    }

    public static ContinuousSampleResult run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        Scene scene = new Scene(worldId);
        scene.createGround();
        scene.launch();

        SampleRuntime.toggle("ghostBumps.chain", "Chain", scene.useChain, value -> {
            scene.useChain = value;
            scene.createGround();
        });
        SampleRuntime.slider("ghostBumps.bevel", "Bevel", scene.bevel, 0.0f, 1.0f, 0.01f, value -> {
            scene.bevel = value;
            if (!scene.useChain) {
                scene.createGround();
            }
        });
        SampleRuntime.choice("ghostBumps.shape", "Shape", scene.shapeType,
            new String[] {"Circle", "Capsule", "Box"}, value -> scene.shapeType = value);
        SampleRuntime.slider("ghostBumps.round", "Round", scene.round, 0.0f, 0.4f, 0.1f,
            value -> scene.round = value);
        SampleRuntime.slider("ghostBumps.friction", "Friction", scene.friction, 0.0f, 1.0f, 0.1f, value -> {
            scene.friction = value;
            if (scene.shapeId != null && b2Shape_IsValid(scene.shapeId)) {
                b2Shape_SetFriction(scene.shapeId, value);
            }
            scene.createGround();
        });
        SampleRuntime.action("ghostBumps.launch", "Launch", scene::launch);

        return ContinuousSampleResult.simulate("ghostBumps", worldId, scene.bodyId);
    }

    private static final class Scene {
        final b2WorldId worldId;
        b2BodyId groundId = b2_nullBodyId;
        b2BodyId bodyId = b2_nullBodyId;
        b2ShapeId shapeId = b2_nullShapeId;
        int shapeType;
        float round;
        float friction = 0.2f;
        float bevel;
        boolean useChain = true;

        Scene(b2WorldId worldId) {
            this.worldId = worldId;
        }

        void createGround() {
            if (b2Body_IsValid(groundId)) {
                b2DestroyBody(groundId);
            }
            shapeId = b2_nullShapeId;
            groundId = b2CreateBody(worldId, b2DefaultBodyDef());

            float m = 1.0f / (float) Math.sqrt(2.0f);
            float mm = 2.0f * ((float) Math.sqrt(2.0f) - 1.0f);
            float hx = 4.0f;
            float hy = 0.25f;
            if (useChain) {
                b2Vec2[] points = new b2Vec2[20];
                points[0] = new b2Vec2(-3.0f * hx, hy);
                points[1] = b2Add(points[0], new b2Vec2(-2.0f * hx * m, 2.0f * hx * m));
                points[2] = b2Add(points[1], new b2Vec2(-2.0f * hx * m, 2.0f * hx * m));
                points[3] = b2Add(points[2], new b2Vec2(-2.0f * hx * m, 2.0f * hx * m));
                points[4] = b2Add(points[3], new b2Vec2(-2.0f * hy * m, -2.0f * hy * m));
                points[5] = b2Add(points[4], new b2Vec2(2.0f * hx * m, -2.0f * hx * m));
                points[6] = b2Add(points[5], new b2Vec2(2.0f * hx * m, -2.0f * hx * m));
                points[7] = b2Add(points[6], new b2Vec2(2.0f * hx * m + 2.0f * hy * (1.0f - m),
                    -2.0f * hx * m - 2.0f * hy * (1.0f - m)));
                points[8] = b2Add(points[7], new b2Vec2(2.0f * hx + hy * mm, 0.0f));
                points[9] = b2Add(points[8], new b2Vec2(2.0f * hx, 0.0f));
                points[10] = b2Add(points[9], new b2Vec2(2.0f * hx + hy * mm, 0.0f));
                points[11] = b2Add(points[10], new b2Vec2(2.0f * hx * m + 2.0f * hy * (1.0f - m),
                    2.0f * hx * m + 2.0f * hy * (1.0f - m)));
                points[12] = b2Add(points[11], new b2Vec2(2.0f * hx * m, 2.0f * hx * m));
                points[13] = b2Add(points[12], new b2Vec2(2.0f * hx * m, 2.0f * hx * m));
                points[14] = b2Add(points[13], new b2Vec2(-2.0f * hy * m, 2.0f * hy * m));
                points[15] = b2Add(points[14], new b2Vec2(-2.0f * hx * m, -2.0f * hx * m));
                points[16] = b2Add(points[15], new b2Vec2(-2.0f * hx * m, -2.0f * hx * m));
                points[17] = b2Add(points[16], new b2Vec2(-2.0f * hx * m, -2.0f * hx * m));
                points[18] = b2Add(points[17], new b2Vec2(-2.0f * hx, 0.0f));
                points[19] = b2Add(points[18], new b2Vec2(-2.0f * hx, 0.0f));

                b2SurfaceMaterial material = new b2SurfaceMaterial();
                material.friction = friction;
                b2ChainDef chainDef = b2DefaultChainDef();
                chainDef.points = points;
                chainDef.count = points.length;
                chainDef.isLoop = true;
                chainDef.materials = new b2SurfaceMaterial[] {material};
                chainDef.materialCount = 1;
                b2CreateChain(groundId, chainDef);
                return;
            }

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.material.friction = friction;
            b2Vec2[] vertices;
            if (bevel > 0.0f) {
                float hb = bevel;
                vertices = new b2Vec2[] {
                    new b2Vec2(hx + hb, hy - 0.05f), new b2Vec2(hx, hy), new b2Vec2(-hx, hy),
                    new b2Vec2(-hx - hb, hy - 0.05f), new b2Vec2(-hx - hb, -hy + 0.05f),
                    new b2Vec2(-hx, -hy), new b2Vec2(hx, -hy), new b2Vec2(hx + hb, -hy + 0.05f)
                };
            } else {
                vertices = new b2Vec2[] {new b2Vec2(hx, hy), new b2Vec2(-hx, hy),
                    new b2Vec2(-hx, -hy), new b2Vec2(hx, -hy)};
            }
            b2Hull hull = b2ComputeHull(vertices, vertices.length);

            float x = -3.0f * hx - m * hx - m * hy;
            float y = hy + m * hx - m * hy;
            for (int i = 0; i < 3; ++i) {
                createGroundPolygon(shapeDef, hull, x, y, -0.25f * B2_PI);
                x -= 2.0f * m * hx;
                y += 2.0f * m * hx;
            }
            x = -2.0f * hx;
            for (int i = 0; i < 3; ++i) {
                createGroundPolygon(shapeDef, hull, x, 0.0f, 0.0f);
                x += 2.0f * hx;
            }
            x = 3.0f * hx + m * hx + m * hy;
            y = hy + m * hx - m * hy;
            for (int i = 0; i < 3; ++i) {
                createGroundPolygon(shapeDef, hull, x, y, 0.25f * B2_PI);
                x += 2.0f * m * hx;
                y += 2.0f * m * hx;
            }
        }

        void createGroundPolygon(b2ShapeDef shapeDef, b2Hull hull, float x, float y, float angle) {
            b2Polygon polygon = b2MakeOffsetPolygon(hull, new b2Vec2(x, y), b2MakeRot(angle));
            b2CreatePolygonShape(groundId, shapeDef, polygon);
        }

        void launch() {
            if (b2Body_IsValid(bodyId)) {
                b2DestroyBody(bodyId);
            }
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(-28.0f, 18.0f);
            bodyId = b2CreateBody(worldId, bodyDef);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            shapeDef.material.friction = friction;
            if (shapeType == 0) {
                shapeId = b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(), 0.5f));
            } else if (shapeType == 1) {
                shapeId = b2CreateCapsuleShape(bodyId, shapeDef,
                    new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f));
            } else {
                float h = 0.5f - round;
                shapeId = b2CreatePolygonShape(bodyId, shapeDef, b2MakeRoundedBox(h, 2.0f * h, round));
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
