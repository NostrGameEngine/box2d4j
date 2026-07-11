package org.box2d4j;

import org.box2d4j.samples.DynamicTreeSample;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DynamicTreeSampleTest {
    @Test
    void sampleMatchesUpstreamCDynamicTree() throws Exception {
        String[] parts = runProbe().split("\\s+");
        assertEquals("dynamicTree", parts[0]);
        DynamicTreeSample.Result result = DynamicTreeSample.run();
        int index = 1;
        int variantCount = Integer.parseInt(parts[index++]);
        assertEquals(variantCount, result.variants.length);
        for (DynamicTreeSample.VariantResult variant : result.variants) {
            assertEquals(Integer.parseInt(parts[index++]), variant.updateType);
            assertEquals(Integer.parseInt(parts[index++]), variant.proxyCount);
            int stepCount = Integer.parseInt(parts[index++]);
            assertEquals(stepCount, variant.movedCounts.length);
            assertEquals(stepCount, variant.rebuildCounts.length);
            for (int i = 0; i < stepCount; ++i) {
                assertEquals(Integer.parseInt(parts[index++]), variant.movedCounts[i]);
                assertEquals(Integer.parseInt(parts[index++]), variant.rebuildCounts[i]);
            }
            assertEquals(Integer.parseInt(parts[index++]), variant.height);
            assertEquals(Float.parseFloat(parts[index++]), variant.areaRatio, 0.0f);
            assertEquals(Integer.parseInt(parts[index++]), variant.byteCount);
            assertEquals(Float.parseFloat(parts[index++]), variant.lowerX, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), variant.lowerY, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), variant.upperX, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), variant.upperY, 0.0f);
            assertEquals(Long.parseUnsignedLong(parts[index++]), variant.stateHash);
            assertEquals(Integer.parseInt(parts[index++]), variant.queryNodeVisits);
            assertEquals(Integer.parseInt(parts[index++]), variant.queryLeafVisits);
            int queryHitCount = Integer.parseInt(parts[index++]);
            assertEquals(queryHitCount, variant.queryHits.length);
            for (int hit : variant.queryHits) {
                assertEquals(Integer.parseInt(parts[index++]), hit);
            }
            assertEquals(Integer.parseInt(parts[index++]), variant.rayNodeVisits);
            assertEquals(Integer.parseInt(parts[index++]), variant.rayLeafVisits);
            int rayHitCount = Integer.parseInt(parts[index++]);
            assertEquals(rayHitCount, variant.rayHits.length);
            for (int hit : variant.rayHits) {
                assertEquals(Integer.parseInt(parts[index++]), hit);
            }
            int representativeCount = Integer.parseInt(parts[index++]);
            assertEquals(representativeCount, variant.representatives.length);
            for (DynamicTreeSample.ProxyState proxy : variant.representatives) {
                assertEquals(Integer.parseInt(parts[index++]), proxy.proxyId);
                assertEquals(Float.parseFloat(parts[index++]), proxy.positionX, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), proxy.positionY, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), proxy.widthX, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), proxy.widthY, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), proxy.boxLowerX, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), proxy.boxLowerY, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), proxy.boxUpperX, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), proxy.boxUpperY, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), proxy.fatLowerX, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), proxy.fatLowerY, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), proxy.fatUpperX, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), proxy.fatUpperY, 0.0f);
                assertEquals(Integer.parseInt(parts[index++]) != 0, proxy.moved);
            }
        }
        assertEquals(parts.length, index);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_dynamic_tree_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_dynamic_tree_sample_probe.c").toString());
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
