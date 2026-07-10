package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class DynamicTreeParityTest {
    @Test
    void dynamicTreeQueriesMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_tree_probe");

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
        command.add(root.resolve("tools/parity/box2d_tree_probe.c").toString());
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String[] lines = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(0, run.waitFor());

        b2DynamicTree tree = b2DynamicTree_Create();
        int a = b2DynamicTree_CreateProxy(tree, new b2AABB(new b2Vec2(0.0f, 0.0f), new b2Vec2(1.0f, 1.0f)), 0x1L, 101L);
        int b = b2DynamicTree_CreateProxy(tree, new b2AABB(new b2Vec2(2.0f, 0.0f), new b2Vec2(3.0f, 1.0f)), 0x2L, 202L);
        int c = b2DynamicTree_CreateProxy(tree, new b2AABB(new b2Vec2(0.5f, 0.5f), new b2Vec2(1.5f, 1.5f)), 0x1L, 303L);
        int d = b2DynamicTree_CreateProxy(tree, new b2AABB(new b2Vec2(-2.0f, -1.0f), new b2Vec2(-1.0f, 1.0f)), 0x4L, 404L);

        b2DynamicTree_MoveProxy(tree, d, new b2AABB(new b2Vec2(1.2f, -0.5f), new b2Vec2(1.8f, 0.5f)));
        b2DynamicTree_EnlargeProxy(tree, a, new b2AABB(new b2Vec2(-0.2f, -0.2f), new b2Vec2(1.2f, 1.2f)));

        b2AABB rootBounds = b2DynamicTree_GetRootBounds(tree);
        String[] summary = lines[0].split("\\s+");
        assertEquals("summary", summary[0]);
        assertEquals(Integer.parseInt(summary[1]), b2DynamicTree_GetProxyCount(tree));
        assertEquals(Long.parseUnsignedLong(summary[2]), b2DynamicTree_GetCategoryBits(tree, b));
        assertEquals(Long.parseUnsignedLong(summary[3]), b2DynamicTree_GetUserData(tree, c));
        assertEquals(Float.parseFloat(summary[4]), rootBounds.lowerBound.x, 0.0f);
        assertEquals(Float.parseFloat(summary[5]), rootBounds.lowerBound.y, 0.0f);
        assertEquals(Float.parseFloat(summary[6]), rootBounds.upperBound.x, 0.0f);
        assertEquals(Float.parseFloat(summary[7]), rootBounds.upperBound.y, 0.0f);

        List<Long> queryHits = new ArrayList<>();
        b2TreeStats queryStats = b2DynamicTree_Query(tree,
            new b2AABB(new b2Vec2(0.75f, -0.25f), new b2Vec2(2.5f, 1.25f)), 0x7L,
            (proxyId, userData) -> {
                queryHits.add(userData);
                return true;
            });
        assertEquals(lines[1], formatHits("query", queryHits, queryStats));

        List<Long> rayHits = new ArrayList<>();
        b2RayCastInput rayInput = new b2RayCastInput();
        rayInput.origin = new b2Vec2(-3.0f, 0.25f);
        rayInput.translation = new b2Vec2(6.0f, 0.0f);
        rayInput.maxFraction = 1.0f;
        b2TreeStats rayStats = b2DynamicTree_RayCast(tree, rayInput, B2_DEFAULT_MASK_BITS, (input, proxyId, userData) -> {
            rayHits.add(userData);
            return input.maxFraction;
        });
        assertEquals(lines[2], formatHits("ray", rayHits, rayStats));

        List<Long> shapeHits = new ArrayList<>();
        b2Vec2[] points = {new b2Vec2(-2.5f, -0.25f), new b2Vec2(-2.5f, 0.25f)};
        b2ShapeCastInput shapeInput = new b2ShapeCastInput();
        shapeInput.proxy = b2MakeProxy(points, points.length, 0.1f);
        shapeInput.translation = new b2Vec2(5.0f, 0.0f);
        shapeInput.maxFraction = 1.0f;
        b2TreeStats shapeStats = b2DynamicTree_ShapeCast(tree, shapeInput, B2_DEFAULT_MASK_BITS, (input, proxyId, userData) -> {
            shapeHits.add(userData);
            return input.maxFraction;
        });
        assertEquals(lines[3], formatHits("shape", shapeHits, shapeStats));

        int partial = b2DynamicTree_Rebuild(tree, false);
        assertTreeSummary(lines[4], "partial", partial, tree);

        int full = b2DynamicTree_Rebuild(tree, true);
        assertTreeSummary(lines[5], "full", full, tree);

        List<Long> postHits = new ArrayList<>();
        b2TreeStats postStats = b2DynamicTree_Query(tree,
            new b2AABB(new b2Vec2(0.75f, -0.25f), new b2Vec2(2.5f, 1.25f)), 0x7L,
            (proxyId, userData) -> {
                postHits.add(userData);
                return true;
            });
        assertEquals(lines[6], formatHits("post", postHits, postStats));

        b2DynamicTree_Validate(tree);
        b2DynamicTree_Destroy(tree);

        tree = b2DynamicTree_Create();
        int[] proxyIds = new int[820];
        int index = 0;
        for (int i = 0; i < 40; ++i) {
            float y = i + 1.5f;
            for (int j = i; j < 40; ++j) {
                float x = 0.5f * i + (j - i) - 20.0f;
                proxyIds[index] = b2DynamicTree_CreateProxy(tree,
                    new b2AABB(new b2Vec2(x - 0.6f, y - 0.6f), new b2Vec2(x + 0.6f, y + 0.6f)),
                    B2_DEFAULT_CATEGORY_BITS, index + 1L);
                index += 1;
            }
        }
        for (int proxyId : proxyIds) {
            b2DynamicTree_DestroyProxy(tree, proxyId);
        }
        index = 0;
        int firstProxy = -1;
        int lastProxy = -1;
        for (int i = 0; i < 40; ++i) {
            float y = i + 1.5f;
            for (int j = i; j < 40; ++j) {
                float x = 0.5f * i + (j - i) - 20.0f;
                int proxyId = b2DynamicTree_CreateProxy(tree,
                    new b2AABB(new b2Vec2(x - 0.6f, y - 0.6f), new b2Vec2(x + 0.6f, y + 0.6f)),
                    B2_DEFAULT_CATEGORY_BITS, 820L - index);
                if (index == 0) {
                    firstProxy = proxyId;
                }
                lastProxy = proxyId;
                index += 1;
            }
        }
        long[] reuseHash = {0xcbf29ce484222325L};
        int[] reuseCount = {0};
        b2TreeStats reuseStats = b2DynamicTree_Query(tree,
            new b2AABB(new b2Vec2(-100.0f, -100.0f), new b2Vec2(100.0f, 100.0f)), B2_DEFAULT_MASK_BITS,
            (proxyId, userData) -> {
                reuseHash[0] = (reuseHash[0] ^ Integer.toUnsignedLong(proxyId)) * 0x100000001b3L;
                reuseHash[0] = (reuseHash[0] ^ Integer.toUnsignedLong((int) userData)) * 0x100000001b3L;
                reuseCount[0] += 1;
                return true;
            });
        String reuseLine = "reuse " + firstProxy + " " + lastProxy + " " + tree.root + " " + tree.freeList
            + " " + tree.nodeCount + " " + reuseStats.nodeVisits + " " + reuseCount[0] + " "
            + Long.toUnsignedString(reuseHash[0]);
        assertEquals(lines[7], reuseLine);
        b2DynamicTree_Destroy(tree);
    }

    private static void assertTreeSummary(String line, String label, int rebuildCount, b2DynamicTree tree) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]), rebuildCount);
        assertEquals(Integer.parseInt(parts[2]), b2DynamicTree_GetHeight(tree));
        assertEquals(Float.parseFloat(parts[3]), b2DynamicTree_GetAreaRatio(tree), 0.0f);
        b2AABB bounds = b2DynamicTree_GetRootBounds(tree);
        assertEquals(Float.parseFloat(parts[4]), bounds.lowerBound.x, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), bounds.lowerBound.y, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), bounds.upperBound.x, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), bounds.upperBound.y, 0.0f);
    }

    private static String formatHits(String label, List<Long> hits, b2TreeStats stats) {
        Collections.sort(hits);
        StringBuilder builder = new StringBuilder(label)
            .append(' ').append(hits.size())
            .append(' ').append(stats.nodeVisits)
            .append(' ').append(stats.leafVisits);
        for (long hit : hits) {
            builder.append(' ').append(hit);
        }
        return builder.toString();
    }
}
