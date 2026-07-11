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

final class ShapeFilterParityTest {
    @Test
    void shapeFilterAndEventApiMatchesUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_shape_filter_probe");

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
        command.add(root.resolve("tools/parity/box2d_shape_filter_probe.c").toString());
        command.add("-lm");
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String[] lines = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(0, run.waitFor());

        b2WorldDef worldDef = b2DefaultWorldDef();
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 0.0f);
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        bodyDef.position = new b2Vec2(0.5f, 0.0f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.enableContactEvents = true;
        b2ShapeId shapeA = b2CreatePolygonShape(bodyA, shapeDef, b2MakeBox(1.0f, 1.0f));
        b2ShapeId shapeB = b2CreatePolygonShape(bodyB, shapeDef, b2MakeBox(1.0f, 1.0f));

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        assertInts(lines[0], "before", b2World_GetCounters(worldId).contactCount, b2Body_GetContactCapacity(bodyA));

        b2Filter filter = b2Shape_GetFilter(shapeA);
        assertFilterLine(lines[1], "filter", filter);
        filter.maskBits = 0;
        b2Shape_SetFilter(shapeA, filter);
        filter.maskBits = 0xffff;
        assertFilterAfterSet(lines[2], b2Shape_GetFilter(shapeA), b2World_GetCounters(worldId).contactCount);

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        assertInts(lines[3], "afterStep", b2World_GetCounters(worldId).contactCount, b2Body_GetContactCapacity(bodyA));

        b2Shape_EnableSensorEvents(shapeB, true);
        b2Shape_EnableContactEvents(shapeB, false);
        b2Shape_EnablePreSolveEvents(shapeB, true);
        b2Shape_EnableHitEvents(shapeB, true);
        assertBools(lines[4], b2Shape_AreSensorEventsEnabled(shapeB), b2Shape_AreContactEventsEnabled(shapeB),
            b2Shape_ArePreSolveEventsEnabled(shapeB), b2Shape_AreHitEventsEnabled(shapeB));

        b2DestroyWorld(worldId);
    }

    private static void assertInts(String line, String label, int a, int b) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]), a);
        assertEquals(Integer.parseInt(parts[2]), b);
    }

    private static void assertFilterLine(String line, String label, b2Filter filter) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Long.parseUnsignedLong(parts[1]), filter.categoryBits);
        assertEquals(Long.parseUnsignedLong(parts[2]), filter.maskBits);
        assertEquals(Integer.parseInt(parts[3]), filter.groupIndex);
    }

    private static void assertFilterAfterSet(String line, b2Filter filter, int contactCount) {
        String[] parts = line.split("\\s+");
        assertEquals("afterSet", parts[0]);
        assertEquals(Long.parseUnsignedLong(parts[1]), filter.categoryBits);
        assertEquals(Long.parseUnsignedLong(parts[2]), filter.maskBits);
        assertEquals(Integer.parseInt(parts[3]), filter.groupIndex);
        assertEquals(Integer.parseInt(parts[4]), contactCount);
    }

    private static void assertBools(String line, boolean sensor, boolean contact, boolean preSolve, boolean hit) {
        String[] parts = line.split("\\s+");
        assertEquals("events", parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, sensor);
        assertEquals(Integer.parseInt(parts[2]) != 0, contact);
        assertEquals(Integer.parseInt(parts[3]) != 0, preSolve);
        assertEquals(Integer.parseInt(parts[4]) != 0, hit);
    }
}
