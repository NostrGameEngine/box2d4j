package org.box2d4j.samples;

import org.box2d4j.b2BodyId;
import org.box2d4j.b2ContactData;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Rot;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class RobustnessSampleResult {
    private static final int[] CHECKPOINTS = {1, 2, 4, 8, 16, 32, 38, 39, 60, 61, 62, 120};
    private static final long FNV_OFFSET = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;

    public final String name;
    public final int bodyCount;
    public final int shapeCount;
    public final int contactCount;
    public final int jointCount;
    public final int islandCount;
    public final int awakeBodyCount;
    public final int dynamicBodyCount;
    public final Checkpoint[] checkpoints;

    private RobustnessSampleResult(String name, b2Counters counters, int awakeBodyCount,
                                   int dynamicBodyCount, Checkpoint[] checkpoints) {
        this.name = name;
        this.bodyCount = counters.bodyCount;
        this.shapeCount = counters.shapeCount;
        this.contactCount = counters.contactCount;
        this.jointCount = counters.jointCount;
        this.islandCount = counters.islandCount;
        this.awakeBodyCount = awakeBodyCount;
        this.dynamicBodyCount = dynamicBodyCount;
        this.checkpoints = checkpoints;
    }

    static RobustnessSampleResult simulate(String name, b2WorldId worldId, b2BodyId[] bodies) {
        Checkpoint[] checkpoints = new Checkpoint[CHECKPOINTS.length];
        int checkpointIndex = 0;
        for (int step = 1; step <= CHECKPOINTS[CHECKPOINTS.length - 1]; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (step == CHECKPOINTS[checkpointIndex]) {
                checkpoints[checkpointIndex] = checkpoint(step, worldId, bodies);
                checkpointIndex += 1;
            }
        }

        RobustnessSampleResult result = new RobustnessSampleResult(name, b2World_GetCounters(worldId),
            b2World_GetAwakeBodyCount(worldId), bodies.length, checkpoints);
        b2DestroyWorld(worldId);
        return result;
    }

    private static Checkpoint checkpoint(int step, b2WorldId worldId, b2BodyId[] bodies) {
        long hash = FNV_OFFSET;
        for (b2BodyId bodyId : bodies) {
            b2Vec2 position = b2Body_GetPosition(bodyId);
            b2Rot rotation = b2Body_GetRotation(bodyId);
            b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
            hash = mix(hash, Float.floatToRawIntBits(position.x));
            hash = mix(hash, Float.floatToRawIntBits(position.y));
            hash = mix(hash, Float.floatToRawIntBits(rotation.c));
            hash = mix(hash, Float.floatToRawIntBits(rotation.s));
            hash = mix(hash, Float.floatToRawIntBits(velocity.x));
            hash = mix(hash, Float.floatToRawIntBits(velocity.y));
            hash = mix(hash, Float.floatToRawIntBits(b2Body_GetAngularVelocity(bodyId)));
            hash = mix(hash, b2Body_IsAwake(bodyId) ? 1 : 0);
        }

        int[] indices = {0, bodies.length / 2, bodies.length - 1};
        BodyState[] representatives = new BodyState[indices.length];
        for (int i = 0; i < indices.length; ++i) {
            representatives[i] = new BodyState(indices[i], bodies[indices[i]]);
        }
        b2Counters counters = b2World_GetCounters(worldId);
        return new Checkpoint(step, counters.contactCount, counters.islandCount,
            b2World_GetAwakeBodyCount(worldId), counters.colorCounts.clone(), hash, representatives);
    }

    private static long mix(long hash, int value) {
        return (hash ^ Integer.toUnsignedLong(value)) * FNV_PRIME;
    }

    public String toLine() {
        StringBuilder builder = new StringBuilder(name)
            .append(' ').append(bodyCount)
            .append(' ').append(shapeCount)
            .append(' ').append(contactCount)
            .append(' ').append(jointCount)
            .append(' ').append(islandCount)
            .append(' ').append(awakeBodyCount)
            .append(' ').append(dynamicBodyCount)
            .append(' ').append(checkpoints.length);
        for (Checkpoint checkpoint : checkpoints) {
            checkpoint.appendTo(builder);
        }
        return builder.toString();
    }

    public static final class Checkpoint {
        public final int step;
        public final int contactCount;
        public final int islandCount;
        public final int awakeBodyCount;
        public final int[] colorCounts;
        public final long stateHash;
        public final BodyState[] representatives;

        private Checkpoint(int step, int contactCount, int islandCount, int awakeBodyCount, int[] colorCounts,
                           long stateHash, BodyState[] representatives) {
            this.step = step;
            this.contactCount = contactCount;
            this.islandCount = islandCount;
            this.awakeBodyCount = awakeBodyCount;
            this.colorCounts = colorCounts;
            this.stateHash = stateHash;
            this.representatives = representatives;
        }

        private void appendTo(StringBuilder builder) {
            builder.append(' ').append(step)
                .append(' ').append(contactCount)
                .append(' ').append(islandCount)
                .append(' ').append(awakeBodyCount)
                .append(' ').append(colorCounts.length);
            for (int colorCount : colorCounts) {
                builder.append(' ').append(colorCount);
            }
            builder
                .append(' ').append(Long.toUnsignedString(stateHash))
                .append(' ').append(representatives.length);
            for (BodyState state : representatives) {
                state.appendTo(builder);
            }
        }
    }

    public static final class BodyState {
        public final int index;
        public final float x;
        public final float y;
        public final float rotationCos;
        public final float rotationSin;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final int contactCount;
        public final boolean awake;

        private BodyState(int index, b2BodyId bodyId) {
            b2Vec2 position = b2Body_GetPosition(bodyId);
            b2Rot rotation = b2Body_GetRotation(bodyId);
            b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
            b2ContactData[] contacts = new b2ContactData[16];
            this.index = index;
            this.x = position.x;
            this.y = position.y;
            this.rotationCos = rotation.c;
            this.rotationSin = rotation.s;
            this.velocityX = velocity.x;
            this.velocityY = velocity.y;
            this.angularVelocity = b2Body_GetAngularVelocity(bodyId);
            this.contactCount = b2Body_GetContactData(bodyId, contacts, contacts.length);
            this.awake = b2Body_IsAwake(bodyId);
        }

        private void appendTo(StringBuilder builder) {
            builder.append(' ').append(index)
                .append(formatFloat(x)).append(formatFloat(y))
                .append(formatFloat(rotationCos)).append(formatFloat(rotationSin))
                .append(formatFloat(velocityX)).append(formatFloat(velocityY))
                .append(formatFloat(angularVelocity))
                .append(' ').append(contactCount)
                .append(' ').append(awake ? 1 : 0);
        }
    }

    private static String formatFloat(float value) {
        return " " + String.format(Locale.ROOT, "%.9g", value);
    }
}
