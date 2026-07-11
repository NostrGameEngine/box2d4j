package org.box2d4j;

import org.box2d4j.samples.SmoothManifold;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SmoothManifoldSampleTest {
    @Test
    void sampleMatchesUpstreamCSmoothManifold() throws Exception {
        String[] parts = runProbe().split("\\s+");
        assertEquals("smoothManifold", parts[0]);
        SmoothManifold.Result result = SmoothManifold.run();
        int index = 1;
        int caseCount = Integer.parseInt(parts[index++]);
        assertEquals(caseCount, result.cases.length);
        for (SmoothManifold.CaseResult value : result.cases) {
            assertEquals(Integer.parseInt(parts[index++]), value.shapeType);
            assertEquals(Float.parseFloat(parts[index++]), value.x, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), value.y, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), value.angle, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), value.round, 0.0f);
            int manifoldCount = Integer.parseInt(parts[index++]);
            assertEquals(manifoldCount, value.manifolds.length);
            for (SmoothManifold.ManifoldState manifold : value.manifolds) {
                int pointCount = Integer.parseInt(parts[index++]);
                assertEquals(pointCount, manifold.points.length);
                assertEquals(Float.parseFloat(parts[index++]), manifold.normalX, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), manifold.normalY, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), manifold.rollingImpulse, 0.0f);
                for (SmoothManifold.PointState point : manifold.points) {
                    assertEquals(Float.parseFloat(parts[index++]), point.pointX, 0.0f);
                    assertEquals(Float.parseFloat(parts[index++]), point.pointY, 0.0f);
                    assertEquals(Float.parseFloat(parts[index++]), point.anchorAX, 0.0f);
                    assertEquals(Float.parseFloat(parts[index++]), point.anchorAY, 0.0f);
                    assertEquals(Float.parseFloat(parts[index++]), point.anchorBX, 0.0f);
                    assertEquals(Float.parseFloat(parts[index++]), point.anchorBY, 0.0f);
                    assertEquals(Float.parseFloat(parts[index++]), point.separation, 0.0f);
                    assertEquals(Integer.parseInt(parts[index++]), point.id);
                }
            }
        }
        assertEquals(parts.length, index);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_smooth_manifold_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_smooth_manifold_sample_probe.c").toString());
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
