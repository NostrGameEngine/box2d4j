package org.box2d4j.samples;

import org.box2d4j.b2DistanceInput;
import org.box2d4j.b2DistanceOutput;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeProxy;
import org.box2d4j.b2Simplex;
import org.box2d4j.b2SimplexCache;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ShapeDistance {
    private final b2Vec2 point;
    private final b2Segment segment;
    private final b2Polygon triangle;
    private final b2Polygon box;

    private ShapeDistance() {
        point = b2Vec2_zero.copy();
        segment = new b2Segment(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f));

        b2Vec2[] points = {
            new b2Vec2(-0.5f, 0.0f),
            new b2Vec2(0.5f, 0.0f),
            new b2Vec2(0.0f, 1.0f)
        };
        b2Hull hull = b2ComputeHull(points, 3);
        triangle = b2MakePolygon(hull, 0.0f);
        box = b2MakeSquare(0.5f);
    }

    public static Result run() {
        ShapeDistance sample = new ShapeDistance();
        if (SampleRuntime.isActive()) {
            InteractiveState state = new InteractiveState(sample);
            SampleRuntime.choice("shapeDistance.shapeA", "Shape A", state.typeA.ordinal(), shapeNames(),
                value -> state.typeA = ShapeType.values()[value]);
            SampleRuntime.slider("shapeDistance.radiusA", "Radius A", state.radiusA, 0.0f, 0.5f, 0.01f,
                value -> state.radiusA = value);
            SampleRuntime.choice("shapeDistance.shapeB", "Shape B", state.typeB.ordinal(), shapeNames(),
                value -> state.typeB = ShapeType.values()[value]);
            SampleRuntime.slider("shapeDistance.radiusB", "Radius B", state.radiusB, 0.0f, 0.5f, 0.01f,
                value -> state.radiusB = value);
            SampleRuntime.slider("shapeDistance.x", "X Offset", state.position.x, -2.0f, 2.0f, 0.01f,
                value -> state.position.x = value);
            SampleRuntime.slider("shapeDistance.y", "Y Offset", state.position.y, -2.0f, 2.0f, 0.01f,
                value -> state.position.y = value);
            SampleRuntime.slider("shapeDistance.angle", "Angle", state.angle, -B2_PI, B2_PI, 0.01f,
                value -> state.angle = value);
            SampleRuntime.toggle("shapeDistance.indices", "Show Indices", state.showIndices,
                value -> state.showIndices = value);
            SampleRuntime.toggle("shapeDistance.cache", "Use Cache", state.useCache,
                value -> {
                    state.useCache = value;
                    state.cache = new b2SimplexCache();
                });
            SampleRuntime.toggle("shapeDistance.simplex", "Draw Simplex", state.drawSimplex,
                value -> state.drawSimplex = value);
            SampleRuntime.integer("shapeDistance.simplexIndex", "Simplex Index", state.simplexIndex,
                0, 19, 1, value -> state.simplexIndex = value);
            SampleRuntime.pointer(state);
            SampleRuntime.snapshot(state::snapshot);
            return state.snapshot();
        }
        return new Result(new CaseResult[] {
            sample.runCase("default", ShapeType.BOX, ShapeType.BOX, 0.0f, 0.0f, b2Vec2_zero.copy(), 0.0f),
            sample.runCase("mixed", ShapeType.TRIANGLE, ShapeType.SEGMENT, 0.05f, 0.1f, new b2Vec2(0.75f, -0.25f), 0.35f)
        });
    }

    private CaseResult runCase(String label, ShapeType typeA, ShapeType typeB, float radiusA, float radiusB,
                               b2Vec2 position, float angle) {
        b2DistanceInput input = new b2DistanceInput();
        input.proxyA = makeProxy(typeA, radiusA);
        input.proxyB = makeProxy(typeB, radiusB);
        input.transformA = b2Transform_identity;
        input.transformB = new b2Transform(position, b2MakeRot(angle));
        input.useRadii = true;

        b2SimplexCache cache = new b2SimplexCache();
        b2Simplex[] simplexes = new b2Simplex[20];
        b2DistanceOutput output = b2ShapeDistance(input, cache, simplexes, simplexes.length);

        return new CaseResult(label, typeA, typeB, radiusA, radiusB, position, angle,
            input.proxyA.count, input.proxyB.count, cache, output);
    }

    private b2ShapeProxy makeProxy(ShapeType type, float radius) {
        switch (type) {
            case POINT:
                return b2MakeProxy(new b2Vec2[] {point}, 1, radius);
            case SEGMENT:
                return b2MakeProxy(new b2Vec2[] {segment.point1, segment.point2}, 2, radius);
            case TRIANGLE:
                return b2MakeProxy(triangle.vertices, triangle.count, radius);
            case BOX:
                return b2MakeProxy(box.vertices, box.count, radius);
            default:
                throw new IllegalArgumentException("Unknown shape type: " + type);
        }
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public enum ShapeType {
        POINT,
        SEGMENT,
        TRIANGLE,
        BOX
    }

    private static String[] shapeNames() {
        return new String[] {"Point", "Segment", "Triangle", "Box"};
    }

    private static final class InteractiveState implements SampleRuntime.PointerHandler {
        final ShapeDistance sample;
        final b2Vec2 position = new b2Vec2(0.75f, -0.25f);
        ShapeType typeA = ShapeType.BOX;
        ShapeType typeB = ShapeType.BOX;
        float radiusA;
        float radiusB;
        float angle;
        boolean showIndices;
        boolean useCache;
        boolean drawSimplex;
        int simplexIndex;
        boolean dragging;
        float dragOffsetX;
        float dragOffsetY;
        b2SimplexCache cache = new b2SimplexCache();

        InteractiveState(ShapeDistance sample) {
            this.sample = sample;
        }

        Result snapshot() {
            b2DistanceInput input = new b2DistanceInput();
            input.proxyA = sample.makeProxy(typeA, radiusA);
            input.proxyB = sample.makeProxy(typeB, radiusB);
            input.transformA = b2Transform_identity;
            input.transformB = new b2Transform(position.copy(), b2MakeRot(angle));
            input.useRadii = true;
            b2SimplexCache nextCache = useCache ? cache : new b2SimplexCache();
            b2Simplex[] simplexes = new b2Simplex[20];
            b2DistanceOutput output = b2ShapeDistance(input, nextCache, simplexes, simplexes.length);
            if (useCache) {
                cache = nextCache;
            }
            return new Result(new CaseResult[] {new CaseResult("interactive", typeA, typeB, radiusA, radiusB,
                position.copy(), angle, input.proxyA.count, input.proxyB.count, nextCache, output)});
        }

        @Override
        public void down(float worldX, float worldY, int button) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = position.x - worldX;
                dragOffsetY = position.y - worldY;
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
                position.x = worldX + dragOffsetX;
                position.y = worldY + dragOffsetY;
            }
        }
    }

    public static final class Result {
        public final CaseResult[] cases;

        public Result(CaseResult[] cases) {
            this.cases = cases;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("shapeDistance ").append(cases.length);
            for (CaseResult result : cases) {
                builder.append(result.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class CaseResult {
        public final String label;
        public final ShapeType typeA;
        public final ShapeType typeB;
        public final float radiusA;
        public final float radiusB;
        public final b2Vec2 position;
        public final float angle;
        public final int proxyACount;
        public final int proxyBCount;
        public final b2SimplexCache cache;
        public final b2DistanceOutput output;

        CaseResult(String label, ShapeType typeA, ShapeType typeB, float radiusA, float radiusB, b2Vec2 position,
                   float angle, int proxyACount, int proxyBCount, b2SimplexCache cache, b2DistanceOutput output) {
            this.label = label;
            this.typeA = typeA;
            this.typeB = typeB;
            this.radiusA = radiusA;
            this.radiusB = radiusB;
            this.position = position;
            this.angle = angle;
            this.proxyACount = proxyACount;
            this.proxyBCount = proxyBCount;
            this.cache = cache;
            this.output = output;
        }

        String toLinePart() {
            StringBuilder builder = new StringBuilder();
            builder.append(' ')
                .append(label).append(' ')
                .append(typeA.ordinal()).append(' ')
                .append(typeB.ordinal()).append(' ')
                .append(formatFloat(radiusA)).append(' ')
                .append(formatFloat(radiusB));
            appendVec(builder, position);
            builder.append(' ')
                .append(formatFloat(angle)).append(' ')
                .append(proxyACount).append(' ')
                .append(proxyBCount).append(' ')
                .append(cache.count);
            for (int i = 0; i < 3; ++i) {
                builder.append(' ').append(Byte.toUnsignedInt(cache.indexA[i]));
            }
            for (int i = 0; i < 3; ++i) {
                builder.append(' ').append(Byte.toUnsignedInt(cache.indexB[i]));
            }
            builder.append(' ').append(formatFloat(output.distance));
            appendVec(builder, output.normal);
            appendVec(builder, output.pointA);
            appendVec(builder, output.pointB);
            builder.append(' ')
                .append(output.iterations).append(' ')
                .append(output.simplexCount);
            return builder.toString();
        }
    }

    private static void appendVec(StringBuilder builder, b2Vec2 v) {
        builder.append(' ').append(formatFloat(v.x)).append(' ').append(formatFloat(v.y));
    }

    private static String formatFloat(float value) {
        if (value == 0.0f) {
            return "0";
        }
        String text = String.format(Locale.ROOT, "%.9g", value);
        if (text.indexOf('e') < 0 && text.indexOf('E') < 0 && text.indexOf('.') >= 0) {
            while (text.endsWith("0")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text;
    }
}
