package org.box2d4j.samples;

import org.box2d4j.b2Capsule;
import org.box2d4j.b2ChainSegment;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Manifold;
import org.box2d4j.b2ManifoldPoint;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2SimplexCache;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ManifoldSample {
    private ManifoldSample() {
    }

    public static Result run() {
        if (SampleRuntime.isActive()) {
            InteractiveState state = new InteractiveState();
            SampleRuntime.slider("manifold.x", "X Offset", state.position.x, -2.0f, 2.0f, 0.01f,
                value -> state.position.x = value);
            SampleRuntime.slider("manifold.y", "Y Offset", state.position.y, -2.0f, 2.0f, 0.01f,
                value -> state.position.y = value);
            SampleRuntime.slider("manifold.angle", "Angle", state.angle, -B2_PI, B2_PI, 0.01f,
                value -> state.angle = value);
            SampleRuntime.slider("manifold.round", "Round", state.round, 0.0f, 0.4f, 0.1f,
                value -> state.round = value);
            SampleRuntime.toggle("manifold.count", "Show Count", false, value -> state.showCount = value);
            SampleRuntime.toggle("manifold.ids", "Show Ids", false, value -> state.showIds = value);
            SampleRuntime.toggle("manifold.separation", "Show Separation", false,
                value -> state.showSeparation = value);
            SampleRuntime.toggle("manifold.anchors", "Show Anchors", false,
                value -> state.showAnchors = value);
            SampleRuntime.toggle("manifold.cache", "Enable Caching", true, value -> {
                state.enableCaching = value;
                state.caches = new Caches();
            });
            SampleRuntime.action("manifold.reset", "Reset", state::reset);
            SampleRuntime.pointer(state);
            SampleRuntime.snapshot(state::snapshot);
            return state.snapshot();
        }
        Caches caches = new Caches();
        PassResult[] passes = {computePass(caches), computePass(caches)};
        return new Result(passes);
    }

    private static PassResult computePass(Caches caches) {
        return computePass(caches, new b2Transform(new b2Vec2(0.17f, 1.12f), b2Rot_identity), 0.1f);
    }

    private static PassResult computePass(Caches caches, b2Transform relative, float round) {
        List<b2Manifold> manifolds = new ArrayList<>();
        b2Vec2 offset = new b2Vec2(-10.0f, -5.0f);
        b2Vec2 increment = new b2Vec2(4.0f, 0.0f);

        b2Circle circle1 = new b2Circle(new b2Vec2(), 0.5f);
        b2Circle circle2 = new b2Circle(new b2Vec2(), 1.0f);
        manifolds.add(b2CollideCircles(circle1, transform(offset), circle2, transform(relative, offset)));
        offset = b2Add(offset, increment);

        b2Capsule capsule1 = new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f);
        b2Circle circle = new b2Circle(new b2Vec2(), 0.5f);
        manifolds.add(b2CollideCapsuleAndCircle(capsule1, transform(offset), circle, transform(relative, offset)));
        offset = b2Add(offset, increment);

        b2Segment segment = new b2Segment(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f));
        manifolds.add(b2CollideSegmentAndCircle(segment, transform(offset), circle, transform(relative, offset)));
        offset = b2Add(offset, increment);

        b2Polygon box = b2MakeSquare(0.5f);
        box.radius = round;
        manifolds.add(b2CollidePolygonAndCircle(box, transform(offset), circle, transform(relative, offset)));
        offset = b2Add(offset, increment);

        b2Capsule capsule2 = new b2Capsule(new b2Vec2(0.25f, 0.0f), new b2Vec2(1.0f, 0.0f), 0.1f);
        manifolds.add(b2CollideCapsules(capsule1, transform(offset), capsule2, transform(relative, offset)));
        offset = b2Add(offset, increment);

        b2Capsule shortCapsule = new b2Capsule(new b2Vec2(-0.4f, 0.0f), new b2Vec2(-0.1f, 0.0f), 0.1f);
        box = b2MakeOffsetBox(0.25f, 1.0f, new b2Vec2(1.0f, -1.0f), b2MakeRot(0.25f * B2_PI));
        manifolds.add(b2CollidePolygonAndCapsule(box, transform(offset), shortCapsule, transform(relative, offset)));
        offset = b2Add(offset, increment);

        manifolds.add(b2CollideSegmentAndCapsule(segment, transform(offset), capsule1, transform(relative, offset)));

        offset = new b2Vec2(-10.0f, 0.0f);
        b2Polygon square1 = b2MakeSquare(0.5f);
        b2Polygon square2 = b2MakeSquare(0.5f);
        manifolds.add(b2CollidePolygons(square1, transform(offset), square2, transform(relative, offset)));
        offset = b2Add(offset, increment);

        b2Polygon longBox = b2MakeBox(2.0f, 0.1f);
        b2Polygon smallBox = b2MakeSquare(0.25f);
        manifolds.add(b2CollidePolygons(longBox, transform(offset), smallBox, transform(relative, offset)));
        offset = b2Add(offset, increment);

        square1 = b2MakeSquare(0.5f);
        float h = 0.5f - round;
        b2Polygon roundedBox = b2MakeRoundedBox(h, h, round);
        manifolds.add(b2CollidePolygons(square1, transform(offset), roundedBox, transform(relative, offset)));
        offset = b2Add(offset, increment);

        manifolds.add(b2CollidePolygons(roundedBox, transform(offset), roundedBox, transform(relative, offset)));
        offset = b2Add(offset, increment);

        manifolds.add(b2CollideSegmentAndPolygon(segment, transform(offset), roundedBox, transform(relative, offset)));
        offset = b2Add(offset, increment);

        b2Vec2[] wedgePoints = {
            new b2Vec2(-0.1f, -0.5f), new b2Vec2(0.1f, -0.5f), new b2Vec2(0.0f, 0.5f)
        };
        b2Hull wedge = b2ComputeHull(wedgePoints, wedgePoints.length);
        b2Polygon wox = b2MakePolygon(wedge, round);
        manifolds.add(b2CollidePolygons(wox, transform(offset), wox, transform(relative, offset)));
        offset = b2Add(offset, increment);

        b2Vec2[] p1s = {
            new b2Vec2(0.175740838f, 0.224936664f), new b2Vec2(-0.301293969f, 0.194021404f),
            new b2Vec2(-0.105151534f, -0.432157338f)
        };
        b2Vec2[] p2s = {
            new b2Vec2(-0.427884758f, -0.225028217f), new b2Vec2(0.0566576123f, -0.128772855f),
            new b2Vec2(0.176625848f, 0.338923335f)
        };
        b2Polygon w1 = b2MakePolygon(b2ComputeHull(p1s, p1s.length), 0.158798501f);
        b2Polygon w2 = b2MakePolygon(b2ComputeHull(p2s, p2s.length), 0.205900759f);
        manifolds.add(b2CollidePolygons(w1, transform(offset), w2, transform(relative, offset)));

        offset = new b2Vec2(-10.0f, 5.0f);
        b2Polygon bigBox = b2MakeBox(1.0f, 1.0f);
        b2Vec2[] trianglePoints = {
            new b2Vec2(-0.05f, 0.0f), new b2Vec2(0.05f, 0.0f), new b2Vec2(0.0f, 0.1f)
        };
        b2Polygon triangle = b2MakePolygon(b2ComputeHull(trianglePoints, trianglePoints.length), 0.0f);
        manifolds.add(b2CollidePolygons(bigBox, transform(offset), triangle, transform(relative, offset)));
        offset = b2Add(offset, increment);

        b2ChainSegment chain1 = chainSegment(new b2Vec2(2.0f, 1.0f),
            new b2Vec2(1.0f, 1.0f), new b2Vec2(-1.0f, 0.0f), new b2Vec2(-2.0f, 0.0f));
        manifolds.add(b2CollideChainSegmentAndCircle(chain1, transform(offset), circle, transform(relative, offset)));
        offset.x += 2.0f * increment.x;

        b2ChainSegment chain2 = chainSegment(new b2Vec2(3.0f, 1.0f),
            new b2Vec2(2.0f, 1.0f), new b2Vec2(1.0f, 1.0f), new b2Vec2(-1.0f, 0.0f));
        manifolds.add(b2CollideChainSegmentAndPolygon(chain1, transform(offset), roundedBox,
            transform(relative, offset), caches.rounded1));
        manifolds.add(b2CollideChainSegmentAndPolygon(chain2, transform(offset), roundedBox,
            transform(relative, offset), caches.rounded2));
        offset.x += 2.0f * increment.x;

        manifolds.add(b2CollideChainSegmentAndCapsule(chain1, transform(offset), capsule1,
            transform(relative, offset), caches.capsule1));
        manifolds.add(b2CollideChainSegmentAndCapsule(chain2, transform(offset), capsule1,
            transform(relative, offset), caches.capsule2));

        ManifoldState[] states = new ManifoldState[manifolds.size()];
        for (int i = 0; i < states.length; ++i) {
            states[i] = manifoldState(manifolds.get(i));
        }
        CacheState[] cacheStates = {
            cacheState(caches.rounded1), cacheState(caches.rounded2),
            cacheState(caches.capsule1), cacheState(caches.capsule2)
        };
        return new PassResult(states, cacheStates);
    }

    private static final class InteractiveState implements SampleRuntime.PointerHandler {
        final b2Vec2 position = new b2Vec2(0.17f, 1.12f);
        float angle;
        float round = 0.1f;
        boolean showCount;
        boolean showIds;
        boolean showSeparation;
        boolean showAnchors;
        boolean enableCaching = true;
        boolean dragging;
        float dragOffsetX;
        float dragOffsetY;
        Caches caches = new Caches();

        Result snapshot() {
            Caches nextCaches = enableCaching ? caches : new Caches();
            PassResult pass = computePass(nextCaches,
                new b2Transform(position.copy(), b2MakeRot(angle)), round);
            return new Result(new PassResult[] {pass});
        }

        void reset() {
            position.x = 0.17f;
            position.y = 1.12f;
            angle = 0.0f;
            caches = new Caches();
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

    private static b2Transform transform(b2Vec2 offset) {
        return new b2Transform(offset, b2Rot_identity);
    }

    private static b2Transform transform(b2Transform relative, b2Vec2 offset) {
        return new b2Transform(b2Add(relative.p, offset), relative.q);
    }

    private static b2ChainSegment chainSegment(b2Vec2 ghost1, b2Vec2 point1, b2Vec2 point2, b2Vec2 ghost2) {
        b2ChainSegment segment = new b2ChainSegment();
        segment.ghost1 = ghost1.copy();
        segment.segment = new b2Segment(point1, point2);
        segment.ghost2 = ghost2.copy();
        segment.chainId = -1;
        return segment;
    }

    private static ManifoldState manifoldState(b2Manifold manifold) {
        PointState[] points = new PointState[manifold.pointCount];
        for (int i = 0; i < manifold.pointCount; ++i) {
            b2ManifoldPoint point = manifold.points[i];
            points[i] = new PointState(point.point.x, point.point.y, point.anchorA.x, point.anchorA.y,
                point.anchorB.x, point.anchorB.y, point.separation, point.id);
        }
        return new ManifoldState(manifold.normal.x, manifold.normal.y, manifold.rollingImpulse, points);
    }

    private static CacheState cacheState(b2SimplexCache cache) {
        int[] indexA = new int[3];
        int[] indexB = new int[3];
        for (int i = 0; i < 3; ++i) {
            indexA[i] = Byte.toUnsignedInt(cache.indexA[i]);
            indexB[i] = Byte.toUnsignedInt(cache.indexB[i]);
        }
        return new CacheState(cache.count, indexA, indexB);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    private static final class Caches {
        final b2SimplexCache rounded1 = new b2SimplexCache();
        final b2SimplexCache rounded2 = new b2SimplexCache();
        final b2SimplexCache capsule1 = new b2SimplexCache();
        final b2SimplexCache capsule2 = new b2SimplexCache();
    }

    public static final class Result {
        public final PassResult[] passes;

        Result(PassResult[] passes) {
            this.passes = passes;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder("manifold ").append(passes.length);
            for (PassResult pass : passes) {
                builder.append(pass.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class PassResult {
        public final ManifoldState[] manifolds;
        public final CacheState[] caches;

        PassResult(ManifoldState[] manifolds, CacheState[] caches) {
            this.manifolds = manifolds;
            this.caches = caches;
        }

        String toLinePart() {
            StringBuilder builder = new StringBuilder().append(' ').append(manifolds.length);
            for (ManifoldState manifold : manifolds) {
                builder.append(manifold.toLinePart());
            }
            builder.append(' ').append(caches.length);
            for (CacheState cache : caches) {
                builder.append(cache.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class ManifoldState {
        public final float normalX;
        public final float normalY;
        public final float rollingImpulse;
        public final PointState[] points;

        ManifoldState(float normalX, float normalY, float rollingImpulse, PointState[] points) {
            this.normalX = normalX;
            this.normalY = normalY;
            this.rollingImpulse = rollingImpulse;
            this.points = points;
        }

        String toLinePart() {
            StringBuilder builder = new StringBuilder().append(' ').append(points.length)
                .append(formatFloat(normalX)).append(formatFloat(normalY)).append(formatFloat(rollingImpulse));
            for (PointState point : points) {
                builder.append(point.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class PointState {
        public final float pointX;
        public final float pointY;
        public final float anchorAX;
        public final float anchorAY;
        public final float anchorBX;
        public final float anchorBY;
        public final float separation;
        public final int id;

        PointState(float pointX, float pointY, float anchorAX, float anchorAY, float anchorBX,
                   float anchorBY, float separation, int id) {
            this.pointX = pointX;
            this.pointY = pointY;
            this.anchorAX = anchorAX;
            this.anchorAY = anchorAY;
            this.anchorBX = anchorBX;
            this.anchorBY = anchorBY;
            this.separation = separation;
            this.id = id;
        }

        String toLinePart() {
            return formatFloat(pointX) + formatFloat(pointY) + formatFloat(anchorAX) + formatFloat(anchorAY)
                + formatFloat(anchorBX) + formatFloat(anchorBY) + formatFloat(separation) + " " + id;
        }
    }

    public static final class CacheState {
        public final int count;
        public final int[] indexA;
        public final int[] indexB;

        CacheState(int count, int[] indexA, int[] indexB) {
            this.count = count;
            this.indexA = indexA;
            this.indexB = indexB;
        }

        String toLinePart() {
            return " " + count + " " + indexA[0] + " " + indexA[1] + " " + indexA[2]
                + " " + indexB[0] + " " + indexB[1] + " " + indexB[2];
        }
    }

    private static String formatFloat(float value) {
        return " " + String.format(Locale.ROOT, "%.9g", value);
    }
}
