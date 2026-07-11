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

final class BodySleepParityTest {
    @Test
    void bodySleepApiMatchesUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_body_sleep_probe");

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
        command.add(root.resolve("tools/parity/box2d_body_sleep_probe.c").toString());
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String[] lines = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(0, run.waitFor());

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(1.0f, 1.0f));

        String[] parts = lines[0].split("\\s+");
        assertEquals("initial", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, b2Body_IsAwake(bodyId));
        assertEquals(Integer.parseInt(parts[2]) != 0, b2Body_IsSleepEnabled(bodyId));
        assertEquals(Float.parseFloat(parts[3]), b2Body_GetSleepThreshold(bodyId), 0.0f);

        b2Body_SetSleepThreshold(bodyId, 0.25f);
        b2Body_SetAwake(bodyId, false);
        parts = lines[1].split("\\s+");
        assertEquals("sleep", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), b2Body_GetSleepThreshold(bodyId), 0.0f);
        assertEquals(Integer.parseInt(parts[2]) != 0, b2Body_IsAwake(bodyId));
        assertEquals(Float.parseFloat(parts[3]), b2Body_GetLinearVelocity(bodyId).y, 0.0f);

        b2Body_EnableSleep(bodyId, false);
        parts = lines[2].split("\\s+");
        assertEquals("disabled", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, b2Body_IsSleepEnabled(bodyId));
        assertEquals(Integer.parseInt(parts[2]) != 0, b2Body_IsAwake(bodyId));

        b2DestroyWorld(worldId);
    }
}
