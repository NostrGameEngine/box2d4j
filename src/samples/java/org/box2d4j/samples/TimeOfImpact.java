package org.box2d4j.samples;

import org.box2d4j.b2DistanceInput;
import org.box2d4j.b2DistanceOutput;
import org.box2d4j.b2SimplexCache;
import org.box2d4j.b2Sweep;
import org.box2d4j.b2TOIInput;
import org.box2d4j.b2TOIOutput;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class TimeOfImpact {
    private TimeOfImpact() {
    }

    public static Result run() {
        b2Vec2[] verticesA = {
            new b2Vec2(-16.25f, 44.75f),
            new b2Vec2(-15.75f, 44.75f),
            new b2Vec2(-15.75f, 45.25f),
            new b2Vec2(-16.25f, 45.25f)
        };
        b2Vec2[] verticesB = {
            new b2Vec2(0.0f, -0.125000000f),
            new b2Vec2(0.0f, 0.125000000f)
        };

        b2Sweep sweepA = new b2Sweep();
        sweepA.localCenter = new b2Vec2();
        sweepA.c1 = new b2Vec2();
        sweepA.c2 = new b2Vec2();
        sweepA.q1 = b2Rot_identity.copy();
        sweepA.q2 = b2Rot_identity.copy();

        b2Sweep sweepB = new b2Sweep();
        sweepB.localCenter = new b2Vec2();
        sweepB.c1 = new b2Vec2(-15.8332710f, 45.3520279f);
        sweepB.c2 = new b2Vec2(-15.8324337f, 45.3413048f);
        sweepB.q1.c = -0.540891349f;
        sweepB.q1.s = 0.841092527f;
        sweepB.q2.c = -0.457797021f;
        sweepB.q2.s = 0.889056742f;

        b2TOIInput input = new b2TOIInput();
        input.proxyA = b2MakeProxy(verticesA, verticesA.length, 0.0f);
        input.proxyB = b2MakeProxy(verticesB, verticesB.length, 0.0299999993f);
        input.sweepA = sweepA;
        input.sweepB = sweepB;
        input.maxFraction = 1.0f;
        b2TOIOutput output = b2TimeOfImpact(input);

        b2Transform start = b2GetSweepTransform(sweepB, 0.0f);
        b2Transform hit = b2GetSweepTransform(sweepB, output.fraction);
        b2Transform end = b2GetSweepTransform(sweepB, 1.0f);
        b2Vec2 startA = b2TransformPoint(start, verticesB[0]);
        b2Vec2 startB = b2TransformPoint(start, verticesB[1]);
        b2Vec2 hitA = b2TransformPoint(hit, verticesB[0]);
        b2Vec2 hitB = b2TransformPoint(hit, verticesB[1]);
        b2Vec2 endA = b2TransformPoint(end, verticesB[0]);
        b2Vec2 endB = b2TransformPoint(end, verticesB[1]);

        float distance = 0.0f;
        int distanceIterations = 0;
        int simplexCount = 0;
        if (output.state == b2_toiStateHit) {
            b2DistanceInput distanceInput = new b2DistanceInput();
            distanceInput.proxyA = input.proxyA;
            distanceInput.proxyB = input.proxyB;
            distanceInput.transformA = b2GetSweepTransform(sweepA, output.fraction);
            distanceInput.transformB = hit;
            distanceInput.useRadii = false;
            b2SimplexCache cache = new b2SimplexCache();
            b2DistanceOutput distanceOutput = b2ShapeDistance(distanceInput, cache, null, 0);
            distance = distanceOutput.distance;
            distanceIterations = distanceOutput.iterations;
            simplexCount = distanceOutput.simplexCount;
        }

        return new Result(output.state, output.fraction, distance, distanceIterations, simplexCount,
            new SegmentState(startA, startB), new SegmentState(hitA, hitB), new SegmentState(endA, endB));
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final int state;
        public final float fraction;
        public final float distance;
        public final int distanceIterations;
        public final int simplexCount;
        public final SegmentState start;
        public final SegmentState hit;
        public final SegmentState end;

        Result(int state, float fraction, float distance, int distanceIterations, int simplexCount,
               SegmentState start, SegmentState hit, SegmentState end) {
            this.state = state;
            this.fraction = fraction;
            this.distance = distance;
            this.distanceIterations = distanceIterations;
            this.simplexCount = simplexCount;
            this.start = start;
            this.hit = hit;
            this.end = end;
        }

        public String toLine() {
            return "timeOfImpact " + state + formatFloat(fraction) + formatFloat(distance) + " "
                + distanceIterations + " " + simplexCount + start.toLinePart() + hit.toLinePart()
                + end.toLinePart();
        }
    }

    public static final class SegmentState {
        public final float x1;
        public final float y1;
        public final float x2;
        public final float y2;

        SegmentState(b2Vec2 point1, b2Vec2 point2) {
            x1 = point1.x;
            y1 = point1.y;
            x2 = point2.x;
            y2 = point2.y;
        }

        String toLinePart() {
            return formatFloat(x1) + formatFloat(y1) + formatFloat(x2) + formatFloat(y2);
        }
    }

    private static String formatFloat(float value) {
        return " " + String.format(Locale.ROOT, "%.9g", value);
    }
}
