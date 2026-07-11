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

final class BroadPhaseParityTest {
    @Test
    void broadPhaseProxyOperationsMatchUpstreamC() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_broad_phase_probe");

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
        command.add(root.resolve("tools/parity/box2d_broad_phase_probe.c").toString());
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString()).directory(root.toFile()).redirectErrorStream(true).start();
        String[] lines = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(0, run.waitFor());

        b2BroadPhase bp = new b2BroadPhase();
        b2CreateBroadPhase(bp);

        int staticKey = b2BroadPhase_CreateProxy(bp, b2_staticBody, new b2AABB(new b2Vec2(0.0f, 0.0f), new b2Vec2(1.0f, 1.0f)), 0x1L, 11, false);
        int dynamicKey = b2BroadPhase_CreateProxy(bp, b2_dynamicBody, new b2AABB(new b2Vec2(0.5f, 0.5f), new b2Vec2(1.5f, 1.5f)), 0x2L, 22, false);
        int kinematicKey = b2BroadPhase_CreateProxy(bp, b2_kinematicBody, new b2AABB(new b2Vec2(2.0f, 0.0f), new b2Vec2(3.0f, 1.0f)), 0x4L, 33, false);
        int forcedStaticKey = b2BroadPhase_CreateProxy(bp, b2_staticBody, new b2AABB(new b2Vec2(4.0f, 0.0f), new b2Vec2(5.0f, 1.0f)), 0x8L, 44, true);

        assertEquals(lines[0], "keys " + staticKey + " " + dynamicKey + " " + kinematicKey + " " + forcedStaticKey);
        assertEquals(lines[1], formatMoveArray(bp));

        b2BroadPhase_MoveProxy(bp, dynamicKey, new b2AABB(new b2Vec2(0.25f, 0.25f), new b2Vec2(1.25f, 1.25f)));
        b2BroadPhase_EnlargeProxy(bp, kinematicKey, new b2AABB(new b2Vec2(1.75f, -0.25f), new b2Vec2(3.25f, 1.25f)));
        assertEquals(lines[2], formatMoveArray(bp));

        String state = "state " + b2BroadPhase_GetShapeIndex(bp, dynamicKey)
            + " " + (b2BroadPhase_TestOverlap(bp, staticKey, dynamicKey) ? 1 : 0)
            + " " + (b2BroadPhase_TestOverlap(bp, dynamicKey, kinematicKey) ? 1 : 0)
            + " " + b2DynamicTree_GetProxyCount(bp.trees[b2_staticBody])
            + " " + b2DynamicTree_GetProxyCount(bp.trees[b2_dynamicBody])
            + " " + b2DynamicTree_GetProxyCount(bp.trees[b2_kinematicBody]);
        assertEquals(lines[3], state);

        b2BroadPhase_RebuildTrees(bp);
        assertRebuilt(lines[4], bp);

        b2BroadPhase_DestroyProxy(bp, forcedStaticKey);
        assertEquals(lines[5], formatMoveArray(bp));
        String afterDestroy = "afterDestroy " + b2DynamicTree_GetProxyCount(bp.trees[b2_staticBody])
            + " " + (b2BroadPhase_TestOverlap(bp, staticKey, dynamicKey) ? 1 : 0);
        assertEquals(lines[6], afterDestroy);

        b2DestroyBroadPhase(bp);
    }

    private static String formatMoveArray(b2BroadPhase bp) {
        StringBuilder builder = new StringBuilder("moves ")
            .append(bp.moveArray.size()).append(' ')
            .append(bp.moveSet.count);
        for (int key : bp.moveArray) {
            builder.append(' ').append(key);
        }
        return builder.toString();
    }

    private static void assertRebuilt(String line, b2BroadPhase bp) {
        String[] parts = line.split("\\s+");
        assertEquals("rebuilt", parts[0]);
        assertEquals(Integer.parseInt(parts[1]), b2DynamicTree_GetHeight(bp.trees[b2_staticBody]));
        assertEquals(Integer.parseInt(parts[2]), b2DynamicTree_GetHeight(bp.trees[b2_dynamicBody]));
        assertEquals(Integer.parseInt(parts[3]), b2DynamicTree_GetHeight(bp.trees[b2_kinematicBody]));
        assertEquals(Float.parseFloat(parts[4]), b2DynamicTree_GetAreaRatio(bp.trees[b2_dynamicBody]), 0.0f);
        assertEquals(Float.parseFloat(parts[5]), b2DynamicTree_GetAreaRatio(bp.trees[b2_kinematicBody]), 0.0f);
    }
}
