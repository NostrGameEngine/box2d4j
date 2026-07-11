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

final class SensorEventParityTest {
    @Test
    void sensorBeginPersistEndEventsMatchUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        int line = 0;

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, 0.0f);
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef sensorBodyDef = b2DefaultBodyDef();
        b2BodyId sensorBodyId = b2CreateBody(worldId, sensorBodyDef);
        b2ShapeDef sensorShapeDef = b2DefaultShapeDef();
        sensorShapeDef.isSensor = true;
        sensorShapeDef.enableSensorEvents = true;
        b2ShapeId sensorShapeId = b2CreateCircleShape(sensorBodyId, sensorShapeDef,
            new b2Circle(new b2Vec2(0.0f, 0.0f), 1.0f));

        b2BodyDef visitorBodyDef = b2DefaultBodyDef();
        visitorBodyDef.type = b2_dynamicBody;
        visitorBodyDef.position = new b2Vec2(0.5f, 0.0f);
        visitorBodyDef.gravityScale = 0.0f;
        b2BodyId visitorBodyId = b2CreateBody(worldId, visitorBodyDef);
        b2ShapeDef visitorShapeDef = b2DefaultShapeDef();
        visitorShapeDef.density = 1.0f;
        visitorShapeDef.enableSensorEvents = true;
        b2ShapeId visitorShapeId = b2CreateCircleShape(visitorBodyId, visitorShapeDef,
            new b2Circle(new b2Vec2(0.0f, 0.0f), 0.25f));

        assertIdsLine(lines[line++], sensorShapeId, visitorShapeId);

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2SensorEvents events = b2World_GetSensorEvents(worldId);
        assertCountsLine(lines[line++], "step1", events);
        assertBeginLine(lines[line++], "step1", events.beginEvents[0]);
        b2ShapeId[] overlaps = new b2ShapeId[4];
        int overlapCount = b2Shape_GetSensorOverlaps(sensorShapeId, overlaps, 4);
        assertOverlapLine(lines[line++], "step1", b2Shape_GetSensorCapacity(sensorShapeId), overlapCount, overlaps);

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        events = b2World_GetSensorEvents(worldId);
        assertCountsLine(lines[line++], "step2", events);
        overlapCount = b2Shape_GetSensorOverlaps(sensorShapeId, overlaps, 4);
        assertOverlapLine(lines[line++], "step2", b2Shape_GetSensorCapacity(sensorShapeId), overlapCount, overlaps);

        b2Body_SetTransform(visitorBodyId, new b2Vec2(3.0f, 0.0f), b2MakeRot(0.0f));
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        events = b2World_GetSensorEvents(worldId);
        assertCountsLine(lines[line++], "step3", events);
        assertEndLine(lines[line++], "step3", events.endEvents[0]);
        overlapCount = b2Shape_GetSensorOverlaps(sensorShapeId, overlaps, 4);
        assertEmptyOverlapLine(lines[line++], "step3", b2Shape_GetSensorCapacity(sensorShapeId), overlapCount);

        assertEquals(lines.length, line);
        b2DestroyWorld(worldId);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_sensor_event_probe");

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
        command.add(root.resolve("tools/parity/box2d_sensor_event_probe.c").toString());
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

    private static void assertIdsLine(String line, b2ShapeId sensorShapeId, b2ShapeId visitorShapeId) {
        String[] parts = line.split("\\s+");
        assertEquals("ids", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), sensorShapeId.index1);
        assertEquals(Integer.parseInt(parts[2]), sensorShapeId.generation);
        assertEquals(Integer.parseInt(parts[3]), visitorShapeId.index1);
        assertEquals(Integer.parseInt(parts[4]), visitorShapeId.generation);
    }

    private static void assertCountsLine(String line, String label, b2SensorEvents events) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals("counts", parts[1]);
        assertEquals(Integer.parseInt(parts[2]), events.beginCount);
        assertEquals(Integer.parseInt(parts[3]), events.endCount);
    }

    private static void assertBeginLine(String line, String label, b2SensorBeginTouchEvent event) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals("begin", parts[1]);
        assertEquals(Integer.parseInt(parts[2]), event.sensorShapeId.index1);
        assertEquals(Integer.parseInt(parts[3]), event.sensorShapeId.generation);
        assertEquals(Integer.parseInt(parts[4]), event.visitorShapeId.index1);
        assertEquals(Integer.parseInt(parts[5]), event.visitorShapeId.generation);
    }

    private static void assertEndLine(String line, String label, b2SensorEndTouchEvent event) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals("end", parts[1]);
        assertEquals(Integer.parseInt(parts[2]), event.sensorShapeId.index1);
        assertEquals(Integer.parseInt(parts[3]), event.sensorShapeId.generation);
        assertEquals(Integer.parseInt(parts[4]), event.visitorShapeId.index1);
        assertEquals(Integer.parseInt(parts[5]), event.visitorShapeId.generation);
    }

    private static void assertOverlapLine(String line, String label, int capacity, int count, b2ShapeId[] overlaps) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals("overlaps", parts[1]);
        assertEquals(Integer.parseInt(parts[2]), capacity);
        assertEquals(Integer.parseInt(parts[3]), count);
        assertEquals(Integer.parseInt(parts[4]), count > 0 ? overlaps[0].index1 : 0);
        assertEquals(Integer.parseInt(parts[5]), count > 0 ? overlaps[0].generation : 0);
    }

    private static void assertEmptyOverlapLine(String line, String label, int capacity, int count) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals("overlaps", parts[1]);
        assertEquals(Integer.parseInt(parts[2]), capacity);
        assertEquals(Integer.parseInt(parts[3]), count);
    }
}
