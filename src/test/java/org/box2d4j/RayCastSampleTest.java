package org.box2d4j;

import org.box2d4j.samples.RayCast;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class RayCastSampleTest {
    @Test
    void sampleMatchesUpstreamCRayCast() throws Exception {
        String[] parts = runProbe().split("\\s+");
        assertEquals("rayCast", parts[0]);
        int outputCount = Integer.parseInt(parts[1]);

        RayCast.Result result = RayCast.run();
        assertEquals(outputCount, result.localOutputs.length);
        int index = 2;
        for (RayCast.OutputState output : result.localOutputs) {
            index = assertOutput(parts, index, output);
        }
        index = assertOutput(parts, index, result.closestOutput);
        assertEquals(Float.parseFloat(parts[index++]), result.maxFraction, 0.0f);
        assertEquals(parts.length, index);
    }

    private static int assertOutput(String[] parts, int index, RayCast.OutputState output) {
        assertEquals(Integer.parseInt(parts[index++]) != 0, output.hit);
        assertEquals(Float.parseFloat(parts[index++]), output.fraction, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), output.pointX, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), output.pointY, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), output.normalX, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), output.normalY, 0.0f);
        assertEquals(Integer.parseInt(parts[index++]), output.iterations);
        return index;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_ray_cast_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_ray_cast_sample_probe.c").toString());
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
