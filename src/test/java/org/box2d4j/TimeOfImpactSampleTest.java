package org.box2d4j;

import org.box2d4j.samples.TimeOfImpact;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TimeOfImpactSampleTest {
    @Test
    void sampleMatchesUpstreamCTimeOfImpact() throws Exception {
        String[] parts = runProbe().split("\\s+");
        assertEquals("timeOfImpact", parts[0]);
        TimeOfImpact.Result result = TimeOfImpact.run();
        int index = 1;
        assertEquals(Integer.parseInt(parts[index++]), result.state);
        assertEquals(Float.parseFloat(parts[index++]), result.fraction, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), result.distance, 0.0f);
        assertEquals(Integer.parseInt(parts[index++]), result.distanceIterations);
        assertEquals(Integer.parseInt(parts[index++]), result.simplexCount);
        index = assertSegment(parts, index, result.start);
        index = assertSegment(parts, index, result.hit);
        index = assertSegment(parts, index, result.end);
        assertEquals(parts.length, index);
    }

    private static int assertSegment(String[] parts, int index, TimeOfImpact.SegmentState segment) {
        assertEquals(Float.parseFloat(parts[index++]), segment.x1, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), segment.y1, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), segment.x2, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), segment.y2, 0.0f);
        return index;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_time_of_impact_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_time_of_impact_sample_probe.c").toString());
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
