package org.box2d4j;

import org.box2d4j.samples.OverlapWorld;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class OverlapWorldSampleTest {
    @Test
    void sampleMatchesUpstreamCOverlapWorld() throws Exception {
        String[] parts = runProbe().split("\\s+");
        assertEquals("overlapWorld", parts[0]);
        OverlapWorld.Result result = OverlapWorld.run();
        int index = 1;
        int variantCount = Integer.parseInt(parts[index++]);
        assertEquals(variantCount, result.variants.length);
        for (OverlapWorld.VariantResult variant : result.variants) {
            assertEquals(Integer.parseInt(parts[index++]), variant.shapeType);
            assertEquals(Float.parseFloat(parts[index++]), variant.queryX, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), variant.queryY, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), variant.queryAngle, 0.0f);
            assertEquals(Integer.parseInt(parts[index++]), variant.beforeBodyCount);
            assertEquals(Integer.parseInt(parts[index++]), variant.beforeShapeCount);
            assertEquals(Integer.parseInt(parts[index++]), variant.afterBodyCount);
            assertEquals(Integer.parseInt(parts[index++]), variant.afterShapeCount);
            assertEquals(Integer.parseInt(parts[index++]), variant.nodeVisits);
            assertEquals(Integer.parseInt(parts[index++]), variant.leafVisits);
            int doomedCount = Integer.parseInt(parts[index++]);
            assertEquals(doomedCount, variant.doomedIndices.length);
            for (int doomed : variant.doomedIndices) {
                assertEquals(Integer.parseInt(parts[index++]), doomed);
            }
            int bodyCount = Integer.parseInt(parts[index++]);
            assertEquals(bodyCount, variant.bodies.length);
            assertEquals(bodyCount, variant.valid.length);
            for (int i = 0; i < bodyCount; ++i) {
                OverlapWorld.BodyState body = variant.bodies[i];
                assertEquals(Float.parseFloat(parts[index++]), body.x, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), body.y, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), body.cos, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), body.sin, 0.0f);
                assertEquals(Integer.parseInt(parts[index++]) != 0, variant.valid[i]);
            }
        }
        assertEquals(parts.length, index);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_overlap_world_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_overlap_world_sample_probe.c").toString());
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
