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

final class ChainParityTest {
    @Test
    void chainsMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_chain_probe");

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
        command.add(root.resolve("tools/parity/box2d_chain_probe.c").toString());
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertEquals(0, run.waitFor(), output);
        String[] lines = output.split("\\R");

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId bodyId = b2CreateBody(worldId, b2DefaultBodyDef());

        b2Vec2[] points = {new b2Vec2(-2.0f, 0.0f), new b2Vec2(-1.0f, 0.0f), new b2Vec2(0.0f, 0.5f),
            new b2Vec2(1.0f, 0.0f), new b2Vec2(2.0f, 0.0f)};
        b2SurfaceMaterial[] materials = {b2DefaultSurfaceMaterial(), b2DefaultSurfaceMaterial(), b2DefaultSurfaceMaterial(),
            b2DefaultSurfaceMaterial(), b2DefaultSurfaceMaterial()};
        materials[1].friction = 0.11f;
        materials[1].restitution = 0.21f;
        materials[1].userMaterialId = 31;
        materials[2].friction = 0.12f;
        materials[2].restitution = 0.22f;
        materials[2].userMaterialId = 32;

        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = points;
        chainDef.count = 5;
        chainDef.materials = materials;
        chainDef.materialCount = 5;
        chainDef.filter.categoryBits = 0x0002L;
        chainDef.filter.maskBits = 0xFFFFL;
        b2ChainId chainId = b2CreateChain(bodyId, chainDef);

        assertChainLine(lines[0], chainId, b2Chain_IsValid(chainId), b2Chain_GetSegmentCount(chainId));
        b2ShapeId[] segments = new b2ShapeId[4];
        int segmentCount = b2Chain_GetSegments(chainId, segments, 4);
        assertEquals("segments " + segmentCount + " " + segments[0].index1 + " " + segments[1].index1, lines[1]);
        assertSegmentLine(lines[2], "seg0", segments[0]);
        assertSegmentLine(lines[3], "seg1", segments[1]);
        assertMaterialLine(lines[4], "material0", segments[0]);
        assertMaterialLine(lines[5], "material1", segments[1]);

        b2Chain_SetFriction(chainId, 0.7f);
        b2Chain_SetRestitution(chainId, 0.3f);
        b2Chain_SetMaterial(chainId, 77);
        assertChainMaterialLine(lines[6], chainId, segments);

        final int[] overlapCount = {0};
        List<Integer> overlapHits = new ArrayList<>();
        b2QueryFilter filter = b2DefaultQueryFilter();
        filter.categoryBits = 0x0002L;
        filter.maskBits = 0x0002L;
        b2TreeStats stats = b2World_OverlapAABB(worldId, new b2AABB(new b2Vec2(-1.5f, -0.2f), new b2Vec2(0.5f, 0.7f)),
            filter, shapeId -> {
                overlapCount[0] += 1;
                overlapHits.add(shapeId.index1);
                return true;
            });
        int lineIndex = 7;
        for (int hit : overlapHits) {
            assertEquals("overlapHit " + hit, lines[lineIndex++]);
        }
        assertEquals("overlap " + overlapCount[0] + " stats " + stats.nodeVisits + " " + stats.leafVisits, lines[lineIndex++]);
        b2RayResult ray = b2World_CastRayClosest(worldId, new b2Vec2(-1.5f, 1.0f), new b2Vec2(2.0f, -2.0f), filter);
        assertRayLine(lines[lineIndex++], ray);

        b2Vec2[] loopPoints = {new b2Vec2(-1.0f, -1.0f), new b2Vec2(1.0f, -1.0f), new b2Vec2(1.0f, 1.0f),
            new b2Vec2(-1.0f, 1.0f)};
        chainDef = b2DefaultChainDef();
        chainDef.points = loopPoints;
        chainDef.count = 4;
        chainDef.isLoop = true;
        b2ChainId loopId = b2CreateChain(bodyId, chainDef);
        b2ShapeId[] loopSegments = new b2ShapeId[4];
        assertEquals("loop " + loopId.index1 + " " + b2Chain_GetSegments(loopId, loopSegments, 4), lines[lineIndex++]);
        assertSegmentLine(lines[lineIndex++], "loopLast", loopSegments[3]);

        b2DestroyChain(chainId);
        assertEquals("destroy " + boolInt(b2Chain_IsValid(chainId)) + " " + boolInt(b2Shape_IsValid(segments[0])), lines[lineIndex]);

        b2DestroyWorld(worldId);
    }

    private static void assertChainLine(String line, b2ChainId chainId, boolean valid, int count) {
        assertEquals("chain " + chainId.index1 + " " + chainId.world0 + " " + chainId.generation + " " + boolInt(valid) + " " + count, line);
    }

    private static void assertSegmentLine(String line, String label, b2ShapeId shapeId) {
        String[] parts = line.split("\\s+");
        b2ChainSegment segment = b2Shape_GetChainSegment(shapeId);
        b2ChainId parent = b2Shape_GetParentChain(shapeId);
        assertEquals(label, parts[0]);
        assertEquals(shapeId.index1, Integer.parseInt(parts[1]));
        assertEquals(b2_chainSegmentShape, Integer.parseInt(parts[2]));
        assertEquals(parent.index1, Integer.parseInt(parts[3]));
        assertEquals(Float.parseFloat(parts[4]), segment.ghost1.x, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), segment.ghost1.y, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), segment.segment.point1.x, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), segment.segment.point1.y, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), segment.segment.point2.x, 0.0f);
        assertEquals(Float.parseFloat(parts[9]), segment.segment.point2.y, 0.0f);
        assertEquals(Float.parseFloat(parts[10]), segment.ghost2.x, 0.0f);
        assertEquals(Float.parseFloat(parts[11]), segment.ghost2.y, 0.0f);
    }

    private static void assertMaterialLine(String line, String label, b2ShapeId shapeId) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), b2Shape_GetFriction(shapeId), 0.0f);
        assertEquals(Float.parseFloat(parts[2]), b2Shape_GetRestitution(shapeId), 0.0f);
        assertEquals(Integer.parseInt(parts[3]), b2Shape_GetMaterial(shapeId));
    }

    private static void assertChainMaterialLine(String line, b2ChainId chainId, b2ShapeId[] segments) {
        String[] parts = line.split("\\s+");
        assertEquals("chainMaterial", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), b2Chain_GetFriction(chainId), 0.0f);
        assertEquals(Float.parseFloat(parts[2]), b2Chain_GetRestitution(chainId), 0.0f);
        assertEquals(Integer.parseInt(parts[3]), b2Chain_GetMaterial(chainId));
        assertEquals(Float.parseFloat(parts[4]), b2Shape_GetFriction(segments[0]), 0.0f);
        assertEquals(Float.parseFloat(parts[5]), b2Shape_GetRestitution(segments[1]), 0.0f);
        assertEquals(Integer.parseInt(parts[6]), b2Shape_GetMaterial(segments[1]));
    }

    private static void assertRayLine(String line, b2RayResult ray) {
        String[] parts = line.split("\\s+");
        assertEquals("ray", parts[0]);
        assertEquals(ray.hit, Integer.parseInt(parts[1]) != 0);
        assertEquals(ray.shapeId.index1, Integer.parseInt(parts[2]));
        assertEquals(Float.parseFloat(parts[3]), ray.point.x, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), ray.point.y, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), ray.normal.x, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), ray.normal.y, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), ray.fraction, 0.0f);
    }

    private static int boolInt(boolean value) {
        return value ? 1 : 0;
    }
}
