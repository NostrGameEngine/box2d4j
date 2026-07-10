package org.box2d4j.samples;

import org.box2d4j.b2CastOutput;
import org.box2d4j.b2DistanceInput;
import org.box2d4j.b2DistanceOutput;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeCastPairInput;
import org.box2d4j.b2ShapeProxy;
import org.box2d4j.b2SimplexCache;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ShapeCast {
    private ShapeCast() {
    }

    public static Result run() {
        if (SampleRuntime.isActive()) {
            InteractiveState state = new InteractiveState();
            String[] shapes = {"Point", "Segment", "Triangle", "Box"};
            SampleRuntime.choice("shapeCast.shapeA", "Shape A", state.typeA.ordinal(), shapes,
                value -> state.typeA = ShapeDistance.ShapeType.values()[value]);
            SampleRuntime.slider("shapeCast.radiusA", "Radius A", state.radiusA, 0.0f, 0.5f, 0.01f,
                value -> state.radiusA = value);
            SampleRuntime.choice("shapeCast.shapeB", "Shape B", state.typeB.ordinal(), shapes,
                value -> state.typeB = ShapeDistance.ShapeType.values()[value]);
            SampleRuntime.slider("shapeCast.radiusB", "Radius B", state.radiusB, 0.0f, 0.5f, 0.01f,
                value -> state.radiusB = value);
            SampleRuntime.slider("shapeCast.x", "X Offset", state.transform.p.x, -2.0f, 2.0f, 0.01f,
                value -> state.transform.p.x = value);
            SampleRuntime.slider("shapeCast.y", "Y Offset", state.transform.p.y, -2.0f, 2.0f, 0.01f,
                value -> state.transform.p.y = value);
            SampleRuntime.slider("shapeCast.angle", "Angle", state.angle, -B2_PI, B2_PI, 0.01f, value -> {
                state.angle = value;
                state.transform.q = b2MakeRot(value);
            });
            SampleRuntime.slider("shapeCast.translationX", "Sweep X", state.translation.x, -4.0f, 4.0f, 0.05f,
                value -> state.translation.x = value);
            SampleRuntime.slider("shapeCast.translationY", "Sweep Y", state.translation.y, -4.0f, 4.0f, 0.05f,
                value -> state.translation.y = value);
            SampleRuntime.toggle("shapeCast.indices", "Show Indices", false,
                value -> state.showIndices = value);
            SampleRuntime.toggle("shapeCast.encroach", "Encroach", false, value -> state.encroach = value);
            SampleRuntime.pointer(state);
            SampleRuntime.snapshot(state::snapshot);
            return state.snapshot();
        }
        return compute(ShapeDistance.ShapeType.BOX, ShapeDistance.ShapeType.POINT, 0.0f, 0.2f,
            new b2Transform(new b2Vec2(-0.6f, 0.0f), b2Rot_identity), new b2Vec2(2.0f, 0.0f), false);
    }

    private static Result compute(ShapeDistance.ShapeType typeA, ShapeDistance.ShapeType typeB,
                                  float radiusA, float radiusB, b2Transform transformB,
                                  b2Vec2 translation, boolean encroach) {
        b2ShapeProxy proxyA = makeProxy(typeA, radiusA);
        b2ShapeProxy proxyB = makeProxy(typeB, radiusB);

        b2ShapeCastPairInput input = new b2ShapeCastPairInput();
        input.proxyA = proxyA;
        input.proxyB = proxyB;
        input.transformA = b2Transform_identity.copy();
        input.transformB = transformB;
        input.translationB = translation;
        input.maxFraction = 1.0f;
        input.canEncroach = encroach;
        b2CastOutput output = b2ShapeCast(input);

        b2Transform hitTransform = new b2Transform();
        hitTransform.q = transformB.q.copy();
        hitTransform.p = b2MulAdd(transformB.p, output.fraction, input.translationB);

        b2DistanceInput distanceInput = new b2DistanceInput();
        distanceInput.proxyA = proxyA;
        distanceInput.proxyB = proxyB;
        distanceInput.transformA = b2Transform_identity.copy();
        distanceInput.transformB = hitTransform;
        distanceInput.useRadii = false;
        b2SimplexCache cache = new b2SimplexCache();
        b2DistanceOutput distance = b2ShapeDistance(distanceInput, cache, null, 0);

        return new Result(output.hit, output.fraction, output.point.x, output.point.y,
            output.normal.x, output.normal.y, output.iterations,
            hitTransform.p.x, hitTransform.p.y, hitTransform.q.c, hitTransform.q.s,
            distance.distance, distance.pointA.x, distance.pointA.y, distance.pointB.x,
            distance.pointB.y, distance.normal.x, distance.normal.y, distance.iterations,
            distance.simplexCount, cache.count, typeA, typeB, radiusA, radiusB,
            transformB.copy(), translation.copy());
    }

    private static b2ShapeProxy makeProxy(ShapeDistance.ShapeType type, float radius) {
        if (type == ShapeDistance.ShapeType.POINT) {
            return b2MakeProxy(new b2Vec2[] {new b2Vec2()}, 1, radius);
        }
        if (type == ShapeDistance.ShapeType.SEGMENT) {
            b2Segment segment = new b2Segment(new b2Vec2(0.0f, 0.0f), new b2Vec2(0.5f, 0.0f));
            return b2MakeProxy(new b2Vec2[] {segment.point1, segment.point2}, 2, radius);
        }
        if (type == ShapeDistance.ShapeType.TRIANGLE) {
            b2Vec2[] points = {new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f),
                new b2Vec2(0.0f, 1.0f)};
            b2Hull hull = b2ComputeHull(points, points.length);
            b2Polygon triangle = b2MakePolygon(hull, 0.0f);
            return b2MakeProxy(triangle.vertices, triangle.count, radius);
        }
        b2Polygon box = b2MakeOffsetBox(0.5f, 0.5f, new b2Vec2(), b2Rot_identity);
        return b2MakeProxy(box.vertices, box.count, radius);
    }

    private static final class InteractiveState implements SampleRuntime.PointerHandler {
        final b2Transform transform = new b2Transform(new b2Vec2(-0.6f, 0.0f), b2Rot_identity);
        final b2Vec2 translation = new b2Vec2(2.0f, 0.0f);
        ShapeDistance.ShapeType typeA = ShapeDistance.ShapeType.BOX;
        ShapeDistance.ShapeType typeB = ShapeDistance.ShapeType.POINT;
        float radiusA;
        float radiusB = 0.2f;
        float angle;
        boolean showIndices;
        boolean encroach;
        boolean dragging;
        float dragOffsetX;
        float dragOffsetY;

        Result snapshot() {
            return compute(typeA, typeB, radiusA, radiusB, transform, translation, encroach);
        }

        @Override
        public void down(float worldX, float worldY, int button) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = transform.p.x - worldX;
                dragOffsetY = transform.p.y - worldY;
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
                transform.p.x = worldX + dragOffsetX;
                transform.p.y = worldY + dragOffsetY;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final boolean hit;
        public final float fraction;
        public final float pointX;
        public final float pointY;
        public final float normalX;
        public final float normalY;
        public final int iterations;
        public final float transformX;
        public final float transformY;
        public final float transformCos;
        public final float transformSin;
        public final float distance;
        public final float pointAX;
        public final float pointAY;
        public final float pointBX;
        public final float pointBY;
        public final float distanceNormalX;
        public final float distanceNormalY;
        public final int distanceIterations;
        public final int simplexCount;
        public final int cacheCount;
        public final ShapeDistance.ShapeType typeA;
        public final ShapeDistance.ShapeType typeB;
        public final float radiusA;
        public final float radiusB;
        public final b2Transform startTransform;
        public final b2Vec2 translation;

        Result(boolean hit, float fraction, float pointX, float pointY, float normalX, float normalY,
               int iterations, float transformX, float transformY, float transformCos, float transformSin,
               float distance, float pointAX, float pointAY, float pointBX, float pointBY,
               float distanceNormalX, float distanceNormalY, int distanceIterations, int simplexCount,
               int cacheCount, ShapeDistance.ShapeType typeA, ShapeDistance.ShapeType typeB,
               float radiusA, float radiusB, b2Transform startTransform, b2Vec2 translation) {
            this.hit = hit;
            this.fraction = fraction;
            this.pointX = pointX;
            this.pointY = pointY;
            this.normalX = normalX;
            this.normalY = normalY;
            this.iterations = iterations;
            this.transformX = transformX;
            this.transformY = transformY;
            this.transformCos = transformCos;
            this.transformSin = transformSin;
            this.distance = distance;
            this.pointAX = pointAX;
            this.pointAY = pointAY;
            this.pointBX = pointBX;
            this.pointBY = pointBY;
            this.distanceNormalX = distanceNormalX;
            this.distanceNormalY = distanceNormalY;
            this.distanceIterations = distanceIterations;
            this.simplexCount = simplexCount;
            this.cacheCount = cacheCount;
            this.typeA = typeA;
            this.typeB = typeB;
            this.radiusA = radiusA;
            this.radiusB = radiusB;
            this.startTransform = startTransform;
            this.translation = translation;
        }

        public String toLine() {
            return "shapeCast " + (hit ? 1 : 0) + floats(fraction, pointX, pointY, normalX, normalY)
                + " " + iterations + floats(transformX, transformY, transformCos, transformSin, distance,
                pointAX, pointAY, pointBX, pointBY, distanceNormalX, distanceNormalY)
                + " " + distanceIterations + " " + simplexCount + " " + cacheCount;
        }
    }

    private static String floats(float... values) {
        StringBuilder builder = new StringBuilder();
        for (float value : values) {
            builder.append(' ').append(String.format(Locale.ROOT, "%.9g", value));
        }
        return builder.toString();
    }
}
