package org.box2d4j.samples;

import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Rot;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.function.LongSupplier;

import static org.box2d4j.B2.*;

public final class BenchmarkSampleResult {
    private static final long FNV_OFFSET = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;

    interface StepAction {
        void run(int step);
    }

    public final String name;
    public final Checkpoint initial;
    public final Checkpoint result;
    public final long workloadHash;

    private BenchmarkSampleResult(String name, Checkpoint initial, Checkpoint result, long workloadHash) {
        this.name = name;
        this.initial = initial;
        this.result = result;
        this.workloadHash = workloadHash;
    }

    static BenchmarkSampleResult simulate(String name, BenchmarkScenes.Scene scene, int stepCount) {
        return simulate(name, scene, stepCount, null, null);
    }

    static BenchmarkSampleResult simulate(String name, BenchmarkScenes.Scene scene, int stepCount,
                                           StepAction beforeStep, StepAction afterStep) {
        return simulate(name, scene, stepCount, beforeStep, afterStep, () -> 0L);
    }

    static BenchmarkSampleResult simulate(String name, BenchmarkScenes.Scene scene, int stepCount,
                                           StepAction beforeStep, StepAction afterStep, LongSupplier workloadHash) {
        Checkpoint initial = capture(0, scene.worldId, scene.bodies.toArray(new b2BodyId[0]));
        for (int step = 0; step < stepCount; ++step) {
            if (beforeStep != null) {
                beforeStep.run(step);
            }
            b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
            if (afterStep != null) {
                afterStep.run(step + 1);
            }
        }
        Checkpoint result = capture(stepCount, scene.worldId, scene.bodies.toArray(new b2BodyId[0]));
        long hash = workloadHash.getAsLong();
        b2DestroyWorld(scene.worldId);
        return new BenchmarkSampleResult(name, initial, result, hash);
    }

    static Checkpoint capture(int step, b2WorldId worldId, b2BodyId[] bodies) {
        long hash = FNV_OFFSET;
        int validBodyCount = 0;
        for (b2BodyId bodyId : bodies) {
            boolean valid = bodyId != null && b2Body_IsValid(bodyId);
            hash = mix(hash, valid ? 1 : 0);
            if (!valid) {
                continue;
            }
            validBodyCount += 1;
            b2Vec2 position = b2Body_GetPosition(bodyId);
            b2Rot rotation = b2Body_GetRotation(bodyId);
            b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
            hash = mix(hash, b2Body_GetType(bodyId));
            hash = mix(hash, Float.floatToRawIntBits(position.x));
            hash = mix(hash, Float.floatToRawIntBits(position.y));
            hash = mix(hash, Float.floatToRawIntBits(rotation.c));
            hash = mix(hash, Float.floatToRawIntBits(rotation.s));
            hash = mix(hash, Float.floatToRawIntBits(velocity.x));
            hash = mix(hash, Float.floatToRawIntBits(velocity.y));
            hash = mix(hash, Float.floatToRawIntBits(b2Body_GetAngularVelocity(bodyId)));
            hash = mix(hash, b2Body_IsAwake(bodyId) ? 1 : 0);
            hash = mix(hash, b2Body_GetShapeCount(bodyId));
            hash = mix(hash, b2Body_GetContactCapacity(bodyId));
        }

        b2Counters counters = b2World_GetCounters(worldId);
        return new Checkpoint(step, counters.bodyCount, counters.shapeCount, counters.contactCount,
            counters.jointCount, counters.islandCount, b2World_GetAwakeBodyCount(worldId), validBodyCount,
            counters.colorCounts.clone(), hash);
    }

    private static long mix(long hash, int value) {
        return (hash ^ Integer.toUnsignedLong(value)) * FNV_PRIME;
    }

    public String toLine() {
        StringBuilder builder = new StringBuilder(name);
        initial.appendTo(builder);
        result.appendTo(builder);
        builder.append(' ').append(Long.toUnsignedString(workloadHash));
        return builder.toString();
    }

    public static final class Checkpoint {
        public final int step;
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int jointCount;
        public final int islandCount;
        public final int awakeBodyCount;
        public final int validBodyCount;
        public final int[] colorCounts;
        public final long bodyHash;

        private Checkpoint(int step, int bodyCount, int shapeCount, int contactCount, int jointCount, int islandCount,
                           int awakeBodyCount, int validBodyCount, int[] colorCounts, long bodyHash) {
            this.step = step;
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.jointCount = jointCount;
            this.islandCount = islandCount;
            this.awakeBodyCount = awakeBodyCount;
            this.validBodyCount = validBodyCount;
            this.colorCounts = colorCounts;
            this.bodyHash = bodyHash;
        }

        private void appendTo(StringBuilder builder) {
            builder.append(' ').append(step)
                .append(' ').append(bodyCount)
                .append(' ').append(shapeCount)
                .append(' ').append(contactCount)
                .append(' ').append(jointCount)
                .append(' ').append(islandCount)
                .append(' ').append(awakeBodyCount)
                .append(' ').append(validBodyCount)
                .append(' ').append(colorCounts.length);
            for (int colorCount : colorCounts) {
                builder.append(' ').append(colorCount);
            }
            builder.append(' ').append(Long.toUnsignedString(bodyHash));
        }
    }
}
