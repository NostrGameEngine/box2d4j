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

final class SmallFallingHingesParityTest {
    @Test
    void smallFallingHingesSlicesMatchUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        int line = 0;

        line = assertScenario(lines, line, "small4", 4);
        line = assertScenario(lines, line, "small8", 8);
        line = assertScenario(lines, line, "small16", 16);
        line = assertScenario(lines, line, "small24", 24);
        line = assertScenario(lines, line, "small32", 32);
        line = assertScenario(lines, line, "small48", 48);
        line = assertScenario(lines, line, "small64", 64);
        line = assertScenario(lines, line, "small120", 120);
        line = assertGridScenario(lines, line, "grid120", 90);
        line = assertGridScenario(lines, line, "grid120long", 165);
        assertEquals(lines.length, line);
    }

    private static int assertScenario(String[] lines, int line, String prefix, int bodyCount) {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.enableSleep = false;
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef groundDef = b2DefaultBodyDef();
        groundDef.position = new b2Vec2(0.0f, -1.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundDef);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(20.0f, 1.0f));

        b2BodyId[] bodyIds = createSmallFallingHinges(worldId, bodyCount);
        for (int i = 0; i < 90; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        b2BodyEvents events = b2World_GetBodyEvents(worldId);
        assertHeaderLine(lines[line++], prefix, counters.bodyCount, counters.contactCount, events.moveCount);
        for (int i = 0; i < bodyIds.length; ++i) {
            assertBodyLine(lines[line++], prefix, i, bodyIds[i]);
        }

        b2DestroyWorld(worldId);
        return line;
    }

    private static int assertGridScenario(String[] lines, int line, String prefix, int stepCount) {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.enableSleep = false;
        b2WorldId worldId = b2CreateWorld(worldDef);

        b2BodyDef groundDef = b2DefaultBodyDef();
        groundDef.position = new b2Vec2(0.0f, -1.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundDef);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(20.0f, 1.0f));

        b2BodyId[] bodyIds = createGridFallingHinges(worldId);
        for (int i = 0; i < stepCount; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        b2BodyEvents events = b2World_GetBodyEvents(worldId);
        assertHeaderLine(lines[line++], prefix, counters.bodyCount, counters.contactCount, events.moveCount);
        for (int i = 0; i < bodyIds.length; ++i) {
            assertBodyLine(lines[line++], prefix, i, bodyIds[i]);
        }

        b2DestroyWorld(worldId);
        return line;
    }

    private static b2BodyId[] createSmallFallingHinges(b2WorldId worldId, int bodyCount) {
        b2BodyId[] bodyIds = new b2BodyId[bodyCount];

        float h = 0.25f;
        float r = 0.1f * h;
        float offset = 0.4f * h;
        b2Polygon box = b2MakeRoundedBox(h - r, h - r, r);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.3f;

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.enableLimit = true;
        jointDef.lowerAngle = -0.1f * B2_PI;
        jointDef.upperAngle = 0.2f * B2_PI;
        jointDef.enableSpring = true;
        jointDef.hertz = 0.5f;
        jointDef.dampingRatio = 0.5f;
        jointDef.localAnchorA = new b2Vec2(h, h);
        jointDef.localAnchorB = new b2Vec2(offset, -h);

        b2BodyId prevBodyId = b2_nullBodyId;
        for (int i = 0; i < bodyCount; ++i) {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.enableSleep = false;
            bodyDef.position = new b2Vec2(offset * i, h + 2.0f * h * i);
            bodyDef.rotation = b2MakeRot(0.1f * i - 1.0f);

            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            if ((i & 1) == 0) {
                prevBodyId = bodyId;
            } else {
                jointDef.bodyIdA = prevBodyId;
                jointDef.bodyIdB = bodyId;
                b2CreateRevoluteJoint(worldId, jointDef);
                prevBodyId = b2_nullBodyId;
            }

            b2CreatePolygonShape(bodyId, shapeDef, box);
            bodyIds[i] = bodyId;
        }
        return bodyIds;
    }

    private static b2BodyId[] createGridFallingHinges(b2WorldId worldId) {
        int columnCount = 4;
        int rowCount = 30;
        b2BodyId[] bodyIds = new b2BodyId[columnCount * rowCount];

        float h = 0.25f;
        float r = 0.1f * h;
        float offset = 0.4f * h;
        float dx = 10.0f * h;
        float xroot = -0.5f * dx * (columnCount - 1.0f);
        b2Polygon box = b2MakeRoundedBox(h - r, h - r, r);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.3f;

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.enableLimit = true;
        jointDef.lowerAngle = -0.1f * B2_PI;
        jointDef.upperAngle = 0.2f * B2_PI;
        jointDef.enableSpring = true;
        jointDef.hertz = 0.5f;
        jointDef.dampingRatio = 0.5f;
        jointDef.localAnchorA = new b2Vec2(h, h);
        jointDef.localAnchorB = new b2Vec2(offset, -h);
        jointDef.drawSize = 0.1f;

        int bodyIndex = 0;
        for (int j = 0; j < columnCount; ++j) {
            float x = xroot + j * dx;
            b2BodyId prevBodyId = b2_nullBodyId;
            for (int i = 0; i < rowCount; ++i) {
                b2BodyDef bodyDef = b2DefaultBodyDef();
                bodyDef.type = b2_dynamicBody;
                bodyDef.enableSleep = false;
                bodyDef.position = new b2Vec2(x + offset * i, h + 2.0f * h * i);
                bodyDef.rotation = b2MakeRot(0.1f * i - 1.0f);

                b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
                if ((i & 1) == 0) {
                    prevBodyId = bodyId;
                } else {
                    jointDef.bodyIdA = prevBodyId;
                    jointDef.bodyIdB = bodyId;
                    b2CreateRevoluteJoint(worldId, jointDef);
                    prevBodyId = b2_nullBodyId;
                }

                b2CreatePolygonShape(bodyId, shapeDef, box);
                bodyIds[bodyIndex++] = bodyId;
            }
        }

        assertEquals(bodyIds.length, bodyIndex);
        return bodyIds;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_small_falling_hinges_probe");

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
        command.add(root.resolve("tools/parity/box2d_small_falling_hinges_probe.c").toString());
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

    private static void assertHeaderLine(String line, String prefix, int bodyCount, int contactCount, int moveCount) {
        String[] parts = line.split("\\s+");
        assertEquals(prefix + "FallingHinges", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), bodyCount);
        assertEquals(Integer.parseInt(parts[2]), contactCount);
        assertEquals(Integer.parseInt(parts[3]), moveCount);
    }

    private static void assertBodyLine(String line, String prefix, int expectedIndex, b2BodyId bodyId) {
        String[] parts = line.split("\\s+");
        assertEquals(prefix + "Body", parts[0]);
        assertEquals(expectedIndex, Integer.parseInt(parts[1]));
        b2Transform transform = b2Body_GetTransform(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        assertEquals(Float.parseFloat(parts[2]), transform.p.x, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), transform.p.y, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), transform.q.c, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), transform.q.s, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), velocity.x, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), velocity.y, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), b2Body_GetAngularVelocity(bodyId), 0.0f);
    }
}
