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

final class BodyForceParityTest {
    @Test
    void forcesAndImpulsesMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_body_force_probe");

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
        command.add(root.resolve("tools/parity/box2d_body_force_probe.c").toString());
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertEquals(0, run.waitFor(), output);
        String[] lines = output.split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2();
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyId impulseBody = createDynamicCircle(worldId);
        b2Body_SetLinearVelocity(impulseBody, new b2Vec2(1.0f, -2.0f));
        b2Body_SetAngularVelocity(impulseBody, 0.25f);
        b2Vec2 impulseCenter = b2Body_GetWorldCenterOfMass(impulseBody);
        b2Body_ApplyLinearImpulse(impulseBody, new b2Vec2(1.5f, -0.75f), b2Add(impulseCenter, new b2Vec2(0.4f, -0.2f)), true);
        b2Body_ApplyLinearImpulseToCenter(impulseBody, new b2Vec2(-0.5f, 2.25f), true);
        b2Body_ApplyAngularImpulse(impulseBody, -0.35f, true);
        assertStateLine(lines[0], "impulse", impulseBody);

        b2BodyId forceBody = createDynamicCircle(worldId);
        b2Body_SetLinearVelocity(forceBody, new b2Vec2(-0.5f, 0.75f));
        b2Body_SetAngularVelocity(forceBody, -0.2f);
        b2Vec2 forceCenter = b2Body_GetWorldCenterOfMass(forceBody);
        b2Body_ApplyForce(forceBody, new b2Vec2(2.0f, 3.0f), b2Add(forceCenter, new b2Vec2(0.5f, -0.25f)), true);
        b2Body_ApplyForceToCenter(forceBody, new b2Vec2(-1.0f, 4.0f), true);
        b2Body_ApplyTorque(forceBody, 0.75f, true);
        b2World_Step(worldId, 1.0f / 30.0f, 3);
        assertStateLine(lines[1], "force1", forceBody);
        b2World_Step(worldId, 1.0f / 30.0f, 3);
        assertStateLine(lines[2], "force2", forceBody);

        b2DestroyWorld(worldId);
    }

    private static b2BodyId createDynamicCircle(b2WorldId worldId) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.linearDamping = 0.0f;
        bodyDef.angularDamping = 0.0f;
        bodyDef.gravityScale = 0.0f;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(0.25f, -0.1f), 0.8f));
        return bodyId;
    }

    private static void assertStateLine(String line, String label, b2BodyId bodyId) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Rot q = b2Body_GetRotation(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        assertEquals(Float.parseFloat(parts[1]), p.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), p.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), q.c, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), q.s, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), v.x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), v.y, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), b2Body_GetAngularVelocity(bodyId), 0.0f);
    }
}
