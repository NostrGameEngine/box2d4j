package org.box2d4j.samples;

import org.box2d4j.b2Hull;
import org.box2d4j.b2Rot;
import org.box2d4j.b2Vec2;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ConvexHull {
    private static final int RAND_LIMIT = 32767;
    private static final int RAND_SEED = 12345;

    private ConvexHull() {
    }

    public static Result run() {
        State state = new State();
        state.generate();
        SampleRuntime.action("convexHull.generate", "Generate", "G", state::generate);
        SampleRuntime.toggle("convexHull.auto", "Auto", "A", false, value -> state.auto = value);
        SampleRuntime.toggle("convexHull.bulk", "Bulk", "B", false, value -> state.bulk = value);
        SampleRuntime.beforeStep(state::step);
        SampleRuntime.snapshot(state::capture);
        return state.capture();
    }

    private static final class State {
        final RandomState random = new RandomState(RAND_SEED);
        final b2Vec2[] points = b2Vec2.array(B2_MAX_POLYGON_VERTICES);
        int generation;
        float angle;
        boolean auto;
        boolean bulk;

        void generate() {
            angle = B2_PI * random.randomFloat();
            b2Rot rotation = b2MakeRot(angle);
            b2Vec2 lowerBound = new b2Vec2(-4.0f, -4.0f);
            b2Vec2 upperBound = new b2Vec2(4.0f, 4.0f);
            for (int i = 0; i < points.length; ++i) {
                float x = 10.0f * random.randomFloat();
                float y = 10.0f * random.randomFloat();
                points[i] = b2RotateVector(rotation,
                    b2Clamp(new b2Vec2(x, y), lowerBound, upperBound));
            }
            generation += 1;
        }

        void step() {
            if (bulk) {
                for (int i = 0; i < 10000; ++i) {
                    generate();
                    b2Hull hull = b2ComputeHull(points, points.length);
                    if (hull.count > 0 && !b2ValidateHull(hull)) {
                        bulk = false;
                        break;
                    }
                }
            } else if (auto) {
                generate();
            }
        }

        Result capture() {
            b2Hull hull = b2ComputeHull(points, points.length);
            boolean valid = hull.count > 0 && b2ValidateHull(hull);
            b2Vec2[] pointCopy = new b2Vec2[points.length];
            for (int i = 0; i < points.length; ++i) {
                pointCopy[i] = points[i].copy();
            }
            b2Vec2[] hullCopy = new b2Vec2[hull.count];
            for (int i = 0; i < hull.count; ++i) {
                hullCopy[i] = hull.points[i].copy();
            }
            return new Result(generation, angle, valid, hull.count, pointCopy, hullCopy);
        }
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final int generation;
        public final float angle;
        public final boolean valid;
        public final int hullCount;
        public final b2Vec2[] points;
        public final b2Vec2[] hullPoints;

        public Result(int generation, float angle, boolean valid, int hullCount, b2Vec2[] points, b2Vec2[] hullPoints) {
            this.generation = generation;
            this.angle = angle;
            this.valid = valid;
            this.hullCount = hullCount;
            this.points = points;
            this.hullPoints = hullPoints;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("convexHull ")
                .append(generation).append(' ')
                .append(formatFloat(angle)).append(' ')
                .append(valid ? 1 : 0).append(' ')
                .append(hullCount);
            for (int i = 0; i < B2_MAX_POLYGON_VERTICES; ++i) {
                appendVec(builder, points[i]);
            }
            for (int i = 0; i < hullCount; ++i) {
                appendVec(builder, hullPoints[i]);
            }
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

    private static final class RandomState {
        private int seed;

        RandomState(int seed) {
            this.seed = seed;
        }

        float randomFloat() {
            float r = randomInt() & RAND_LIMIT;
            r /= RAND_LIMIT;
            return 2.0f * r - 1.0f;
        }

        private int randomInt() {
            int x = seed;
            x ^= x << 13;
            x ^= x >>> 17;
            x ^= x << 5;
            seed = x;
            return x & RAND_LIMIT;
        }
    }
}
