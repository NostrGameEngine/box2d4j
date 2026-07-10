package org.box2d4j.samples;

import org.box2d4j.b2Capsule;
import org.box2d4j.b2CastOutput;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Hull;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RayCastInput;
import org.box2d4j.b2Segment;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class RayCast {
    private RayCast() {
    }

    public static Result run() {
        if (SampleRuntime.isActive()) {
            InteractiveState state = new InteractiveState();
            SampleRuntime.slider("rayCast.x", "X Offset", 0.0f, -2.0f, 2.0f, 0.01f,
                value -> state.transform.p.x = value);
            SampleRuntime.slider("rayCast.y", "Y Offset", 0.0f, -2.0f, 2.0f, 0.01f,
                value -> state.transform.p.y = value);
            SampleRuntime.slider("rayCast.angle", "Angle", 0.0f, -B2_PI, B2_PI, 0.01f, value -> {
                state.angle = value;
                state.transform.q = b2MakeRot(value);
            });
            SampleRuntime.toggle("rayCast.fraction", "Show Fraction", false,
                value -> state.showFraction = value);
            SampleRuntime.action("rayCast.reset", "Reset", state::reset);
            SampleRuntime.pointer(state);
            SampleRuntime.snapshot(state::snapshot);
            return state.snapshot();
        }
        return compute(b2Transform_identity.copy(), new b2Vec2(0.0f, 30.0f), new b2Vec2(0.0f, 0.0f), false);
    }

    private static Result compute(b2Transform baseTransform, b2Vec2 rayStart, b2Vec2 rayEnd,
                                  boolean showFraction) {
        b2Circle circle = new b2Circle(new b2Vec2(), 2.0f);
        b2Capsule capsule = new b2Capsule(new b2Vec2(-1.0f, 1.0f), new b2Vec2(1.0f, -1.0f), 1.5f);
        b2Polygon box = b2MakeBox(2.0f, 2.0f);
        b2Vec2[] vertices = {
            new b2Vec2(-2.0f, 0.0f),
            new b2Vec2(2.0f, 0.0f),
            new b2Vec2(2.0f, 3.0f)
        };
        b2Hull hull = b2ComputeHull(vertices, vertices.length);
        b2Polygon triangle = b2MakePolygon(hull, 0.0f);
        b2Segment segment = new b2Segment(new b2Vec2(-3.0f, 0.0f), new b2Vec2(3.0f, 0.0f));

        b2Vec2 offset = new b2Vec2(-20.0f, 20.0f);
        b2Vec2 increment = new b2Vec2(10.0f, 0.0f);
        OutputState[] outputs = new OutputState[5];
        b2CastOutput closest = new b2CastOutput();
        float maxFraction = 1.0f;

        for (int i = 0; i < outputs.length; ++i) {
            b2Transform transform = new b2Transform(b2Add(baseTransform.p, offset), baseTransform.q);
            b2Vec2 start = b2InvTransformPoint(transform, rayStart);
            b2Vec2 translation = b2InvRotateVector(transform.q, b2Sub(rayEnd, rayStart));
            b2RayCastInput input = new b2RayCastInput(start, translation, maxFraction);
            b2CastOutput localOutput;
            if (i == 0) {
                localOutput = b2RayCastCircle(input, circle);
            } else if (i == 1) {
                localOutput = b2RayCastCapsule(input, capsule);
            } else if (i == 2) {
                localOutput = b2RayCastPolygon(input, box);
            } else if (i == 3) {
                localOutput = b2RayCastPolygon(input, triangle);
            } else {
                localOutput = b2RayCastSegment(input, segment, false);
            }

            if (localOutput.hit) {
                closest = worldOutput(transform, localOutput);
                maxFraction = localOutput.fraction;
            }
            outputs[i] = outputState(localOutput);
            offset = b2Add(offset, increment);
        }

        return new Result(outputs, outputState(closest), maxFraction, baseTransform.copy(), rayStart.copy(),
            rayEnd.copy(), showFraction);
    }

    private static final class InteractiveState implements SampleRuntime.PointerHandler {
        final b2Transform transform = b2Transform_identity.copy();
        final b2Vec2 rayStart = new b2Vec2(0.0f, 30.0f);
        final b2Vec2 rayEnd = new b2Vec2(0.0f, 0.0f);
        float angle;
        boolean showFraction;
        boolean dragging;

        Result snapshot() {
            return compute(transform, rayStart, rayEnd, showFraction);
        }

        void reset() {
            transform.p = new b2Vec2();
            transform.q = b2Rot_identity.copy();
            angle = 0.0f;
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

    private static b2CastOutput worldOutput(b2Transform transform, b2CastOutput localOutput) {
        b2CastOutput output = new b2CastOutput();
        output.hit = localOutput.hit;
        output.fraction = localOutput.fraction;
        output.iterations = localOutput.iterations;
        output.point = b2TransformPoint(transform, localOutput.point);
        output.normal = b2RotateVector(transform.q, localOutput.normal);
        return output;
    }

    private static OutputState outputState(b2CastOutput output) {
        return new OutputState(output.hit, output.fraction, output.point.x, output.point.y,
            output.normal.x, output.normal.y, output.iterations);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final OutputState[] localOutputs;
        public final OutputState closestOutput;
        public final float maxFraction;
        public final b2Transform transform;
        public final b2Vec2 rayStart;
        public final b2Vec2 rayEnd;
        public final boolean showFraction;

        Result(OutputState[] localOutputs, OutputState closestOutput, float maxFraction, b2Transform transform,
               b2Vec2 rayStart, b2Vec2 rayEnd, boolean showFraction) {
            this.localOutputs = localOutputs;
            this.closestOutput = closestOutput;
            this.maxFraction = maxFraction;
            this.transform = transform;
            this.rayStart = rayStart;
            this.rayEnd = rayEnd;
            this.showFraction = showFraction;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder("rayCast ").append(localOutputs.length);
            for (OutputState output : localOutputs) {
                builder.append(output.toLinePart());
            }
            return builder.append(closestOutput.toLinePart()).append(formatFloat(maxFraction)).toString();
        }
    }

    public static final class OutputState {
        public final boolean hit;
        public final float fraction;
        public final float pointX;
        public final float pointY;
        public final float normalX;
        public final float normalY;
        public final int iterations;

        OutputState(boolean hit, float fraction, float pointX, float pointY,
                    float normalX, float normalY, int iterations) {
            this.hit = hit;
            this.fraction = fraction;
            this.pointX = pointX;
            this.pointY = pointY;
            this.normalX = normalX;
            this.normalY = normalY;
            this.iterations = iterations;
        }

        String toLinePart() {
            return " " + (hit ? 1 : 0) + formatFloat(fraction) + formatFloat(pointX) + formatFloat(pointY)
                + formatFloat(normalX) + formatFloat(normalY) + " " + iterations;
        }
    }

    private static String formatFloat(float value) {
        return " " + String.format(Locale.ROOT, "%.9g", value);
    }
}
