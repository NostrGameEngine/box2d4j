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

final class KinematicExtentParityTest {
    @Test
    void offCenterKinematicShapeContributesToSleepVelocity() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);
        b2BodyId centered = createBody(worldId, 0.0f, 0.0f);
        b2BodyId offset = createBody(worldId, 20.0f, 10.0f);

        for (int i = 0; i < 60; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 1);
        }
        b2Rot centeredRotation = b2Body_GetRotation(centered);
        b2Rot offsetRotation = b2Body_GetRotation(offset);
        String output = String.format("kinematicExtent %d %d %d %08x %08x %08x %08x",
            b2World_GetAwakeBodyCount(worldId), b2Body_IsAwake(centered) ? 1 : 0,
            b2Body_IsAwake(offset) ? 1 : 0, Float.floatToRawIntBits(centeredRotation.c),
            Float.floatToRawIntBits(centeredRotation.s), Float.floatToRawIntBits(offsetRotation.c),
            Float.floatToRawIntBits(offsetRotation.s));
        b2DestroyWorld(worldId);
        return output;
    }

    private static b2BodyId createBody(b2WorldId worldId, float positionX, float localCenterX) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_kinematicBody;
        bodyDef.position = new b2Vec2(positionX, 0.0f);
        bodyDef.angularVelocity = 0.1f;
        bodyDef.sleepThreshold = 0.5f;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(bodyId, b2DefaultShapeDef(),
            new b2Circle(new b2Vec2(localCenterX, 0.0f), 1.0f));
        return bodyId;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_kinematic_extent_probe");

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
        command.add(root.resolve("tools/parity/box2d_kinematic_extent_probe.c").toString());
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
