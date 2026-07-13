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

final class InitialVelocityParityTest {
    @Test
    void initialVelocitiesExistOnlyForBodiesCreatedAwakeLikeUpstream() throws Exception {
        String[] expected = runProbe().split("\\R");
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2();
        b2WorldId worldId = b2CreateWorld(worldDef);
        int line = 0;

        b2BodyDef def = movingDef();
        b2BodyId staticBody = b2CreateBody(worldId, def);
        assertState(expected[line++], "static", staticBody);

        def = movingDef();
        def.type = b2_dynamicBody;
        def.isAwake = false;
        b2BodyId sleepingBody = b2CreateBody(worldId, def);
        assertState(expected[line++], "sleeping", sleepingBody);
        b2Body_SetAwake(sleepingBody, true);
        assertState(expected[line++], "woken", sleepingBody);

        def = movingDef();
        def.type = b2_dynamicBody;
        def.isEnabled = false;
        b2BodyId disabledBody = b2CreateBody(worldId, def);
        assertState(expected[line++], "disabled", disabledBody);
        b2Body_Enable(disabledBody);
        assertState(expected[line++], "enabled", disabledBody);

        def = movingDef();
        def.type = b2_dynamicBody;
        b2BodyId awakeBody = b2CreateBody(worldId, def);
        assertState(expected[line++], "awake", awakeBody);

        assertEquals(expected.length, line);
        b2DestroyWorld(worldId);
    }

    private static b2BodyDef movingDef() {
        b2BodyDef def = b2DefaultBodyDef();
        def.position = new b2Vec2(1.0f, 2.0f);
        def.linearVelocity = new b2Vec2(2.0f, -3.0f);
        def.angularVelocity = 4.0f;
        return def;
    }

    private static void assertState(String expected, String label, b2BodyId bodyId) {
        String[] parts = expected.split("\\s+");
        b2Vec2 linear = b2Body_GetLinearVelocity(bodyId);
        b2Vec2 local = b2Body_GetLocalPointVelocity(bodyId, new b2Vec2(1.0f, 0.0f));
        b2Vec2 world = b2Body_GetWorldPointVelocity(bodyId, new b2Vec2(2.0f, 2.0f));
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, b2Body_IsAwake(bodyId));
        assertEquals(Float.parseFloat(parts[2]), linear.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), linear.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), b2Body_GetAngularVelocity(bodyId), 0.0f);
        assertEquals(Float.parseFloat(parts[5]), local.x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), local.y, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), world.x, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), world.y, 0.0f);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_initial_velocity_probe");
        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-Wall");
        command.add("-Wextra");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted().map(Path::toString).forEach(command::add);
        }
        command.add(root.resolve("tools/parity/box2d_initial_velocity_probe.c").toString());
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
