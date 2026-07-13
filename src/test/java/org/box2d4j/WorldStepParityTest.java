package org.box2d4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class WorldStepParityTest {
    @AfterEach
    void restoreAssertHandler() {
        b2SetAssertFcn((condition, fileName, lineNumber) -> 1);
    }

    @Test
    void zeroTimeAndLockedCallbackBehaviorMatchUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        assertEquals(3, lines.length);
        assertZeroStep(lines[0]);
        assertLockedCallback(lines[1]);
        assertTelemetry(lines[2]);
    }

    private static void assertZeroStep(String expected) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId staticBody = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.enableContactEvents = true;
        b2CreatePolygonShape(staticBody, shapeDef, b2MakeSquare(1.0f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.gravityScale = 0.0f;
        bodyDef.linearVelocity = new b2Vec2(1.0f, 0.0f);
        b2BodyId dynamicBody = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(dynamicBody, shapeDef, b2MakeSquare(1.0f));

        int before = b2World_GetCounters(worldId).contactCount;
        b2World_Step(worldId, 0.0f, 1);
        int afterFirstZero = b2World_GetCounters(worldId).contactCount;
        b2World_Step(worldId, 1.0f / 60.0f, 1);
        int afterPositive = b2World_GetCounters(worldId).contactCount;
        int positiveMoves = b2World_GetBodyEvents(worldId).moveCount;
        int positiveBegins = b2World_GetContactEvents(worldId).beginCount;
        b2Vec2 beforeSecondZero = b2Body_GetPosition(dynamicBody);
        b2World_Step(worldId, 0.0f, 1);
        b2Vec2 afterSecondZero = b2Body_GetPosition(dynamicBody);

        String[] parts = expected.split("\\s+");
        assertEquals("zero", parts[0]);
        int[] actualCounts = {
            before, afterFirstZero, afterPositive, positiveMoves, positiveBegins,
            b2World_GetBodyEvents(worldId).moveCount,
            b2World_GetContactEvents(worldId).beginCount,
            b2World_GetSensorEvents(worldId).beginCount
        };
        for (int i = 0; i < actualCounts.length; ++i) {
            assertEquals(Integer.parseInt(parts[i + 1]), actualCounts[i]);
        }
        assertEquals(Float.parseFloat(parts[9]), beforeSecondZero.x, 0.0f);
        assertEquals(Float.parseFloat(parts[10]), afterSecondZero.x, 0.0f);
        b2DestroyWorld(worldId);
    }

    private static void assertLockedCallback(String expected) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId staticBody = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeId protectedShape = b2CreatePolygonShape(staticBody, b2DefaultShapeDef(), b2MakeSquare(1.0f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId dynamicBody = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(dynamicBody, b2DefaultShapeDef(), b2MakeSquare(1.0f));

        AtomicInteger assertions = new AtomicInteger();
        AtomicInteger callbacks = new AtomicInteger();
        b2SetAssertFcn((condition, fileName, lineNumber) -> {
            assertions.incrementAndGet();
            return 0;
        });

        b2BodyId[] attemptedBody = {b2_nullBodyId};
        int[] lockedMoves = {-1};
        b2TreeStats[] lockedStats = {null};
        float[] lockedMoverFraction = {-1.0f};
        b2World_SetCustomFilterCallback(worldId, (shapeA, shapeB, context) -> {
            callbacks.incrementAndGet();
            attemptedBody[0] = b2CreateBody(worldId, b2DefaultBodyDef());
            b2World_Step(worldId, 1.0f / 60.0f, 1);
            b2Shape_SetFriction(protectedShape, 0.25f);
            lockedMoves[0] = b2World_GetBodyEvents(worldId).moveCount;
            lockedStats[0] = b2World_OverlapAABB(worldId,
                new b2AABB(new b2Vec2(-2.0f, -2.0f), new b2Vec2(2.0f, 2.0f)),
                b2DefaultQueryFilter(), shapeId -> true);
            lockedMoverFraction[0] = b2World_CastMover(worldId,
                new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f),
                new b2Vec2(1.0f, 0.0f), b2DefaultQueryFilter());
            return true;
        }, null);

        b2World_Step(worldId, 1.0f / 60.0f, 1);
        String[] parts = expected.split("\\s+");
        assertEquals("locked", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), callbacks.get());
        assertEquals(Integer.parseInt(parts[2]), assertions.get());
        assertEquals(Integer.parseInt(parts[3]), b2Body_IsValid(attemptedBody[0]) ? 1 : 0);
        assertEquals(Float.parseFloat(parts[4]), b2Shape_GetFriction(protectedShape), 0.0f);
        assertEquals(Integer.parseInt(parts[5]), lockedMoves[0]);
        assertEquals(Integer.parseInt(parts[6]), lockedStats[0].nodeVisits);
        assertEquals(Integer.parseInt(parts[7]), lockedStats[0].leafVisits);
        assertEquals(Integer.parseInt(parts[8]), b2World_GetCounters(worldId).contactCount);
        assertEquals(Float.parseFloat(parts[9]), lockedMoverFraction[0], 0.0f);
        assertFalse(b2Body_IsValid(attemptedBody[0]));
        b2DestroyWorld(worldId);
    }

    private static void assertTelemetry(String expected) {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);
        for (int i = 0; i < 7; ++i) {
            createTelemetryBody(worldId, b2_staticBody, -18.0f + 3.0f * i, 0.0f);
        }
        for (int i = 0; i < 5; ++i) {
            createTelemetryBody(worldId, b2_dynamicBody, -12.0f + 3.0f * i, 4.0f);
        }
        for (int i = 0; i < 3; ++i) {
            createTelemetryBody(worldId, b2_kinematicBody, -6.0f + 3.0f * i, 8.0f);
        }

        b2Counters before = b2World_GetCounters(worldId);
        b2World_Step(worldId, 1.0f / 60.0f, 1);
        b2Counters after = b2World_GetCounters(worldId);
        b2Profile profile = b2World_GetProfile(worldId);

        String[] parts = expected.split("\\s+");
        assertEquals("telemetry", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), before.staticTreeHeight);
        assertEquals(Integer.parseInt(parts[2]), before.treeHeight);
        assertEquals(Integer.parseInt(parts[3]), after.staticTreeHeight);
        assertEquals(Integer.parseInt(parts[4]), after.treeHeight);
        int nativeProfileMask = Integer.parseInt(parts[7]);
        int javaProfileMask = profileMask(profile);
        org.junit.jupiter.api.Assertions.assertTrue(nativeProfileMask != 0);
        int solverPhaseMask = 2048 | 4096 | 8192 | 16384 | 32768 | 65536 | 131072 | 262144 | 524288;
        assertEquals(nativeProfileMask & solverPhaseMask, javaProfileMask & nativeProfileMask & solverPhaseMask,
            "every solver phase observed by upstream C must be observed by Java");
        assertEquals(0, after.stackUsed);
        org.junit.jupiter.api.Assertions.assertTrue(after.taskCount > 0);
        b2DestroyWorld(worldId);
    }

    private static void createTelemetryBody(b2WorldId worldId, int type, float x, float y) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = type;
        bodyDef.position = new b2Vec2(x, y);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyId, b2DefaultShapeDef(), b2MakeSquare(0.5f));
    }

    private static int profileMask(b2Profile profile) {
        int mask = 0;
        mask |= profile.step > 0.0f ? 1 : 0;
        mask |= profile.pairs > 0.0f ? 2 : 0;
        mask |= profile.collide > 0.0f ? 4 : 0;
        mask |= profile.solve > 0.0f ? 8 : 0;
        mask |= profile.prepareStages > 0.0f ? 16 : 0;
        mask |= profile.solveConstraints > 0.0f ? 32 : 0;
        mask |= profile.transforms > 0.0f ? 64 : 0;
        mask |= profile.hitEvents > 0.0f ? 128 : 0;
        mask |= profile.refit > 0.0f ? 256 : 0;
        mask |= profile.sleepIslands > 0.0f ? 512 : 0;
        mask |= profile.sensors > 0.0f ? 1024 : 0;
        mask |= profile.mergeIslands > 0.0f ? 2048 : 0;
        mask |= profile.prepareConstraints > 0.0f ? 4096 : 0;
        mask |= profile.integrateVelocities > 0.0f ? 8192 : 0;
        mask |= profile.warmStart > 0.0f ? 16384 : 0;
        mask |= profile.solveImpulses > 0.0f ? 32768 : 0;
        mask |= profile.integratePositions > 0.0f ? 65536 : 0;
        mask |= profile.relaxImpulses > 0.0f ? 131072 : 0;
        mask |= profile.applyRestitution > 0.0f ? 262144 : 0;
        mask |= profile.storeImpulses > 0.0f ? 524288 : 0;
        return mask;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_world_step_probe");

        List<String> sources = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted()
                .forEach(path -> sources.add(path.toString()));
        }

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        command.addAll(sources);
        command.add(root.resolve("tools/parity/box2d_world_step_probe.c").toString());
        command.add("-lm");
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertEquals(0, run.waitFor(), output);
        return output;
    }
}
