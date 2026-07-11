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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class CapsuleShapeParityTest {
    @Test
    void capsuleShapeApiMatchesUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_capsule_shape_probe");

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
        command.add(root.resolve("tools/parity/box2d_capsule_shape_probe.c").toString());
        command.add("-lm");
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
        worldDef.gravity = new b2Vec2(0.0f, -10.0f);
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef groundBodyDef = b2DefaultBodyDef();
        groundBodyDef.position = new b2Vec2(0.0f, -10.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundBodyDef);
        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        groundShapeDef.enableContactEvents = true;
        b2ShapeId groundShapeId = b2CreatePolygonShape(groundId, groundShapeDef, b2MakeBox(50.0f, 10.0f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 1.15f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        shapeDef.material.friction = 0.35f;
        shapeDef.enableContactEvents = true;
        b2ShapeId capsuleShapeId = b2CreateCapsuleShape(bodyId, shapeDef,
            new b2Capsule(new b2Vec2(-0.35f, 0.0f), new b2Vec2(0.45f, 0.0f), 0.25f));

        b2Capsule capsule = b2Shape_GetCapsule(capsuleShapeId);
        assertCapsuleLine(lines[0], "capsule0", capsule);
        capsule.center1.x = 99.0f;
        assertNotEquals(99.0f, b2Shape_GetCapsule(capsuleShapeId).center1.x, 0.0f);
        assertMassLine(lines[1], "mass0", b2Body_GetMassData(bodyId));

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2ContactData[] data = new b2ContactData[4];
        int contactCount = b2Body_GetContactData(bodyId, data, data.length);
        int pointCount = contactCount > 0 ? data[0].manifold.pointCount : 0;
        assertContactsLine(lines[2], "contacts0", b2World_GetCounters(worldId).contactCount,
            b2Shape_GetContactCapacity(capsuleShapeId), b2Shape_GetContactCapacity(groundShapeId), pointCount);

        b2Shape_SetCapsule(capsuleShapeId, new b2Capsule(new b2Vec2(-0.15f, -0.1f), new b2Vec2(0.15f, 0.6f), 0.2f));
        assertCapsuleLine(lines[3], "capsule1", b2Shape_GetCapsule(capsuleShapeId));
        assertSingleIntLine(lines[4], "contacts1", b2World_GetCounters(worldId).contactCount);
        assertMassLine(lines[5], "mass1", b2Body_GetMassData(bodyId));
        b2Body_ApplyMassFromShapes(bodyId);
        assertMassLine(lines[6], "mass2", b2Body_GetMassData(bodyId));

        b2DestroyWorld(worldId);
    }

    private static void assertCapsuleLine(String line, String label, b2Capsule capsule) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), capsule.center1.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), capsule.center1.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), capsule.center2.x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), capsule.center2.y, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), capsule.radius, 0.0f);
    }

    private static void assertMassLine(String line, String label, b2MassData mass) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), mass.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), mass.center.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), mass.center.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), mass.rotationalInertia, 0.0f);
    }

    private static void assertContactsLine(String line, String label, int contactCount, int dynamicCapacity,
                                           int groundCapacity, int pointCount) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]), contactCount);
        assertEquals(Integer.parseInt(parts[2]), dynamicCapacity);
        assertEquals(Integer.parseInt(parts[3]), groundCapacity);
        assertEquals(Integer.parseInt(parts[4]), pointCount);
    }

    private static void assertSingleIntLine(String line, String label, int value) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]), value);
    }
}
