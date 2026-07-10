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

final class BodyMassParityTest {
    @Test
    void bodyMassPropertiesMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_body_mass_probe");

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
        command.add(root.resolve("tools/parity/box2d_body_mass_probe.c").toString());
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
        bodyDef.position = new b2Vec2(2.0f, 3.0f);
        bodyDef.rotation = b2MakeRot(0.2f);
        bodyDef.linearVelocity = new b2Vec2(1.0f, -2.0f);
        bodyDef.angularVelocity = 0.75f;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef boxDef = b2DefaultShapeDef();
        boxDef.density = 2.0f;
        b2CreatePolygonShape(bodyId, boxDef, b2MakeOffsetBox(0.5f, 1.0f, new b2Vec2(0.25f, 0.0f), b2MakeRot(0.1f)));

        b2ShapeDef circleDef = b2DefaultShapeDef();
        circleDef.density = 0.75f;
        b2CreateCircleShape(bodyId, circleDef, new b2Circle(new b2Vec2(-0.75f, 0.25f), 0.3f));

        b2MassData massData = b2Body_GetMassData(bodyId);
        String[] parts = lines[0].split("\\s+");
        assertEquals("mass", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), b2Body_GetMass(bodyId), 0.0f);
        assertEquals(Float.parseFloat(parts[2]), b2Body_GetRotationalInertia(bodyId), 0.0f);
        assertEquals(Float.parseFloat(parts[3]), massData.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), massData.rotationalInertia, 0.0f);

        assertVec(lines[1], "local", b2Body_GetLocalCenterOfMass(bodyId));
        assertVec(lines[2], "world", b2Body_GetWorldCenterOfMass(bodyId));
        assertVec(lines[3], "massCenter", massData.center);
        assertVec(lines[4], "localPoint", b2Body_GetLocalPoint(bodyId, new b2Vec2(2.5f, 4.0f)));
        assertVec(lines[5], "worldPoint", b2Body_GetWorldPoint(bodyId, new b2Vec2(0.5f, -0.25f)));
        assertVec(lines[6], "localVector", b2Body_GetLocalVector(bodyId, new b2Vec2(1.0f, 0.5f)));
        assertVec(lines[7], "worldVector", b2Body_GetWorldVector(bodyId, new b2Vec2(1.0f, 0.5f)));
        assertVec(lines[8], "velocity", b2Body_GetLinearVelocity(bodyId));

        parts = lines[9].split("\\s+");
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
