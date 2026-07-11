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

final class ShapeGeometryParityTest {
    @Test
    void shapeGeometryApiMatchesUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_shape_geometry_probe");

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
        command.add(root.resolve("tools/parity/box2d_shape_geometry_probe.c").toString());
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
        worldDef.gravity = new b2Vec2(0.0f, 0.0f);
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(5.0f, 0.5f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 0.75f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        b2ShapeId shapeId = b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(0.5f, 0.5f));

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        assertContactsLine(lines[0], "contacts0", b2World_GetCounters(worldId).contactCount);
        b2Polygon polygon0 = b2Shape_GetPolygon(shapeId);
        assertPolygonLine(lines[1], "poly0", polygon0);
        polygon0.vertices[0].x = 99.0f;
        assertNotEquals(99.0f, b2Shape_GetPolygon(shapeId).vertices[0].x, 0.0f);

        b2Shape_SetCircle(shapeId, new b2Circle(new b2Vec2(0.2f, -0.1f), 0.35f));
        assertContactsLine(lines[2], "contacts1", b2World_GetCounters(worldId).contactCount);
        b2Circle circle1 = b2Shape_GetCircle(shapeId);
        assertCircleLine(lines[3], "circle1", circle1);
        circle1.center.x = 99.0f;
        assertNotEquals(99.0f, b2Shape_GetCircle(shapeId).center.x, 0.0f);

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        assertContactsLine(lines[4], "contacts2", b2World_GetCounters(worldId).contactCount);

        b2Shape_SetSegment(shapeId, new b2Segment(new b2Vec2(-0.75f, -0.2f), new b2Vec2(0.9f, -0.2f)));
        assertContactsLine(lines[5], "contacts3", b2World_GetCounters(worldId).contactCount);
        b2Segment segment1 = b2Shape_GetSegment(shapeId);
        assertSegmentLine(lines[6], "segment1", segment1);
        segment1.point1.x = 99.0f;
        assertNotEquals(99.0f, b2Shape_GetSegment(shapeId).point1.x, 0.0f);

        b2Shape_SetPolygon(shapeId, b2MakeOffsetBox(0.25f, 0.75f, new b2Vec2(0.1f, -0.2f), b2MakeRot(0.35f)));
        assertContactsLine(lines[7], "contacts4", b2World_GetCounters(worldId).contactCount);
        assertPolygonLine(lines[8], "poly1", b2Shape_GetPolygon(shapeId));

        b2DestroyWorld(worldId);
    }

    private static void assertContactsLine(String line, String label, int contactCount) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]), contactCount);
    }

    private static void assertCircleLine(String line, String label, b2Circle circle) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), circle.center.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), circle.center.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), circle.radius, 0.0f);
    }

    private static void assertSegmentLine(String line, String label, b2Segment segment) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), segment.point1.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), segment.point1.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), segment.point2.x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), segment.point2.y, 0.0f);
    }

    private static void assertPolygonLine(String line, String label, b2Polygon polygon) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]), polygon.count);
        assertEquals(Float.parseFloat(parts[2]), polygon.radius, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), polygon.centroid.x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), polygon.centroid.y, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), polygon.vertices[0].x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), polygon.vertices[0].y, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), polygon.vertices[1].x, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), polygon.vertices[1].y, 0.0f);
        assertEquals(Float.parseFloat(parts[9]), polygon.normals[0].x, 0.0f);
        assertEquals(Float.parseFloat(parts[10]), polygon.normals[0].y, 0.0f);
        assertEquals(Float.parseFloat(parts[11]), polygon.normals[1].x, 0.0f);
        assertEquals(Float.parseFloat(parts[12]), polygon.normals[1].y, 0.0f);
    }
}
