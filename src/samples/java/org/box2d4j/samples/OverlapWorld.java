package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2ShapeProxy;
import org.box2d4j.b2Transform;
import org.box2d4j.b2TreeStats;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;
import java.util.List;

import static org.box2d4j.B2.*;

public final class OverlapWorld {
    public static final int CIRCLE_SHAPE = 0;
    public static final int CAPSULE_SHAPE = 1;
    public static final int BOX_SHAPE = 2;
    private static final int BODY_COUNT = 10;
    private static final int IGNORE_INDEX = 7;

    private OverlapWorld() {
    }

    public static Result run() {
        if (SampleRuntime.isActive()) {
            InteractiveScene scene = new InteractiveScene();
            for (int shapeIndex = 0; shapeIndex < 7; ++shapeIndex) {
                final int index = shapeIndex;
                String name = new String[] {"Polygon 1", "Polygon 2", "Polygon 3", "Box", "Circle",
                    "Capsule", "Segment"}[shapeIndex];
                SampleRuntime.action("overlapWorld.create" + shapeIndex, name, () -> scene.create(index));
                SampleRuntime.action("overlapWorld.create10x" + shapeIndex, "10x " + name,
                    () -> scene.createN(index, 10));
            }
            SampleRuntime.action("overlapWorld.destroy", "Destroy Shape", scene::destroyBody);
            SampleRuntime.choice("overlapWorld.query", "Overlap Shape", scene.shapeType,
                new String[] {"Circle", "Capsule", "Box"}, scene::setShapeType);
            SampleRuntime.slider("overlapWorld.angle", "Query Angle", 0.0f, -B2_PI, B2_PI, 0.01f,
                value -> scene.angle = value);
            SampleRuntime.pointer(scene);
            SampleRuntime.afterStep(scene::afterStep);
            b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
            b2DestroyWorld(scene.worldId);
            return new Result(new VariantResult[0]);
        }
        VariantResult[] variants = {
            runVariant(CIRCLE_SHAPE, new b2Vec2(0.0f, 10.0f), 0.0f),
            runVariant(CIRCLE_SHAPE, new b2Vec2(0.6f, 6.5f), 0.0f),
            runVariant(CAPSULE_SHAPE, new b2Vec2(10.5f, 9.9f), 0.25f),
            runVariant(BOX_SHAPE, new b2Vec2(8.1f, 15.8f), -0.3f)
        };
        return new Result(variants);
    }

    private static final class InteractiveScene implements SampleRuntime.PointerHandler {
        static final int MAX_COUNT = 64;
        final b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        final b2BodyId[] bodyIds = new b2BodyId[MAX_COUNT];
        final BodyUserData[] userData = new BodyUserData[MAX_COUNT];
        final b2Polygon[] polygons = new b2Polygon[4];
        final b2Capsule capsule = new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f);
        final b2Circle circle = new b2Circle(new b2Vec2(), 0.5f);
        final org.box2d4j.b2Segment segment = new org.box2d4j.b2Segment(
            new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f));
        final RandomState random = new RandomState(12345);
        final b2Vec2 position = new b2Vec2(0.0f, 10.0f);
        final b2BodyId queryBodyId;
        b2ShapeId queryShapeId = b2_nullShapeId;
        int bodyIndex;
        int shapeType;
        float angle;
        boolean dragging;

        InteractiveScene() {
            b2Vec2[] vertices = {new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f),
                new b2Vec2(0.0f, 1.5f)};
            polygons[0] = b2MakePolygon(b2ComputeHull(vertices, vertices.length), 0.0f);
            vertices = new b2Vec2[] {new b2Vec2(-0.1f, 0.0f), new b2Vec2(0.1f, 0.0f),
                new b2Vec2(0.0f, 1.5f)};
            polygons[1] = b2MakePolygon(b2ComputeHull(vertices, vertices.length), 0.0f);
            float width = 1.0f;
            float bevel = width / (2.0f + (float) Math.sqrt(2.0f));
            float side = (float) Math.sqrt(2.0f) * bevel;
            vertices = new b2Vec2[] {
                new b2Vec2(0.5f * side, 0.0f), new b2Vec2(0.5f * width, bevel),
                new b2Vec2(0.5f * width, bevel + side), new b2Vec2(0.5f * side, width),
                new b2Vec2(-0.5f * side, width), new b2Vec2(-0.5f * width, bevel + side),
                new b2Vec2(-0.5f * width, bevel), new b2Vec2(-0.5f * side, 0.0f)
            };
            polygons[2] = b2MakePolygon(b2ComputeHull(vertices, vertices.length), 0.0f);
            polygons[3] = b2MakeBox(0.5f, 0.5f);
            createN(0, 10);

            b2BodyDef queryBodyDef = b2DefaultBodyDef();
            queryBodyDef.type = b2_kinematicBody;
            queryBodyDef.position = position.copy();
            queryBodyId = b2CreateBody(worldId, queryBodyDef);
            setShapeType(CIRCLE_SHAPE);
        }

        void create(int index) {
            if (bodyIds[bodyIndex] != null && b2Body_IsValid(bodyIds[bodyIndex])) {
                b2DestroyBody(bodyIds[bodyIndex]);
            }
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(random.range(-20.0f, 20.0f), random.range(0.0f, 20.0f));
            bodyDef.rotation = b2MakeRot(random.range(-B2_PI, B2_PI));
            bodyIds[bodyIndex] = b2CreateBody(worldId, bodyDef);
            userData[bodyIndex] = new BodyUserData(bodyIndex, bodyIndex == IGNORE_INDEX);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.userData = userData[bodyIndex];
            if (index < 4) {
                b2CreatePolygonShape(bodyIds[bodyIndex], shapeDef, polygons[index]);
            } else if (index == 4) {
                b2CreateCircleShape(bodyIds[bodyIndex], shapeDef, circle);
            } else if (index == 5) {
                b2CreateCapsuleShape(bodyIds[bodyIndex], shapeDef, capsule);
            } else {
                b2CreateSegmentShape(bodyIds[bodyIndex], shapeDef, segment);
            }
            bodyIndex = (bodyIndex + 1) % MAX_COUNT;
        }

        void createN(int index, int count) {
            for (int i = 0; i < count; ++i) {
                create(index);
            }
        }

        void destroyBody() {
            for (int i = 0; i < MAX_COUNT; ++i) {
                if (bodyIds[i] != null && b2Body_IsValid(bodyIds[i])) {
                    b2DestroyBody(bodyIds[i]);
                    bodyIds[i] = null;
                    return;
                }
            }
        }

        void setShapeType(int nextShapeType) {
            shapeType = nextShapeType;
            if (b2Shape_IsValid(queryShapeId)) {
                b2DestroyShape(queryShapeId, false);
            }
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.userData = new BodyUserData(-1, true);
            shapeDef.filter.maskBits = 0L;
            if (shapeType == CIRCLE_SHAPE) {
                queryShapeId = b2CreateCircleShape(queryBodyId, shapeDef,
                    new b2Circle(new b2Vec2(), 1.0f));
            } else if (shapeType == CAPSULE_SHAPE) {
                queryShapeId = b2CreateCapsuleShape(queryBodyId, shapeDef,
                    new b2Capsule(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f), 0.5f));
            } else {
                queryShapeId = b2CreatePolygonShape(queryBodyId, shapeDef, b2MakeBox(2.0f, 0.5f));
            }
        }

        void afterStep() {
            b2Body_SetTransform(queryBodyId, position, b2MakeRot(angle));
            b2Transform transform = new b2Transform(position, b2MakeRot(angle));
            b2ShapeProxy proxy;
            if (shapeType == CIRCLE_SHAPE) {
                proxy = b2MakeProxy(new b2Vec2[] {position}, 1, 1.0f);
            } else if (shapeType == CAPSULE_SHAPE) {
                b2Vec2 p1 = b2TransformPoint(transform, new b2Vec2(-1.0f, 0.0f));
                b2Vec2 p2 = b2TransformPoint(transform, new b2Vec2(1.0f, 0.0f));
                proxy = b2MakeProxy(new b2Vec2[] {p1, p2}, 2, 0.5f);
            } else {
                b2Polygon box = b2MakeOffsetBox(2.0f, 0.5f, position, transform.q);
                proxy = b2MakeProxy(box.vertices, box.count, box.radius);
            }
            List<b2BodyId> doomed = new java.util.ArrayList<>();
            b2World_OverlapShape(worldId, proxy, b2DefaultQueryFilter(), shapeId -> {
                Object value = b2Shape_GetUserData(shapeId);
                if (value instanceof BodyUserData && ((BodyUserData) value).ignore) {
                    return true;
                }
                b2BodyId bodyId = b2Shape_GetBody(shapeId);
                if (doomed.size() < 16) {
                    doomed.add(bodyId);
                }
                return true;
            });
            for (b2BodyId bodyId : doomed) {
                if (b2Body_IsValid(bodyId)) {
                    for (int i = 0; i < bodyIds.length; ++i) {
                        if (bodyIds[i] != null && B2_ID_EQUALS(bodyIds[i], bodyId)) {
                            bodyIds[i] = null;
                            break;
                        }
                    }
                    b2DestroyBody(bodyId);
                }
            }
        }

        @Override
        public void down(float worldX, float worldY, int button) {
            if (button == 0) {
                dragging = true;
                position.x = worldX;
                position.y = worldY;
            }
        }

        @Override
        public void up(float worldX, float worldY, int button) {
            if (button == 0) {
                dragging = false;
            }
        }

        @Override
        public void move(float worldX, float worldY) {
            if (dragging) {
                position.x = worldX;
                position.y = worldY;
            }
        }
    }

    public static VariantResult runVariant(int shapeType, b2Vec2 queryPosition, float queryAngle) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        RandomState random = new RandomState(12345);
        b2BodyId[] bodyIds = new b2BodyId[BODY_COUNT];
        BodyUserData[] userData = new BodyUserData[BODY_COUNT];

        b2Vec2[] vertices = {
            new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), new b2Vec2(0.0f, 1.5f)
        };
        b2Hull hull = b2ComputeHull(vertices, vertices.length);
        b2Polygon polygon = b2MakePolygon(hull, 0.0f);

        for (int i = 0; i < BODY_COUNT; ++i) {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(random.range(-20.0f, 20.0f), random.range(0.0f, 20.0f));
            bodyDef.rotation = b2MakeRot(random.range(-B2_PI, B2_PI));
            bodyIds[i] = b2CreateBody(worldId, bodyDef);
            userData[i] = new BodyUserData(i, i == IGNORE_INDEX);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.userData = userData[i];
            b2CreatePolygonShape(bodyIds[i], shapeDef, polygon);
        }

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        BodyState[] bodies = new BodyState[BODY_COUNT];
        for (int i = 0; i < BODY_COUNT; ++i) {
            b2Transform transform = b2Body_GetTransform(bodyIds[i]);
            bodies[i] = new BodyState(transform.p.x, transform.p.y, transform.q.c, transform.q.s);
        }
        b2Counters before = b2World_GetCounters(worldId);

        b2Transform queryTransform = new b2Transform(queryPosition, b2MakeRot(queryAngle));
        b2ShapeProxy proxy;
        if (shapeType == CIRCLE_SHAPE) {
            proxy = b2MakeProxy(new b2Vec2[] {queryTransform.p}, 1, 1.0f);
        } else if (shapeType == CAPSULE_SHAPE) {
            b2Capsule capsule = new b2Capsule(
                b2TransformPoint(queryTransform, new b2Vec2(-1.0f, 0.0f)),
                b2TransformPoint(queryTransform, new b2Vec2(1.0f, 0.0f)), 0.5f);
            proxy = b2MakeProxy(new b2Vec2[] {capsule.center1, capsule.center2}, 2, capsule.radius);
        } else {
            b2Polygon box = b2MakeOffsetBox(2.0f, 0.5f, queryTransform.p, queryTransform.q);
            proxy = b2MakeProxy(box.vertices, box.count, box.radius);
        }

        int[] doomed = new int[16];
        b2ShapeId[] doomedShapeIds = new b2ShapeId[16];
        int[] doomedCount = {0};
        b2TreeStats stats = b2World_OverlapShape(worldId, proxy, b2DefaultQueryFilter(), shapeId -> {
            BodyUserData data = (BodyUserData) b2Shape_GetUserData(shapeId);
            if (data != null && data.ignore) {
                return true;
            }
            if (doomedCount[0] < doomed.length) {
                int index = doomedCount[0]++;
                doomed[index] = data == null ? -1 : data.index;
                doomedShapeIds[index] = new b2ShapeId(shapeId.index1, shapeId.world0, shapeId.generation);
            }
            return true;
        });

        for (int i = 0; i < doomedCount[0]; ++i) {
            BodyUserData data = (BodyUserData) b2Shape_GetUserData(doomedShapeIds[i]);
            if (data != null) {
                b2DestroyBody(bodyIds[data.index]);
            }
        }

        boolean[] valid = new boolean[BODY_COUNT];
        for (int i = 0; i < BODY_COUNT; ++i) {
            valid[i] = b2Body_IsValid(bodyIds[i]);
        }
        int[] doomedIndices = new int[doomedCount[0]];
        System.arraycopy(doomed, 0, doomedIndices, 0, doomedCount[0]);
        b2Counters after = b2World_GetCounters(worldId);
        VariantResult result = new VariantResult(shapeType, queryPosition.x, queryPosition.y, queryAngle,
            before.bodyCount, before.shapeCount,
            after.bodyCount, after.shapeCount, stats.nodeVisits, stats.leafVisits,
            doomedIndices, bodies, valid);
        b2DestroyWorld(worldId);
        return result;
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    private static final class BodyUserData {
        final int index;
        final boolean ignore;

        BodyUserData(int index, boolean ignore) {
            this.index = index;
            this.ignore = ignore;
        }
    }

    private static final class RandomState {
        private int seed;

        RandomState(int seed) {
            this.seed = seed;
        }

        float range(float lo, float hi) {
            int x = seed;
            x ^= x << 13;
            x ^= x >>> 17;
            x ^= x << 5;
            seed = x;
            float r = (x & 32767) / 32767.0f;
            return (hi - lo) * r + lo;
        }
    }

    public static final class Result {
        public final VariantResult[] variants;

        Result(VariantResult[] variants) {
            this.variants = variants;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder("overlapWorld ").append(variants.length);
            for (VariantResult variant : variants) {
                builder.append(variant.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class VariantResult {
        public final int shapeType;
        public final float queryX;
        public final float queryY;
        public final float queryAngle;
        public final int beforeBodyCount;
        public final int beforeShapeCount;
        public final int afterBodyCount;
        public final int afterShapeCount;
        public final int nodeVisits;
        public final int leafVisits;
        public final int[] doomedIndices;
        public final BodyState[] bodies;
        public final boolean[] valid;

        VariantResult(int shapeType, float queryX, float queryY, float queryAngle,
                      int beforeBodyCount, int beforeShapeCount, int afterBodyCount,
                      int afterShapeCount, int nodeVisits, int leafVisits, int[] doomedIndices,
                      BodyState[] bodies, boolean[] valid) {
            this.shapeType = shapeType;
            this.queryX = queryX;
            this.queryY = queryY;
            this.queryAngle = queryAngle;
            this.beforeBodyCount = beforeBodyCount;
            this.beforeShapeCount = beforeShapeCount;
            this.afterBodyCount = afterBodyCount;
            this.afterShapeCount = afterShapeCount;
            this.nodeVisits = nodeVisits;
            this.leafVisits = leafVisits;
            this.doomedIndices = doomedIndices;
            this.bodies = bodies;
            this.valid = valid;
        }

        String toLinePart() {
            StringBuilder builder = new StringBuilder().append(' ').append(shapeType)
                .append(formatFloat(queryX)).append(formatFloat(queryY)).append(formatFloat(queryAngle))
                .append(' ').append(beforeBodyCount).append(' ').append(beforeShapeCount)
                .append(' ').append(afterBodyCount).append(' ').append(afterShapeCount)
                .append(' ').append(nodeVisits).append(' ').append(leafVisits)
                .append(' ').append(doomedIndices.length);
            for (int index : doomedIndices) {
                builder.append(' ').append(index);
            }
            builder.append(' ').append(bodies.length);
            for (int i = 0; i < bodies.length; ++i) {
                builder.append(bodies[i].toLinePart()).append(' ').append(valid[i] ? 1 : 0);
            }
            return builder.toString();
        }
    }

    public static final class BodyState {
        public final float x;
        public final float y;
        public final float cos;
        public final float sin;

        BodyState(float x, float y, float cos, float sin) {
            this.x = x;
            this.y = y;
            this.cos = cos;
            this.sin = sin;
        }

        String toLinePart() {
            return formatFloat(x) + formatFloat(y) + formatFloat(cos) + formatFloat(sin);
        }
    }

    private static String formatFloat(float value) {
        return " " + String.format(Locale.ROOT, "%.9g", value);
    }
}
