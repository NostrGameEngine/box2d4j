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

final class SpeculativeContactParityTest {
    @Test
    void speculativeToggleFiltersManifoldPointsLikeC() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        return runCase("speculativeOn", true) + System.lineSeparator()
            + runCase("speculativeOff", false);
    }

    private static String runCase(String label, boolean enableSpeculative) {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);
        b2World_EnableSpeculative(worldId, enableSpeculative);

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(2.0f, 0.5f));
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 1.015f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyId, b2DefaultShapeDef(), b2MakeBox(0.5f, 0.5f));

        b2World_Step(worldId, 1.0f / 60.0f, 1);
        b2ContactData[] data = new b2ContactData[1];
        int count = b2Body_GetContactData(bodyId, data, 1);
        int pointCount = count == 0 ? 0 : data[0].manifold.pointCount;
        int separation0 = pointCount > 0 ? Float.floatToRawIntBits(data[0].manifold.points[0].separation) : 0;
        int separation1 = pointCount > 1 ? Float.floatToRawIntBits(data[0].manifold.points[1].separation) : 0;
        String output = String.format("%s %d %d %08x %08x", label, count, pointCount, separation0, separation1);
        b2DestroyWorld(worldId);
        return output;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_speculative_contact_probe");

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted().map(Path::toString).forEach(command::add);
        }
        command.add(root.resolve("tools/parity/box2d_speculative_contact_probe.c").toString());
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
