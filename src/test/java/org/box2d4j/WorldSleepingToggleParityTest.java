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

final class WorldSleepingToggleParityTest {
    @Test
    void disablingWorldSleepingWakesEverySleepingBody() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        bodyDef.position = new b2Vec2(3.0f, 0.0f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);

        for (int i = 0; i < 60; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 1);
        }
        StringBuilder output = new StringBuilder("worldSleepingToggle");
        appendState(output, worldId, bodyA, bodyB);

        b2World_EnableSleeping(worldId, false);
        appendState(output, worldId, bodyA, bodyB);
        b2World_EnableSleeping(worldId, false);
        b2World_Step(worldId, 1.0f / 60.0f, 1);
        appendState(output, worldId, bodyA, bodyB);

        b2World_EnableSleeping(worldId, true);
        appendState(output, worldId, bodyA, bodyB);
        for (int i = 0; i < 60; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 1);
        }
        appendState(output, worldId, bodyA, bodyB);

        b2DestroyWorld(worldId);
        return output.toString();
    }

    private static void appendState(StringBuilder output, b2WorldId worldId, b2BodyId bodyA, b2BodyId bodyB) {
        output.append(String.format(" %d %d %d %d", b2World_IsSleepingEnabled(worldId) ? 1 : 0,
            b2World_GetAwakeBodyCount(worldId), b2Body_IsAwake(bodyA) ? 1 : 0, b2Body_IsAwake(bodyB) ? 1 : 0));
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_world_sleeping_toggle_probe");

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
        command.add(root.resolve("tools/parity/box2d_world_sleeping_toggle_probe.c").toString());
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
