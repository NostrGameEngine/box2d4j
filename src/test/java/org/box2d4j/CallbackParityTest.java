package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class CallbackParityTest {
    @Test
    void customFilterAndPreSolveCallbacksMatchUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, 0.0f);

        b2WorldId worldId = b2CreateWorld(worldDef);
        int[] filterCalls = {0};
        b2World_SetCustomFilterCallback(worldId, (shapeIdA, shapeIdB, context) -> {
            filterCalls[0] += 1;
            return false;
        }, null);
        createBoxBody(worldId, new b2Vec2(0.0f, 0.0f), false);
        createBoxBody(worldId, new b2Vec2(0.25f, 0.0f), false);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2Counters counters = b2World_GetCounters(worldId);
        b2ContactEvents events = b2World_GetContactEvents(worldId);
        assertEquals(lines[0], "filter " + filterCalls[0] + " " + counters.contactCount + " " +
            events.beginCount + " " + events.endCount);
        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(worldDef);
        int[] preSolveCalls = {0};
        int[] preSolvePointCount = {0};
        b2World_SetPreSolveCallback(worldId, (shapeIdA, shapeIdB, manifold, context) -> {
            preSolveCalls[0] += 1;
            preSolvePointCount[0] = manifold.pointCount;
            return false;
        }, null);
        b2BodyId bodyA = createBoxBody(worldId, new b2Vec2(0.0f, 0.0f), true);
        b2BodyId bodyB = createBoxBody(worldId, new b2Vec2(0.25f, 0.0f), false);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        counters = b2World_GetCounters(worldId);
        events = b2World_GetContactEvents(worldId);
        b2ContactData[] data = new b2ContactData[2];
        int contactDataCount = b2Body_GetContactData(bodyA, data, 2);
        assertEquals(lines[1], "presolve " + preSolveCalls[0] + " " + preSolvePointCount[0] + " " +
            counters.contactCount + " " + events.beginCount + " " + events.endCount + " " +
            b2Body_GetContactCapacity(bodyA) + " " + b2Body_GetContactCapacity(bodyB) + " " + contactDataCount);
        b2DestroyWorld(worldId);
    }

    private static b2BodyId createBoxBody(b2WorldId worldId, b2Vec2 position, boolean enablePreSolve) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = position;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.enablePreSolveEvents = enablePreSolve;
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(0.5f, 0.5f));
        return bodyId;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_callback_probe");

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
        command.add(root.resolve("tools/parity/box2d_callback_probe.c").toString());
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
