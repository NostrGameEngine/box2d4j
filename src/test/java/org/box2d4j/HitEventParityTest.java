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

final class HitEventParityTest {
    @Test
    void contactHitEventMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, 0.0f);
        worldDef.hitEventThreshold = 0.1f;
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef groundDef = b2DefaultBodyDef();
        groundDef.position = new b2Vec2(0.0f, -0.5f);
        b2BodyId groundId = b2CreateBody(worldId, groundDef);
        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        b2ShapeId groundShapeId = b2CreatePolygonShape(groundId, groundShapeDef, b2MakeBox(2.0f, 0.5f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 0.45f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2Body_SetLinearVelocity(bodyId, new b2Vec2(0.0f, -3.0f));
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.enableHitEvents = true;
        b2ShapeId dynamicShapeId = b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(0.4f, 0.4f));

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2ContactEvents events = b2World_GetContactEvents(worldId);

        assertCountsLine(lines[0], events);
        int line = 1;
        if (events.hitCount > 0) {
            assertHitLine(lines[line++], events.hitEvents[0]);
        }
        assertIdsLine(lines[line++], groundShapeId, dynamicShapeId);
        assertEquals(lines.length, line);

        b2DestroyWorld(worldId);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_hit_event_probe");

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
        command.add(root.resolve("tools/parity/box2d_hit_event_probe.c").toString());
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

    private static void assertCountsLine(String line, b2ContactEvents events) {
        String[] parts = line.split("\\s+");
        assertEquals("counts", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), events.beginCount);
        assertEquals(Integer.parseInt(parts[2]), events.endCount);
        assertEquals(Integer.parseInt(parts[3]), events.hitCount);
    }

    private static void assertHitLine(String line, b2ContactHitEvent event) {
        String[] parts = line.split("\\s+");
        assertEquals("hit", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), event.shapeIdA.index1);
        assertEquals(Integer.parseInt(parts[2]), event.shapeIdA.generation);
        assertEquals(Integer.parseInt(parts[3]), event.shapeIdB.index1);
        assertEquals(Integer.parseInt(parts[4]), event.shapeIdB.generation);
        assertEquals(Float.parseFloat(parts[5]), event.point.x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), event.point.y, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), event.normal.x, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), event.normal.y, 0.0f);
        assertEquals(Float.parseFloat(parts[9]), event.approachSpeed, 0.0f);
    }

    private static void assertIdsLine(String line, b2ShapeId groundShapeId, b2ShapeId dynamicShapeId) {
        String[] parts = line.split("\\s+");
        assertEquals("ids", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), groundShapeId.index1);
        assertEquals(Integer.parseInt(parts[2]), dynamicShapeId.index1);
    }
}
