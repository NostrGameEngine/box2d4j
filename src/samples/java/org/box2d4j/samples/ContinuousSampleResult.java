package org.box2d4j.samples;

import org.box2d4j.b2BodyId;
import org.box2d4j.b2ContactData;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Rot;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ContinuousSampleResult {
    private static final int[] CHECKPOINTS = {
        1, 2, 4, 8, 12, 16, 24, 32, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96,
        97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 120
    };

    public final String name;
    public final BodyState[] states;
    public final int bodyCount;
    public final int shapeCount;
    public final int contactCount;
    public final int islandCount;
    public final int awakeBodyCount;

    private ContinuousSampleResult(String name, BodyState[] states, b2Counters counters, int awakeBodyCount) {
        this.name = name;
        this.states = states;
        this.bodyCount = counters.bodyCount;
        this.shapeCount = counters.shapeCount;
        this.contactCount = counters.contactCount;
        this.islandCount = counters.islandCount;
        this.awakeBodyCount = awakeBodyCount;
    }

    static ContinuousSampleResult simulate(String name, b2WorldId worldId, b2BodyId bodyId) {
        return simulate(name, worldId, bodyId, null);
    }

    static ContinuousSampleResult simulate(String name, b2WorldId worldId, b2BodyId bodyId, StepAction stepAction) {
        BodyState[] states = new BodyState[CHECKPOINTS.length];
        int checkpointIndex = 0;
        for (int step = 1; step <= CHECKPOINTS[CHECKPOINTS.length - 1]; ++step) {
            if (stepAction != null) {
                stepAction.beforeStep(step);
            }
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (step == CHECKPOINTS[checkpointIndex]) {
                states[checkpointIndex] = new BodyState(step, bodyId);
                checkpointIndex += 1;
            }
        }

        ContinuousSampleResult result = new ContinuousSampleResult(
            name, states, b2World_GetCounters(worldId), b2World_GetAwakeBodyCount(worldId));
        b2DestroyWorld(worldId);
        return result;
    }

    interface StepAction {
        void beforeStep(int step);
    }

    public String toLine() {
        StringBuilder builder = new StringBuilder(name)
            .append(' ').append(bodyCount)
            .append(' ').append(shapeCount)
            .append(' ').append(contactCount)
            .append(' ').append(islandCount)
            .append(' ').append(awakeBodyCount)
            .append(' ').append(states.length);
        for (BodyState state : states) {
            state.appendTo(builder);
        }
        return builder.toString();
    }

    public static final class BodyState {
        public final int step;
        public final float x;
        public final float y;
        public final float rotationCos;
        public final float rotationSin;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final int contactCount;
        public final boolean awake;

        private BodyState(int step, b2BodyId bodyId) {
            b2Vec2 position = b2Body_GetPosition(bodyId);
            b2Rot rotation = b2Body_GetRotation(bodyId);
            b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
            b2ContactData[] contactData = new b2ContactData[8];

            this.step = step;
            this.x = position.x;
            this.y = position.y;
            this.rotationCos = rotation.c;
            this.rotationSin = rotation.s;
            this.velocityX = velocity.x;
            this.velocityY = velocity.y;
            this.angularVelocity = b2Body_GetAngularVelocity(bodyId);
            this.contactCount = b2Body_GetContactData(bodyId, contactData, contactData.length);
            this.awake = b2Body_IsAwake(bodyId);
        }

        private void appendTo(StringBuilder builder) {
            builder.append(' ').append(step)
                .append(formatFloat(x))
                .append(formatFloat(y))
                .append(formatFloat(rotationCos))
                .append(formatFloat(rotationSin))
                .append(formatFloat(velocityX))
                .append(formatFloat(velocityY))
                .append(formatFloat(angularVelocity))
                .append(' ').append(contactCount)
                .append(' ').append(awake ? 1 : 0);
        }
    }

    private static String formatFloat(float value) {
        return " " + String.format(Locale.ROOT, "%.9g", value);
    }
}
