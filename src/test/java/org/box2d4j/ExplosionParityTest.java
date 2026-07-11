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

final class ExplosionParityTest {
    @Test
    void worldExplosionMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, 0.0f);
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;

        bodyDef.position = new b2Vec2(1.0f, 0.2f);
        bodyDef.rotation = b2MakeRot(0.25f);
        b2BodyId circleBody = b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.2f;
        shapeDef.filter.categoryBits = 0x0001L;
        b2CreateCircleShape(circleBody, shapeDef, new b2Circle(new b2Vec2(0.1f, -0.05f), 0.35f));

        bodyDef.position = new b2Vec2(-0.75f, 0.85f);
        bodyDef.rotation = b2MakeRot(-0.4f);
        b2BodyId capsuleBody = b2CreateBody(worldId, bodyDef);
        shapeDef = b2DefaultShapeDef();
        shapeDef.density = 0.9f;
        shapeDef.filter.categoryBits = 0x0002L;
        b2CreateCapsuleShape(capsuleBody, shapeDef, new b2Capsule(new b2Vec2(-0.25f, 0.0f), new b2Vec2(0.3f, 0.1f), 0.18f));

        bodyDef.position = new b2Vec2(0.25f, -1.15f);
        bodyDef.rotation = b2MakeRot(0.7f);
        b2BodyId boxBody = b2CreateBody(worldId, bodyDef);
        shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.5f;
        shapeDef.filter.categoryBits = 0x0001L;
        b2CreatePolygonShape(boxBody, shapeDef, b2MakeOffsetBox(0.35f, 0.25f, new b2Vec2(0.12f, -0.08f), b2MakeRot(0.2f)));

        bodyDef.position = new b2Vec2(0.45f, 0.45f);
        bodyDef.rotation = b2MakeRot(0.1f);
        b2BodyId filteredBody = b2CreateBody(worldId, bodyDef);
        shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.filter.categoryBits = 0x0004L;
        b2CreatePolygonShape(filteredBody, shapeDef, b2MakeBox(0.2f, 0.2f));

        b2ExplosionDef explosionDef = b2DefaultExplosionDef();
        explosionDef.maskBits = 0x0003L;
        explosionDef.position = new b2Vec2(0.0f, 0.0f);
        explosionDef.radius = 0.45f;
        explosionDef.falloff = 1.25f;
        explosionDef.impulsePerLength = 3.75f;
        b2World_Explode(worldId, explosionDef);

        assertBodyLine(lines[0], "circle", circleBody);
        assertBodyLine(lines[1], "capsule", capsuleBody);
        assertBodyLine(lines[2], "box", boxBody);
        assertBodyLine(lines[3], "filtered", filteredBody);

        b2DestroyWorld(worldId);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_explosion_probe");

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
        command.add(root.resolve("tools/parity/box2d_explosion_probe.c").toString());
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

    private static void assertBodyLine(String line, String label, b2BodyId bodyId) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        assertEquals(Float.parseFloat(parts[1]), velocity.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), velocity.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), b2Body_GetAngularVelocity(bodyId), 0.0f);
    }
}
