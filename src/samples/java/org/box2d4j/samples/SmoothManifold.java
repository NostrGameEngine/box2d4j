package org.box2d4j.samples;

import org.box2d4j.b2ChainSegment;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Manifold;
import org.box2d4j.b2ManifoldPoint;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2SimplexCache;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class SmoothManifold {
    public static final int CIRCLE_SHAPE = 0;
    public static final int BOX_SHAPE = 1;

    private SmoothManifold() {
    }

    public static Result run() {
        b2Vec2[] points = points();
        b2ChainSegment[] segments = new b2ChainSegment[points.length];
        for (int i = 0; i < points.length; ++i) {
            int i0 = i > 0 ? i - 1 : points.length - 1;
            int i2 = i < points.length - 1 ? i + 1 : 0;
            int i3 = i2 < points.length - 1 ? i2 + 1 : 0;
            b2ChainSegment segment = new b2ChainSegment();
            segment.ghost1 = points[i0].copy();
            segment.segment = new b2Segment(points[i], points[i2]);
            segment.ghost2 = points[i3].copy();
            segment.chainId = -1;
            segments[i] = segment;
        }

        if (SampleRuntime.isActive()) {
            InteractiveState state = new InteractiveState(segments);
            SampleRuntime.choice("smoothManifold.shape", "Shape", state.shapeType,
                new String[] {"Circle", "Box"}, value -> state.shapeType = value);
            SampleRuntime.slider("smoothManifold.x", "X Offset", state.position.x, -30.0f, 30.0f, 0.05f,
                value -> state.position.x = value);
            SampleRuntime.slider("smoothManifold.y", "Y Offset", state.position.y, 0.0f, 35.0f, 0.05f,
                value -> state.position.y = value);
            SampleRuntime.slider("smoothManifold.angle", "Angle", state.angle, -B2_PI, B2_PI, 0.01f,
                value -> state.angle = value);
            SampleRuntime.slider("smoothManifold.round", "Round", state.round, 0.0f, 0.4f, 0.1f,
                value -> state.round = value);
            SampleRuntime.toggle("smoothManifold.ids", "Show Ids", false, value -> state.showIds = value);
            SampleRuntime.toggle("smoothManifold.separation", "Show Separation", false,
                value -> state.showSeparation = value);
            SampleRuntime.toggle("smoothManifold.anchors", "Show Anchors", false,
                value -> state.showAnchors = value);
            SampleRuntime.action("smoothManifold.reset", "Reset", state::reset);
            SampleRuntime.pointer(state);
            SampleRuntime.snapshot(state::snapshot);
            return state.snapshot();
        }

        CaseResult[] cases = {
            runCase(segments, BOX_SHAPE, new b2Vec2(0.0f, 20.0f), 0.0f, 0.0f),
            runCase(segments, BOX_SHAPE, new b2Vec2(-7.35f, 20.8f), 0.2f, 0.1f),
            runCase(segments, CIRCLE_SHAPE, new b2Vec2(-7.35f, 20.8f), 0.0f, 0.0f)
        };
        return new Result(cases);
    }

    private static final class InteractiveState implements SampleRuntime.PointerHandler {
        final b2ChainSegment[] segments;
        final b2Vec2 position = new b2Vec2(0.0f, 20.0f);
        int shapeType = BOX_SHAPE;
        float angle;
        float round;
        boolean showIds;
        boolean showSeparation;
        boolean showAnchors;
        boolean dragging;
        float dragOffsetX;
        float dragOffsetY;

        InteractiveState(b2ChainSegment[] segments) {
            this.segments = segments;
        }

        Result snapshot() {
            return new Result(new CaseResult[] {runCase(segments, shapeType, position.copy(), angle, round)});
        }

        void reset() {
            position.x = 0.0f;
            position.y = 20.0f;
            angle = 0.0f;
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

    private static CaseResult runCase(b2ChainSegment[] segments, int shapeType, b2Vec2 position,
                                      float angle, float round) {
        b2Transform transform1 = b2Transform_identity.copy();
        b2Transform transform2 = new b2Transform(position, b2MakeRot(angle));
        ManifoldState[] manifolds = new ManifoldState[segments.length];
        for (int i = 0; i < segments.length; ++i) {
            b2Manifold manifold;
            if (shapeType == CIRCLE_SHAPE) {
                manifold = b2CollideChainSegmentAndCircle(segments[i], transform1,
                    new b2Circle(new b2Vec2(), 0.5f), transform2);
            } else {
                float h = 0.5f - round;
                b2Polygon box = b2MakeRoundedBox(h, h, round);
                manifold = b2CollideChainSegmentAndPolygon(segments[i], transform1, box,
                    transform2, new b2SimplexCache());
            }
            manifolds[i] = manifoldState(manifold);
        }
        return new CaseResult(shapeType, position.x, position.y, angle, round, manifolds);
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

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final CaseResult[] cases;

        Result(CaseResult[] cases) {
            this.cases = cases;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder("smoothManifold ").append(cases.length);
            for (CaseResult value : cases) {
                builder.append(value.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class CaseResult {
        public final int shapeType;
        public final float x;
        public final float y;
        public final float angle;
        public final float round;
        public final ManifoldState[] manifolds;

        CaseResult(int shapeType, float x, float y, float angle, float round, ManifoldState[] manifolds) {
            this.shapeType = shapeType;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.round = round;
            this.manifolds = manifolds;
        }

        String toLinePart() {
            StringBuilder builder = new StringBuilder().append(' ').append(shapeType)
                .append(formatFloat(x)).append(formatFloat(y)).append(formatFloat(angle))
                .append(formatFloat(round)).append(' ').append(manifolds.length);
            for (ManifoldState manifold : manifolds) {
                builder.append(manifold.toLinePart());
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

    private static String formatFloat(float value) {
        return " " + String.format(Locale.ROOT, "%.9g", value);
    }

    private static b2Vec2[] points() {
        return new b2Vec2[] {
            new b2Vec2(-20.58325f, 14.54175f), new b2Vec2(-21.90625f, 15.8645f),
            new b2Vec2(-24.552f, 17.1875f), new b2Vec2(-27.198f, 11.89575f),
            new b2Vec2(-29.84375f, 15.8645f), new b2Vec2(-29.84375f, 21.15625f),
            new b2Vec2(-25.875f, 23.802f), new b2Vec2(-20.58325f, 25.125f),
            new b2Vec2(-25.875f, 29.09375f), new b2Vec2(-20.58325f, 31.7395f),
            new b2Vec2(-11.0089998f, 23.2290001f), new b2Vec2(-8.67700005f, 21.15625f),
            new b2Vec2(-6.03125f, 21.15625f), new b2Vec2(-7.35424995f, 29.09375f),
            new b2Vec2(-3.38549995f, 29.09375f), new b2Vec2(1.90625f, 30.41675f),
            new b2Vec2(5.875f, 17.1875f), new b2Vec2(11.16675f, 25.125f),
            new b2Vec2(9.84375f, 29.09375f), new b2Vec2(13.8125f, 31.7395f),
            new b2Vec2(21.75f, 30.41675f), new b2Vec2(28.3644981f, 26.448f),
            new b2Vec2(25.71875f, 18.5105f), new b2Vec2(24.3957481f, 13.21875f),
            new b2Vec2(17.78125f, 11.89575f), new b2Vec2(15.1355f, 7.92700005f),
            new b2Vec2(5.875f, 9.25f), new b2Vec2(1.90625f, 11.89575f),
            new b2Vec2(-3.25f, 11.89575f), new b2Vec2(-3.25f, 9.9375f),
            new b2Vec2(-4.70825005f, 9.25f), new b2Vec2(-8.67700005f, 9.25f),
            new b2Vec2(-11.323f, 11.89575f), new b2Vec2(-13.96875f, 11.89575f),
            new b2Vec2(-15.29175f, 14.54175f), new b2Vec2(-19.2605f, 14.54175f)
        };
    }
}
