package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RayResult;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2ShapeProxy;
import org.box2d4j.b2Transform;
import org.box2d4j.b2TreeStats;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.box2d4j.B2.*;

public final class CastWorld {
    public static final int ANY = 0;
    public static final int CLOSEST = 1;
    public static final int MULTIPLE = 2;
    public static final int SORTED = 3;
    public static final int SIMPLE = 4;
    public static final int RAY_CAST = 0;
    public static final int CIRCLE_CAST = 1;
    public static final int CAPSULE_CAST = 2;
    public static final int POLYGON_CAST = 3;
    private static final int CREATED_BODY_COUNT = 8;
    private static final int IGNORE_INDEX = 7;

    private CastWorld() {
    }

    public static Result run() {
        Scene scene = createScene();
        if (SampleRuntime.isActive()) {
            InteractiveState state = new InteractiveState(scene);
            SampleRuntime.toggle("castWorld.simple", "Simple", false, value -> state.simple = value);
            SampleRuntime.choice("castWorld.type", "Type", state.castType,
                new String[] {"Ray", "Circle", "Capsule", "Polygon"}, value -> {
                    state.castType = value;
                    state.rebuildMarker();
                });
            SampleRuntime.slider("castWorld.radius", "Radius", state.radius, 0.0f, 2.0f, 0.1f, value -> {
                state.radius = value;
                state.rebuildMarker();
            });
            SampleRuntime.choice("castWorld.mode", "Mode", state.mode,
                new String[] {"Any", "Closest", "Multiple", "Sorted"}, value -> state.mode = value);
            SampleRuntime.slider("castWorld.angle", "Angle", 0.0f, -B2_PI, B2_PI, 0.01f,
                value -> state.angle = value);
            for (int shapeIndex = 0; shapeIndex < 7; ++shapeIndex) {
                final int index = shapeIndex;
                String name = new String[] {"Polygon 1", "Polygon 2", "Polygon 3", "Box", "Circle",
                    "Capsule", "Segment"}[shapeIndex];
                SampleRuntime.action("castWorld.create" + shapeIndex, name, () -> state.create(index));
                SampleRuntime.action("castWorld.create10x" + shapeIndex, "10x " + name,
                    () -> state.createN(index, 10));
            }
            SampleRuntime.action("castWorld.destroy", "Destroy Shape", state::destroyBody);
            SampleRuntime.pointer(state);
            SampleRuntime.afterStep(state::afterStep);
            b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
            b2DestroyWorld(scene.worldId);
            return new Result(0, 0, 0, 0, IGNORE_INDEX, new QueryResult[0], new BodyState[0]);
        }
        b2Vec2 defaultStart = new b2Vec2(-20.0f, 10.0f);
        b2Vec2 defaultEnd = new b2Vec2(20.0f, 10.0f);
        List<QueryResult> queries = new ArrayList<>();
        for (int castType = RAY_CAST; castType <= POLYGON_CAST; ++castType) {
            float angle = 0.2f * castType;
            for (int mode = ANY; mode <= SORTED; ++mode) {
                queries.add(cast(scene.worldId, castType, mode, defaultStart, defaultEnd, angle, 0.5f));
            }
        }
        queries.add(simpleCast(scene.worldId, defaultStart, defaultEnd));
        queries.add(cast(scene.worldId, RAY_CAST, SORTED,
            new b2Vec2(-20.0f, 16.7f), new b2Vec2(20.0f, 16.7f), 0.0f, 0.5f));

        b2Counters counters = b2World_GetCounters(scene.worldId);
        BodyState[] bodies = new BodyState[CREATED_BODY_COUNT];
        for (int i = 0; i < bodies.length; ++i) {
            b2Transform transform = b2Body_GetTransform(scene.bodyIds[i]);
            bodies[i] = new BodyState(b2Body_GetType(scene.bodyIds[i]), transform.p.x, transform.p.y,
                transform.q.c, transform.q.s);
        }
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            b2World_GetAwakeBodyCount(scene.worldId), IGNORE_INDEX,
            queries.toArray(new QueryResult[0]), bodies);
        b2DestroyWorld(scene.worldId);
        return result;
    }

    private static Scene createScene() {
        Scene scene = new Scene();
        scene.worldId = b2CreateWorld(b2DefaultWorldDef());
        RandomState random = new RandomState(12345);

        b2BodyId groundId = b2CreateBody(scene.worldId, b2DefaultBodyDef());
        b2CreateSegmentShape(groundId, b2DefaultShapeDef(),
            new b2Segment(new b2Vec2(-40.0f, 0.0f), new b2Vec2(40.0f, 0.0f)));

        b2Polygon[] polygons = new b2Polygon[4];
        b2Vec2[] vertices = {
            new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), new b2Vec2(0.0f, 1.5f)
        };
        polygons[0] = b2MakePolygon(b2ComputeHull(vertices, vertices.length), 0.0f);
        vertices = new b2Vec2[] {
            new b2Vec2(-0.1f, 0.0f), new b2Vec2(0.1f, 0.0f), new b2Vec2(0.0f, 1.5f)
        };
        polygons[1] = b2MakePolygon(b2ComputeHull(vertices, vertices.length), 0.0f);
        polygons[1].radius = 0.5f;
        float w = 1.0f;
        float b = w / (2.0f + (float) Math.sqrt(2.0f));
        float s = (float) Math.sqrt(2.0f) * b;
        vertices = new b2Vec2[] {
            new b2Vec2(0.5f * s, 0.0f), new b2Vec2(0.5f * w, b), new b2Vec2(0.5f * w, b + s),
            new b2Vec2(0.5f * s, w), new b2Vec2(-0.5f * s, w), new b2Vec2(-0.5f * w, b + s),
            new b2Vec2(-0.5f * w, b), new b2Vec2(-0.5f * s, 0.0f)
        };
        polygons[2] = b2MakePolygon(b2ComputeHull(vertices, vertices.length), 0.0f);
        polygons[3] = b2MakeBox(0.5f, 0.5f);
        b2Circle circle = new b2Circle(new b2Vec2(), 0.5f);
        b2Capsule capsule = new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f);
        b2Segment segment = new b2Segment(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f));

        for (int i = 0; i < CREATED_BODY_COUNT; ++i) {
            int shapeIndex = i % 7;
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(random.range(-20.0f, 20.0f), random.range(0.0f, 20.0f));
            bodyDef.rotation = b2MakeRot(random.range(-B2_PI, B2_PI));
            int mod = i % 3;
            bodyDef.type = mod == 0 ? b2_staticBody : mod == 1 ? b2_kinematicBody : b2_dynamicBody;
            if (bodyDef.type == b2_dynamicBody) {
                bodyDef.gravityScale = 0.0f;
            }
            scene.bodyIds[i] = b2CreateBody(scene.worldId, bodyDef);
            BodyUserData data = new BodyUserData(i, i == IGNORE_INDEX);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.userData = data;
            if (shapeIndex < 4) {
                b2CreatePolygonShape(scene.bodyIds[i], shapeDef, polygons[shapeIndex]);
            } else if (shapeIndex == 4) {
                b2CreateCircleShape(scene.bodyIds[i], shapeDef, circle);
            } else if (shapeIndex == 5) {
                b2CreateCapsuleShape(scene.bodyIds[i], shapeDef, capsule);
            } else {
                b2CreateSegmentShape(scene.bodyIds[i], shapeDef, segment);
            }
        }
        b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
        return scene;
    }

    private static QueryResult cast(b2WorldId worldId, int castType, int mode, b2Vec2 start,
                                    b2Vec2 end, float angle, float radius) {
        CastContext context = new CastContext();
        b2Vec2 translation = b2Sub(end, start);
        b2TreeStats stats;
        if (castType == RAY_CAST) {
            stats = b2World_CastRay(worldId, start, translation, b2DefaultQueryFilter(),
                (shapeId, point, normal, fraction) -> castCallback(context, mode, shapeId, point, normal, fraction));
        } else {
            b2Transform transform = new b2Transform(start, b2MakeRot(angle));
            b2ShapeProxy proxy;
            if (castType == CIRCLE_CAST) {
                proxy = b2MakeProxy(new b2Vec2[] {start}, 1, radius);
            } else if (castType == CAPSULE_CAST) {
                b2Vec2 p1 = b2TransformPoint(transform, new b2Vec2(-0.25f, 0.0f));
                b2Vec2 p2 = b2TransformPoint(transform, new b2Vec2(0.25f, 0.0f));
                proxy = b2MakeProxy(new b2Vec2[] {p1, p2}, 2, radius);
            } else {
                b2Polygon box = b2MakeOffsetRoundedBox(0.25f, 0.5f, transform.p, transform.q, radius);
                proxy = b2MakeProxy(box.vertices, box.count, box.radius);
            }
            stats = b2World_CastShape(worldId, proxy, translation, b2DefaultQueryFilter(),
                (shapeId, point, normal, fraction) -> castCallback(context, mode, shapeId, point, normal, fraction));
        }
        return queryResult(castType, mode, start, end, angle, radius, stats, context);
    }

    private static float castCallback(CastContext context, int mode, b2ShapeId shapeId,
                                      b2Vec2 point, b2Vec2 normal, float fraction) {
        BodyUserData data = (BodyUserData) b2Shape_GetUserData(shapeId);
        if ((data != null && data.ignore) || fraction == 0.0f) {
            return -1.0f;
        }
        int shapeIndex = data == null ? -1 : data.index;
        if (mode == ANY || mode == CLOSEST) {
            context.set(0, point, normal, fraction, shapeIndex);
            context.count = 1;
            return mode == ANY ? 0.0f : fraction;
        }
        if (mode == MULTIPLE) {
            int index = context.count;
            context.set(index, point, normal, fraction, shapeIndex);
            context.count = index + 1;
            return context.count == 3 ? 0.0f : 1.0f;
        }

        int index = 3;
        while (fraction < context.fractions[index - 1]) {
            index -= 1;
            if (index == 0) {
                break;
            }
        }
        if (index == 3) {
            return context.fractions[2];
        }
        for (int j = 2; j > index; --j) {
            context.copy(j, j - 1);
        }
        context.set(index, point, normal, fraction, shapeIndex);
        context.count = Math.min(context.count + 1, 3);
        return context.count == 3 ? context.fractions[2] : 1.0f;
    }

    private static QueryResult simpleCast(b2WorldId worldId, b2Vec2 start, b2Vec2 end) {
        b2RayResult result = b2World_CastRayClosest(worldId, start, b2Sub(end, start), b2DefaultQueryFilter());
        BodyUserData data = result.hit ? (BodyUserData) b2Shape_GetUserData(result.shapeId) : null;
        HitState[] hits = result.hit ? new HitState[] {new HitState(result.point.x, result.point.y,
            result.normal.x, result.normal.y, result.fraction, data == null ? -1 : data.index)} : new HitState[0];
        return new QueryResult(RAY_CAST, SIMPLE, start.x, start.y, end.x, end.y, 0.0f, 0.5f,
            result.nodeVisits, result.leafVisits, hits);
    }

    private static QueryResult queryResult(int castType, int mode, b2Vec2 start, b2Vec2 end,
                                           float angle, float radius, b2TreeStats stats, CastContext context) {
        HitState[] hits = new HitState[context.count];
        for (int i = 0; i < hits.length; ++i) {
            hits[i] = new HitState(context.points[i].x, context.points[i].y,
                context.normals[i].x, context.normals[i].y, context.fractions[i], context.shapeIndices[i]);
        }
        return new QueryResult(castType, mode, start.x, start.y, end.x, end.y, angle, radius,
            stats.nodeVisits, stats.leafVisits, hits);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    private static final class Scene {
        b2WorldId worldId;
        final b2BodyId[] bodyIds = new b2BodyId[64];
    }

    private static final class InteractiveState implements SampleRuntime.PointerHandler {
        final Scene scene;
        final RandomState random = new RandomState(9876);
        final b2Polygon[] polygons = new b2Polygon[4];
        final b2Circle circle = new b2Circle(new b2Vec2(), 0.5f);
        final b2Capsule capsule = new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f);
        final b2Segment segment = new b2Segment(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f));
        final b2Vec2 rayStart = new b2Vec2(-20.0f, 10.0f);
        final b2Vec2 rayEnd = new b2Vec2(20.0f, 10.0f);
        final b2BodyId guideBodyId;
        final b2ShapeId guideSegmentId;
        b2ShapeId guideMarkerId = b2_nullShapeId;
        QueryResult lastQuery;
        int bodyIndex = CREATED_BODY_COUNT;
        int castType = RAY_CAST;
        int mode = CLOSEST;
        float radius = 0.5f;
        float angle;
        boolean simple;
        boolean dragging;

        InteractiveState(Scene scene) {
            this.scene = scene;
            b2Vec2[] vertices = {new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f),
                new b2Vec2(0.0f, 1.5f)};
            polygons[0] = b2MakePolygon(b2ComputeHull(vertices, vertices.length), 0.0f);
            vertices = new b2Vec2[] {new b2Vec2(-0.1f, 0.0f), new b2Vec2(0.1f, 0.0f),
                new b2Vec2(0.0f, 1.5f)};
            polygons[1] = b2MakePolygon(b2ComputeHull(vertices, vertices.length), 0.0f);
            polygons[1].radius = 0.5f;
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

            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_kinematicBody;
            guideBodyId = b2CreateBody(scene.worldId, bodyDef);
            b2ShapeDef shapeDef = ignoredShapeDef();
            guideSegmentId = b2CreateSegmentShape(guideBodyId, shapeDef,
                new b2Segment(rayStart.copy(), rayEnd.copy()));
            rebuildMarker();
        }

        void create(int index) {
            int slot = bodyIndex % scene.bodyIds.length;
            if (scene.bodyIds[slot] != null && b2Body_IsValid(scene.bodyIds[slot])) {
                b2DestroyBody(scene.bodyIds[slot]);
            }
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(random.range(-20.0f, 20.0f), random.range(0.0f, 20.0f));
            bodyDef.rotation = b2MakeRot(random.range(-B2_PI, B2_PI));
            int mod = slot % 3;
            bodyDef.type = mod == 0 ? b2_staticBody : mod == 1 ? b2_kinematicBody : b2_dynamicBody;
            bodyDef.gravityScale = bodyDef.type == b2_dynamicBody ? 0.0f : 1.0f;
            b2BodyId bodyId = b2CreateBody(scene.worldId, bodyDef);
            scene.bodyIds[slot] = bodyId;
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.userData = new BodyUserData(slot, slot == IGNORE_INDEX);
            if (index < 4) {
                b2CreatePolygonShape(bodyId, shapeDef, polygons[index]);
            } else if (index == 4) {
                b2CreateCircleShape(bodyId, shapeDef, circle);
            } else if (index == 5) {
                b2CreateCapsuleShape(bodyId, shapeDef, capsule);
            } else {
                b2CreateSegmentShape(bodyId, shapeDef, segment);
            }
            bodyIndex = (slot + 1) % scene.bodyIds.length;
        }

        void createN(int index, int count) {
            for (int i = 0; i < count; ++i) {
                create(index);
            }
        }

        void destroyBody() {
            for (int i = 0; i < scene.bodyIds.length; ++i) {
                if (scene.bodyIds[i] != null && b2Body_IsValid(scene.bodyIds[i])) {
                    b2DestroyBody(scene.bodyIds[i]);
                    scene.bodyIds[i] = null;
                    return;
                }
            }
        }

        void rebuildMarker() {
            if (b2Shape_IsValid(guideMarkerId)) {
                b2DestroyShape(guideMarkerId, false);
            }
            b2ShapeDef shapeDef = ignoredShapeDef();
            float markerRadius = castType == RAY_CAST ? 0.08f : radius;
            if (castType == CAPSULE_CAST) {
                guideMarkerId = b2CreateCapsuleShape(guideBodyId, shapeDef,
                    new b2Capsule(new b2Vec2(-0.25f, 0.0f), new b2Vec2(0.25f, 0.0f), markerRadius));
            } else if (castType == POLYGON_CAST) {
                guideMarkerId = b2CreatePolygonShape(guideBodyId, shapeDef,
                    b2MakeRoundedBox(0.25f, 0.5f, markerRadius));
            } else {
                guideMarkerId = b2CreateCircleShape(guideBodyId, shapeDef,
                    new b2Circle(new b2Vec2(), markerRadius));
            }
        }

        void afterStep() {
            b2Body_SetTransform(guideBodyId, rayStart, b2MakeRot(angle));
            b2Vec2 localEnd = b2InvRotateVector(b2Body_GetRotation(guideBodyId), b2Sub(rayEnd, rayStart));
            b2Shape_SetSegment(guideSegmentId, new b2Segment(new b2Vec2(), localEnd));
            b2Body_Disable(guideBodyId);
            lastQuery = simple ? simpleCast(scene.worldId, rayStart, rayEnd)
                : cast(scene.worldId, castType, mode, rayStart, rayEnd, angle, radius);
            b2Body_Enable(guideBodyId);
        }

        private static b2ShapeDef ignoredShapeDef() {
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.userData = new BodyUserData(-1, true);
            shapeDef.filter.maskBits = 0L;
            return shapeDef;
        }

        @Override
        public void down(float worldX, float worldY, int button) {
            if (button == 0) {
                rayStart.x = worldX;
                rayStart.y = worldY;
                rayEnd.x = worldX;
                rayEnd.y = worldY;
                dragging = true;
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
                rayEnd.x = worldX;
                rayEnd.y = worldY;
            }
        }
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

    private static final class CastContext {
        final b2Vec2[] points = b2Vec2.array(3);
        final b2Vec2[] normals = b2Vec2.array(3);
        final float[] fractions = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        final int[] shapeIndices = {-1, -1, -1};
        int count;

        void set(int index, b2Vec2 point, b2Vec2 normal, float fraction, int shapeIndex) {
            points[index] = point.copy();
            normals[index] = normal.copy();
            fractions[index] = fraction;
            shapeIndices[index] = shapeIndex;
        }

        void copy(int to, int from) {
            points[to] = points[from].copy();
            normals[to] = normals[from].copy();
            fractions[to] = fractions[from];
            shapeIndices[to] = shapeIndices[from];
        }
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int awakeBodyCount;
        public final int ignoreIndex;
        public final QueryResult[] queries;
        public final BodyState[] bodies;

        Result(int bodyCount, int shapeCount, int contactCount, int awakeBodyCount, int ignoreIndex,
               QueryResult[] queries, BodyState[] bodies) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.ignoreIndex = ignoreIndex;
            this.queries = queries;
            this.bodies = bodies;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder("castWorld ").append(bodyCount).append(' ')
                .append(shapeCount).append(' ').append(contactCount).append(' ').append(awakeBodyCount)
                .append(' ').append(ignoreIndex).append(' ').append(queries.length);
            for (QueryResult query : queries) {
                builder.append(query.toLinePart());
            }
            builder.append(' ').append(bodies.length);
            for (BodyState body : bodies) {
                builder.append(body.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class QueryResult {
        public final int castType;
        public final int mode;
        public final float startX;
        public final float startY;
        public final float endX;
        public final float endY;
        public final float angle;
        public final float radius;
        public final int nodeVisits;
        public final int leafVisits;
        public final HitState[] hits;

        QueryResult(int castType, int mode, float startX, float startY, float endX, float endY,
                    float angle, float radius, int nodeVisits, int leafVisits, HitState[] hits) {
            this.castType = castType;
            this.mode = mode;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.angle = angle;
            this.radius = radius;
            this.nodeVisits = nodeVisits;
            this.leafVisits = leafVisits;
            this.hits = hits;
        }

        String toLinePart() {
            StringBuilder builder = new StringBuilder().append(' ').append(castType).append(' ').append(mode)
                .append(formatFloat(startX)).append(formatFloat(startY)).append(formatFloat(endX))
                .append(formatFloat(endY)).append(formatFloat(angle)).append(formatFloat(radius))
                .append(' ').append(nodeVisits).append(' ').append(leafVisits).append(' ').append(hits.length);
            for (HitState hit : hits) {
                builder.append(hit.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class HitState {
        public final float pointX;
        public final float pointY;
        public final float normalX;
        public final float normalY;
        public final float fraction;
        public final int shapeIndex;

        HitState(float pointX, float pointY, float normalX, float normalY, float fraction, int shapeIndex) {
            this.pointX = pointX;
            this.pointY = pointY;
            this.normalX = normalX;
            this.normalY = normalY;
            this.fraction = fraction;
            this.shapeIndex = shapeIndex;
        }

        String toLinePart() {
            return formatFloat(pointX) + formatFloat(pointY) + formatFloat(normalX) + formatFloat(normalY)
                + formatFloat(fraction) + " " + shapeIndex;
        }
    }

    public static final class BodyState {
        public final int type;
        public final float x;
        public final float y;
        public final float cos;
        public final float sin;

        BodyState(int type, float x, float y, float cos, float sin) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.cos = cos;
            this.sin = sin;
        }

        String toLinePart() {
            return " " + type + formatFloat(x) + formatFloat(y) + formatFloat(cos) + formatFloat(sin);
        }
    }

    private static String formatFloat(float value) {
        return " " + String.format(Locale.ROOT, "%.9g", value);
    }
}
