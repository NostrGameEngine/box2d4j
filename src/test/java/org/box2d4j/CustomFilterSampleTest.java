package org.box2d4j;

import org.box2d4j.samples.CustomFilter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CustomFilterSampleTest {
    @Test
    void sampleMatchesUpstreamCCustomFilter() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("customFilter", parts[0]);

        CustomFilter.Result result = CustomFilter.run();
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.awakeBodyCount);
        assertEquals(Integer.parseInt(parts[5]), result.filterCalls);
        int shapeCount = Integer.parseInt(parts[6]);
        int bodyCount = Integer.parseInt(parts[7]);
        assertEquals(shapeCount, result.shapeStates.length);
        assertEquals(bodyCount, result.bodyStates.length);
        for (int i = 0; i < shapeCount; ++i) {
            assertEquals(Integer.parseInt(parts[8 + i]), result.shapeStates[i].userData);
        }
        int bodyStart = 8 + shapeCount;
        for (int i = 0; i < bodyCount; ++i) {
            assertBody(parts, bodyStart + 6 * i, result.bodyStates[i]);
        }
        assertEquals(upstream, result.toLine());
    }

    private static void assertBody(String[] parts, int index, CustomFilter.BodyState body) {
        assertEquals(Integer.parseInt(parts[index]), body.contactCapacity);
        assertEquals(Float.parseFloat(parts[index + 1]), body.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 2]), body.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 3]), body.angle, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 4]), body.velocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 5]), body.velocityY, 0.0f);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_custom_filter_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_custom_filter_sample_probe.c").toString());
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
