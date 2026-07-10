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

final class KinematicsParityTest {
    @Test
    void freeBodyKinematicsMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_kinematics_probe");

        List<String> sources = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted()
                .forEach(path -> sources.add(path.toString()));
        }

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        command.addAll(sources);
        command.add(root.resolve("tools/parity/box2d_kinematics_probe.c").toString());
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String[] lines = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(0, run.waitFor());

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, -10.0f);
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(1.25f, 7.0f);
        bodyDef.rotation = b2MakeRot(0.3f);
        bodyDef.linearVelocity = new b2Vec2(2.0f, -1.0f);
        bodyDef.angularVelocity = 0.4f;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        b2CreatePolygonShape(bodyId, shapeDef,
            b2MakeOffsetBox(0.75f, 0.5f, new b2Vec2(0.2f, -0.1f), b2MakeRot(0.15f)));

        for (int i = 0; i < 12; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        assertVec(lines[0], "position", b2Body_GetPosition(bodyId));
        assertVec(lines[1], "center", b2Body_GetWorldCenterOfMass(bodyId));
        assertVec(lines[2], "velocity", b2Body_GetLinearVelocity(bodyId));

        String[] parts = lines[3].split("\\s+");
        assertEquals("angle", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), b2Rot_GetAngle(b2Body_GetRotation(bodyId)), 0.0f);

        parts = lines[4].split("\\s+");
        assertEquals("angular", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), b2Body_GetAngularVelocity(bodyId), 0.0f);

        b2DestroyWorld(worldId);
    }

    private static void assertVec(String line, String label, b2Vec2 actual) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), actual.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), actual.y, 0.0f);
    }
}
