package org.box2d4j;

import org.box2d4j.samples.ShapeCast;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ShapeCastSampleTest {
    @Test
    void sampleMatchesUpstreamCShapeCast() throws Exception {
        String[] parts = runProbe().split("\\s+");
        assertEquals("shapeCast", parts[0]);
        ShapeCast.Result result = ShapeCast.run();
        int i = 1;
        assertEquals(Integer.parseInt(parts[i++]) != 0, result.hit);
        assertEquals(Float.parseFloat(parts[i++]), result.fraction, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.pointX, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.pointY, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.normalX, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.normalY, 0.0f);
        assertEquals(Integer.parseInt(parts[i++]), result.iterations);
        assertEquals(Float.parseFloat(parts[i++]), result.transformX, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.transformY, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.transformCos, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.transformSin, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.distance, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.pointAX, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.pointAY, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.pointBX, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.pointBY, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.distanceNormalX, 0.0f);
        assertEquals(Float.parseFloat(parts[i++]), result.distanceNormalY, 0.0f);
        assertEquals(Integer.parseInt(parts[i++]), result.distanceIterations);
        assertEquals(Integer.parseInt(parts[i++]), result.simplexCount);
        assertEquals(Integer.parseInt(parts[i++]), result.cacheCount);
        assertEquals(parts.length, i);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_shape_cast_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_shape_cast_sample_probe.c").toString());
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
