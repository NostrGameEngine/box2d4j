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

final class MoverParityTest {
    @Test
    void moverWorldApisMatchUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId groundId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2ShapeId boxId = b2CreatePolygonShape(groundId, shapeDef, b2MakeBox(1.0f, 1.0f));

        b2Capsule castMover = new b2Capsule(new b2Vec2(-3.0f, -0.5f), new b2Vec2(-3.0f, 0.5f), 0.35f);
        float fraction = b2World_CastMover(worldId, castMover, new b2Vec2(5.0f, 0.0f), b2DefaultQueryFilter());
        assertScalarLine(lines[0], "cast", fraction);

        b2Capsule collideMover = new b2Capsule(new b2Vec2(1.2f, -0.5f), new b2Vec2(1.2f, 0.5f), 0.35f);
        List<b2ShapeId> shapeIds = new ArrayList<>();
        List<b2PlaneResult> planes = new ArrayList<>();
        b2World_CollideMover(worldId, collideMover, b2DefaultQueryFilter(), (shapeId, plane) -> {
            shapeIds.add(shapeId);
            planes.add(plane);
            return true;
        });

        assertPlaneLine(lines[1], shapeIds.get(0), planes.get(0));
        assertCountLine(lines[2], shapeIds.size(), boxId);
        b2DestroyWorld(worldId);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_mover_probe");

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
        command.add(root.resolve("tools/parity/box2d_mover_probe.c").toString());
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

    private static void assertScalarLine(String line, String label, float value) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), value, 0.0f);
    }

    private static void assertPlaneLine(String line, b2ShapeId shapeId, b2PlaneResult plane) {
        String[] parts = line.split("\\s+");
        assertEquals("plane", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), shapeId.index1);
        assertEquals(Integer.parseInt(parts[2]), shapeId.generation);
        assertEquals(Float.parseFloat(parts[3]), plane.plane.normal.x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), plane.plane.normal.y, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), plane.plane.offset, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), plane.point.x, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), plane.point.y, 0.0f);
        assertEquals(Integer.parseInt(parts[8]) != 0, plane.hit);
    }

    private static void assertCountLine(String line, int count, b2ShapeId boxId) {
        String[] parts = line.split("\\s+");
        assertEquals("count", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), count);
        assertEquals(Integer.parseInt(parts[2]), boxId.index1);
    }
}
