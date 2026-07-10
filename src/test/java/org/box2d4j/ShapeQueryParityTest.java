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

final class ShapeQueryParityTest {
    @Test
    void shapeQueriesMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_shape_query_probe");

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
        command.add(root.resolve("tools/parity/box2d_shape_query_probe.c").toString());
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

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(1.25f, -0.5f);
        bodyDef.rotation = b2MakeRot(0.35f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.7f;
        b2ShapeId polygonShapeId = b2CreatePolygonShape(bodyId, shapeDef,
            b2MakeOffsetBox(0.6f, 0.25f, new b2Vec2(0.1f, 0.2f), b2MakeRot(-0.2f)));
        b2ShapeId capsuleShapeId = b2CreateCapsuleShape(bodyId, shapeDef,
            new b2Capsule(new b2Vec2(-0.4f, -0.15f), new b2Vec2(0.35f, 0.45f), 0.18f));
        b2ShapeId circleShapeId = b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(0.15f, -0.25f), 0.32f));
        b2ShapeId segmentShapeId = b2CreateSegmentShape(bodyId, shapeDef,
            new b2Segment(new b2Vec2(-0.7f, -0.4f), new b2Vec2(0.8f, -0.1f)));

        int index = 0;
        index = assertShapeQueries(lines, index, "poly", polygonShapeId, new b2Vec2(1.35f, -0.25f),
            new b2RayCastInput(new b2Vec2(-0.25f, -0.5f), new b2Vec2(3.0f, 0.2f), 1.0f));
        index = assertShapeQueries(lines, index, "capsule", capsuleShapeId, new b2Vec2(1.1f, -0.5f),
            new b2RayCastInput(new b2Vec2(0.1f, -1.0f), new b2Vec2(2.4f, 1.4f), 1.0f));
        index = assertShapeQueries(lines, index, "circle", circleShapeId, new b2Vec2(1.35f, -0.85f),
            new b2RayCastInput(new b2Vec2(0.4f, -1.1f), new b2Vec2(2.0f, 0.8f), 1.0f));
        index = assertShapeQueries(lines, index, "segment", segmentShapeId, new b2Vec2(1.0f, -0.8f),
            new b2RayCastInput(new b2Vec2(0.3f, -1.0f), new b2Vec2(1.6f, 1.0f), 1.0f));
        assertEquals(lines.length, index);

        b2DestroyWorld(worldId);
    }

    private static int assertShapeQueries(String[] lines, int index, String label, b2ShapeId shapeId, b2Vec2 point,
                                          b2RayCastInput rayInput) {
        assertAabbLine(lines[index++], "aabb-" + label, b2Shape_GetAABB(shapeId));
        assertMassLine(lines[index++], "mass-" + label, b2Shape_GetMassData(shapeId));
        assertPointLine(lines[index++], "point-" + label, b2Shape_TestPoint(shapeId, point), b2Shape_GetClosestPoint(shapeId, point));
        assertRayLine(lines[index++], "ray-" + label, b2Shape_RayCast(shapeId, rayInput));
        return index;
    }

    private static void assertAabbLine(String line, String label, b2AABB aabb) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), aabb.lowerBound.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), aabb.lowerBound.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), aabb.upperBound.x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), aabb.upperBound.y, 0.0f);
    }

    private static void assertMassLine(String line, String label, b2MassData mass) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), mass.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), mass.center.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), mass.center.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), mass.rotationalInertia, 0.0f);
    }

    private static void assertPointLine(String line, String label, boolean hit, b2Vec2 closest) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, hit);
        assertEquals(Float.parseFloat(parts[2]), closest.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), closest.y, 0.0f);
    }

    private static void assertRayLine(String line, String label, b2CastOutput output) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]) != 0, output.hit);
        assertEquals(Float.parseFloat(parts[2]), output.point.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), output.point.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), output.normal.x, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), output.normal.y, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), output.fraction, 0.0f);
        assertEquals(Integer.parseInt(parts[7]), output.iterations);
    }
}
