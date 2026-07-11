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

final class WorldLifecycleParityTest {
    @Test
    void upstreamWorldLifecycleSlicesMatchC() throws Exception {
        String[] lines = runProbe().split("\\R");
        assertEquals(4, lines.length);
        assertEmptyWorldLine(lines[0]);
        assertDestroyAllBodiesLine(lines[1]);
        assertIdValidityLine(lines[2]);
        assertWorldRecycleLine(lines[3]);
    }

    private static void assertEmptyWorldLine(String line) {
        String[] parts = line.split("\\s+");
        assertEquals("empty", parts[0]);

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        int validStart = b2World_IsValid(worldId) ? 1 : 0;
        for (int i = 0; i < 60; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 1);
        }
        b2Counters counters = b2World_GetCounters(worldId);
        b2DestroyWorld(worldId);

        assertEquals(Integer.parseInt(parts[1]), validStart);
        assertEquals(Integer.parseInt(parts[2]), counters.bodyCount);
        assertEquals(Integer.parseInt(parts[3]), counters.shapeCount);
        assertEquals(Integer.parseInt(parts[4]), counters.contactCount);
        assertEquals(Integer.parseInt(parts[5]), b2World_IsValid(worldId) ? 1 : 0);
    }

    private static void assertDestroyAllBodiesLine(String line) {
        String[] parts = line.split("\\s+");
        assertEquals("destroyAll", parts[0]);

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        int validStart = b2World_IsValid(worldId) ? 1 : 0;
        b2BodyId[] bodyIds = new b2BodyId[10];
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2Polygon square = b2MakeSquare(0.5f);
        int count = 0;
        boolean creating = true;
        for (int i = 0; i < 2 * bodyIds.length + 10; ++i) {
            if (creating) {
                if (count < bodyIds.length) {
                    bodyIds[count] = b2CreateBody(worldId, bodyDef);
                    b2CreatePolygonShape(bodyIds[count], b2DefaultShapeDef(), square);
                    count += 1;
                } else {
                    creating = false;
                }
            } else if (count > 0) {
                b2DestroyBody(bodyIds[count - 1]);
                bodyIds[count - 1] = b2_nullBodyId;
                count -= 1;
            }
            b2World_Step(worldId, 1.0f / 60.0f, 3);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        b2DestroyWorld(worldId);

        assertEquals(Integer.parseInt(parts[1]), validStart);
        assertEquals(Integer.parseInt(parts[2]), counters.bodyCount);
        assertEquals(Integer.parseInt(parts[3]), counters.shapeCount);
        assertEquals(Integer.parseInt(parts[4]), counters.contactCount);
        assertEquals(Integer.parseInt(parts[5]), b2World_IsValid(worldId) ? 1 : 0);
    }

    private static void assertIdValidityLine(String line) {
        String[] parts = line.split("\\s+");
        assertEquals("isValid", parts[0]);

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId bodyId1 = b2CreateBody(worldId, b2DefaultBodyDef());
        b2BodyId bodyId2 = b2CreateBody(worldId, b2DefaultBodyDef());

        int worldIndex = Integer.parseInt(parts[1]) >>> 16;
        assertEquals(worldIndex, worldId.index1);
        assertEquals(Long.parseUnsignedLong(parts[2]), b2StoreBodyId(bodyId1));
        assertEquals(Long.parseUnsignedLong(parts[3]), b2StoreBodyId(bodyId2));
        assertEquals(Integer.parseInt(parts[4]), b2Body_IsValid(bodyId1) ? 1 : 0);
        assertEquals(Integer.parseInt(parts[5]), b2Body_IsValid(bodyId2) ? 1 : 0);

        b2DestroyBody(bodyId1);
        assertEquals(Integer.parseInt(parts[6]), b2Body_IsValid(bodyId1) ? 1 : 0);
        b2DestroyBody(bodyId2);
        assertEquals(Integer.parseInt(parts[7]), b2Body_IsValid(bodyId2) ? 1 : 0);
        b2DestroyWorld(worldId);

        assertEquals(Integer.parseInt(parts[8]), b2World_IsValid(worldId) ? 1 : 0);
        assertEquals(Integer.parseInt(parts[9]), b2Body_IsValid(bodyId1) ? 1 : 0);
        assertEquals(Integer.parseInt(parts[10]), b2Body_IsValid(bodyId2) ? 1 : 0);
    }

    private static void assertWorldRecycleLine(String line) {
        String[] parts = line.split("\\s+");
        assertEquals("recycle", parts[0]);
        int worldCount = Integer.parseInt(parts[1]);
        int iterationCount = Integer.parseInt(parts[2]);
        assertEquals(B2_MAX_WORLDS / 2, worldCount);

        b2WorldId[] worldIds = new b2WorldId[worldCount];
        b2WorldId firstId = null;
        b2WorldId firstSlotLastId = null;
        b2WorldId lastSlotFirstId = null;
        b2WorldId lastId = null;

        for (int i = 0; i < iterationCount; ++i) {
            for (int j = 0; j < worldCount; ++j) {
                worldIds[j] = b2CreateWorld(b2DefaultWorldDef());
                if (i == 0 && j == 0) {
                    firstId = worldIds[j];
                }
                if (i == iterationCount - 1 && j == 0) {
                    firstSlotLastId = worldIds[j];
                }
                if (i == 0 && j == worldCount - 1) {
                    lastSlotFirstId = worldIds[j];
                }
                if (i == iterationCount - 1 && j == worldCount - 1) {
                    lastId = worldIds[j];
                }
                b2CreateBody(worldIds[j], b2DefaultBodyDef());
            }

            for (int j = 0; j < worldCount; ++j) {
                for (int k = 0; k < 10; ++k) {
                    b2World_Step(worldIds[j], 1.0f / 60.0f, 1);
                }
            }

            for (int j = worldCount - 1; j >= 0; --j) {
                b2DestroyWorld(worldIds[j]);
            }
        }

        b2WorldId cFirst = b2LoadWorldId(Integer.parseUnsignedInt(parts[3]));
        b2WorldId cFirstSlotLast = b2LoadWorldId(Integer.parseUnsignedInt(parts[4]));
        b2WorldId cLastSlotFirst = b2LoadWorldId(Integer.parseUnsignedInt(parts[5]));
        b2WorldId cLast = b2LoadWorldId(Integer.parseUnsignedInt(parts[6]));
        assertEquals(cFirst.index1, firstId.index1);
        assertEquals(cFirstSlotLast.index1, firstSlotLastId.index1);
        assertEquals(cLastSlotFirst.index1, lastSlotFirstId.index1);
        assertEquals(cLast.index1, lastId.index1);
        assertEquals(cFirstSlotLast.generation - cFirst.generation, firstSlotLastId.generation - firstId.generation);
        assertEquals(cLast.generation - cLastSlotFirst.generation, lastId.generation - lastSlotFirstId.generation);
        assertEquals(Integer.parseInt(parts[7]), b2World_IsValid(worldIds[0]) ? 1 : 0);
        assertEquals(Integer.parseInt(parts[8]), b2World_IsValid(worldIds[worldCount - 1]) ? 1 : 0);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_world_lifecycle_probe");

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
        command.add(root.resolve("tools/parity/box2d_world_lifecycle_probe.c").toString());
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
