package org.box2d4j;

import org.box2d4j.samples.ConvexHull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ConvexHullSampleTest {
    @Test
    void sampleMatchesUpstreamCConvexHull() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("convexHull", parts[0]);

        ConvexHull.Result result = ConvexHull.run();
        assertEquals(Integer.parseInt(parts[1]), result.generation);
        assertEquals(Float.parseFloat(parts[2]), result.angle, 0.0f);
        assertEquals(Integer.parseInt(parts[3]) != 0, result.valid);
        assertEquals(Integer.parseInt(parts[4]), result.hullCount);
        int index = 5;
        for (int i = 0; i < B2.B2_MAX_POLYGON_VERTICES; ++i) {
            assertEquals(Float.parseFloat(parts[index++]), result.points[i].x, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), result.points[i].y, 0.0f);
        }
        for (int i = 0; i < result.hullCount; ++i) {
            assertEquals(Float.parseFloat(parts[index++]), result.hullPoints[i].x, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), result.hullPoints[i].y, 0.0f);
        }
        assertEquals(upstream, result.toLine());
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_convex_hull_sample_probe");

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
        command.add("-I" + root.resolve("vendor/box2d/shared"));
        command.addAll(sources);
        command.add(root.resolve("vendor/box2d/shared/random.c").toString());
        command.add(root.resolve("tools/parity/box2d_convex_hull_sample_probe.c").toString());
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
