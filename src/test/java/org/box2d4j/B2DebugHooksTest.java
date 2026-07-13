package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class B2DebugHooksTest {
    @Test
    void listenerObservesOnlyWorldsOnTheInstallingThread() throws Exception {
        List<String> events = new ArrayList<>();
        try (B2DebugHooks.Scope ignored = B2DebugHooks.install(new B2DebugHooks.Listener() {
            @Override
            public void beforeWorldCreated(b2WorldDef worldDef) {
                events.add("beforeCreate:" + worldDef.workerCount);
            }

            @Override
            public void onWorldCreated(b2WorldId worldId) {
                events.add("create:" + worldId.index1);
            }

            @Override
            public void beforeWorldStep(b2WorldId worldId, float timeStep, int subStepCount) {
                events.add("beforeStep:" + worldId.index1 + ":" + subStepCount);
            }

            @Override
            public void onStepComplete(b2WorldId worldId) {
                events.add("step:" + worldId.index1);
            }

            @Override
            public void beforeWorldDestroyed(b2WorldId worldId) {
                events.add("destroy:" + worldId.index1);
            }
        })) {
            b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
            b2World_Step(worldId, 1.0f / 60.0f, 4);

            Thread other = new Thread(() -> {
                b2WorldId otherWorld = b2CreateWorld(b2DefaultWorldDef());
                b2World_Step(otherWorld, 1.0f / 60.0f, 4);
                b2DestroyWorld(otherWorld);
            });
            other.start();
            other.join();

            b2DestroyWorld(worldId);
        }

        assertEquals(List.of("beforeCreate:0", "create:1", "beforeStep:1:4", "step:1", "destroy:1"), events);
    }

    @Test
    void listenerCanRetainAWorldUntilItsScopeCloses() {
        b2WorldId worldId;
        try (B2DebugHooks.Scope ignored = B2DebugHooks.install(new B2DebugHooks.Listener() {
            @Override
            public boolean retainWorldOnDestroy(b2WorldId candidate) {
                return true;
            }
        })) {
            worldId = b2CreateWorld(b2DefaultWorldDef());
            b2DestroyWorld(worldId);
            assertTrue(b2World_IsValid(worldId));
        }

        b2DestroyWorld(worldId);
        assertFalse(b2World_IsValid(worldId));
    }
}
