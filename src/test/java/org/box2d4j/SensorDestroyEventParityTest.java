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

final class SensorDestroyEventParityTest {
    @Test
    void destroyingAnOverlappingSensorDefersItsEndEventLikeUpstream() throws Exception {
        assertEquals(runProbe(), "sensorDestroyEvent" + runCase(false) + runCase(true)
            + runVisitorCase(false) + runVisitorCase(true));
    }

    private static String runCase(boolean destroyBody) {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyId sensorBodyId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef sensorDef = b2DefaultShapeDef();
        sensorDef.isSensor = true;
        sensorDef.enableSensorEvents = true;
        b2ShapeId sensorShapeId = b2CreateCircleShape(sensorBodyId, sensorDef,
            new b2Circle(new b2Vec2(), 1.0f));

        b2BodyDef visitorBodyDef = b2DefaultBodyDef();
        visitorBodyDef.type = b2_dynamicBody;
        b2BodyId visitorBodyId = b2CreateBody(worldId, visitorBodyDef);
        b2ShapeDef visitorDef = b2DefaultShapeDef();
        visitorDef.enableSensorEvents = true;
        b2ShapeId visitorShapeId = b2CreateCircleShape(visitorBodyId, visitorDef,
            new b2Circle(new b2Vec2(), 0.5f));

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        int beginCount = b2World_GetSensorEvents(worldId).beginCount;
        if (destroyBody) {
            b2DestroyBody(sensorBodyId);
        } else {
            b2DestroyShape(sensorShapeId, false);
        }
        int immediateEndCount = b2World_GetSensorEvents(worldId).endCount;
        b2World_Step(worldId, 0.0f, 1);
        b2SensorEvents events = b2World_GetSensorEvents(worldId);
        int deferredEndCount = events.endCount;
        int sensorIndex = deferredEndCount > 0 ? events.endEvents[0].sensorShapeId.index1 : 0;
        int visitorIndex = deferredEndCount > 0 ? events.endEvents[0].visitorShapeId.index1 : 0;
        int visitorValid = b2Shape_IsValid(visitorShapeId) ? 1 : 0;
        b2DestroyWorld(worldId);
        return String.format(" %d %d %d %d %d %d %d", destroyBody ? 1 : 0, beginCount, immediateEndCount,
            deferredEndCount, sensorIndex, visitorIndex, visitorValid);
    }

    private static String runVisitorCase(boolean destroyBody) {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyId sensorBodyId = b2CreateBody(worldId, b2DefaultBodyDef());
        b2ShapeDef sensorDef = b2DefaultShapeDef();
        sensorDef.isSensor = true;
        sensorDef.enableSensorEvents = true;
        b2ShapeId sensorShapeId = b2CreateCircleShape(sensorBodyId, sensorDef,
            new b2Circle(new b2Vec2(), 1.0f));

        b2BodyDef visitorBodyDef = b2DefaultBodyDef();
        visitorBodyDef.type = b2_dynamicBody;
        b2BodyId visitorBodyId = b2CreateBody(worldId, visitorBodyDef);
        b2ShapeDef visitorDef = b2DefaultShapeDef();
        visitorDef.enableSensorEvents = true;
        b2ShapeId visitorShapeId = b2CreateCircleShape(visitorBodyId, visitorDef,
            new b2Circle(new b2Vec2(), 0.5f));

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        int beginCount = b2World_GetSensorEvents(worldId).beginCount;
        if (destroyBody) {
            b2DestroyBody(visitorBodyId);
        } else {
            b2DestroyShape(visitorShapeId, false);
        }
        b2World_Step(worldId, 0.0f, 1);
        int zeroStepEndCount = b2World_GetSensorEvents(worldId).endCount;
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2SensorEvents events = b2World_GetSensorEvents(worldId);
        int positiveStepEndCount = events.endCount;
        int sensorIndex = positiveStepEndCount > 0 ? events.endEvents[0].sensorShapeId.index1 : 0;
        int visitorIndex = positiveStepEndCount > 0 ? events.endEvents[0].visitorShapeId.index1 : 0;
        int sensorValid = b2Shape_IsValid(sensorShapeId) ? 1 : 0;
        b2DestroyWorld(worldId);
        return String.format(" %d %d %d %d %d %d %d", destroyBody ? 1 : 0, beginCount, zeroStepEndCount,
            positiveStepEndCount, sensorIndex, visitorIndex, sensorValid);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_sensor_destroy_event_probe");

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
        command.add(root.resolve("tools/parity/box2d_sensor_destroy_event_probe.c").toString());
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
}
