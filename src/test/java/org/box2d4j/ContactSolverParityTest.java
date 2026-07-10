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

final class ContactSolverParityTest {
    @Test
    void helloWorldContactSolverSettlesLikeUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_hello_contact_probe");

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
        command.add(root.resolve("tools/parity/box2d_hello_contact_probe.c").toString());
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String[] lines = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(0, run.waitFor());
        String[] body = lines[0].split("\\s+");
        assertEquals("body", body[0]);

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, -10.0f);
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef groundBodyDef = b2DefaultBodyDef();
        groundBodyDef.position = new b2Vec2(0.0f, -10.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundBodyDef);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(50.0f, 10.0f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 4.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.3f;
        b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(1.0f, 1.0f));

        for (int i = 0; i < 90; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Rot q = b2Body_GetRotation(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);

        assertEquals(Float.parseFloat(body[1]), p.x, 2.0e-7f);
        assertEquals(Float.parseFloat(body[2]), p.y, 2.0e-7f);
        assertEquals(Float.parseFloat(body[3]), b2Rot_GetAngle(q), 2.0e-7f);
        assertEquals(Float.parseFloat(body[4]), v.x, 0.0f);
        assertEquals(Float.parseFloat(body[5]), v.y, 0.0f);
        assertEquals(Float.parseFloat(body[6]), b2Body_GetAngularVelocity(bodyId), 0.0f);
        assertEquals(0, b2World_GetAwakeBodyCount(worldId));

        b2DestroyWorld(worldId);
    }
}
