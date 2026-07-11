package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DeterminismParityTest {
    @Test
    void fallingHingesSleepStepMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        String[] expected = lines[0].split("\\s+");
        assertEquals("determinism", expected[0]);
        assertEquals(0x35467e1e, (int) Long.parseLong(expected[2], 16));

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        FallingHingeData data = createFallingHinges(worldId);

        boolean done = false;
        while (!done) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            done = updateFallingHinges(worldId, data);
        }

        assertEquals(Integer.parseInt(expected[1]), data.sleepStep);
        assertEquals((int) Long.parseLong(expected[2], 16), data.hash);

        b2DestroyWorld(worldId);
    }

    private static FallingHingeData createFallingHinges(b2WorldId worldId) {
        b2BodyDef groundDef = b2DefaultBodyDef();
        groundDef.position = new b2Vec2(0.0f, -1.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundDef);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(20.0f, 1.0f));

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
        return new FallingHingeData(bodyIds);
    }

    private static boolean updateFallingHinges(b2WorldId worldId, FallingHingeData data) {
        if (data.hash == 0) {
            b2BodyEvents bodyEvents = b2World_GetBodyEvents(worldId);
            if (bodyEvents.moveCount == 0) {
                assertEquals(0, b2World_GetAwakeBodyCount(worldId));
                int hash = B2_HASH_INIT;
                ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
                for (b2BodyId bodyId : data.bodyIds) {
                    b2Transform transform = b2Body_GetTransform(bodyId);
                    buffer.clear();
                    buffer.putFloat(transform.p.x);
                    buffer.putFloat(transform.p.y);
                    buffer.putFloat(transform.q.c);
                    buffer.putFloat(transform.q.s);
                    hash = b2Hash(hash, buffer.array(), buffer.capacity());
                }
                data.hash = hash;
                data.sleepStep = data.stepCount;
            }
        }

        data.stepCount += 1;
        assertTrue(data.stepCount < 1000, "falling hinges did not sleep");
        return data.hash != 0;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_determinism_probe");

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
        command.add("-I" + root.resolve("vendor/box2d/shared"));
        command.addAll(sources);
        command.add(root.resolve("vendor/box2d/shared/determinism.c").toString());
        command.add(root.resolve("tools/parity/box2d_determinism_probe.c").toString());
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

    private static final class FallingHingeData {
        final b2BodyId[] bodyIds;
        int stepCount;
        int sleepStep = -1;
        int hash;

        FallingHingeData(b2BodyId[] bodyIds) {
            this.bodyIds = bodyIds;
        }
    }
}
