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

final class DisabledTargetTransformParityTest {
    @Test
    void targetTransformDoesNotLeakVelocityThroughDisabledState() throws Exception {
        String[] expected = runProbe().split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2();
        b2WorldId worldId = b2CreateWorld(worldDef);
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(1.0f, 1.0f));

        b2Body_Disable(bodyId);
        b2Body_SetTargetTransform(bodyId, new b2Transform(new b2Vec2(10.0f, -3.0f), b2MakeRot(0.5f)), 0.5f);
        assertVelocity(expected[0], "disabled", bodyId);
        b2Body_Enable(bodyId);
        assertVelocity(expected[1], "enabled", bodyId);

        b2DestroyWorld(worldId);
    }

    private static void assertVelocity(String expected, String label, b2BodyId bodyId) {
        String[] parts = expected.split("\\s+");
        b2Vec2 linearVelocity = b2Body_GetLinearVelocity(bodyId);
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), linearVelocity.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), linearVelocity.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2Body_GetAngularVelocity(bodyId), 0.0f);
        assertEquals(Integer.parseInt(parts[4]) != 0, b2Body_IsAwake(bodyId));
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_disabled_target_probe");

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
                .sorted()
                .map(Path::toString)
                .forEach(command::add);
        }
        command.add(root.resolve("tools/parity/box2d_disabled_target_probe.c").toString());
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
