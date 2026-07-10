package org.box2d4j;

import org.box2d4j.samples.ShapeDistance;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ShapeDistanceSampleTest {
    @Test
    void sampleMatchesUpstreamCShapeDistance() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("shapeDistance", parts[0]);

        ShapeDistance.Result result = ShapeDistance.run();
        int caseCount = Integer.parseInt(parts[1]);
        assertEquals(caseCount, result.cases.length);
        int index = 2;
        for (int i = 0; i < caseCount; ++i) {
            index = assertCase(parts, index, result.cases[i]);
        }
        assertEquals(upstream, result.toLine());
    }

    private static int assertCase(String[] parts, int index, ShapeDistance.CaseResult result) {
        assertEquals(parts[index++], result.label);
        assertEquals(Integer.parseInt(parts[index++]), result.typeA.ordinal());
        assertEquals(Integer.parseInt(parts[index++]), result.typeB.ordinal());
        assertEquals(Float.parseFloat(parts[index++]), result.radiusA, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), result.radiusB, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), result.position.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), result.position.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), result.angle, 0.0f);
        assertEquals(Integer.parseInt(parts[index++]), result.proxyACount);
        assertEquals(Integer.parseInt(parts[index++]), result.proxyBCount);
        assertEquals(Integer.parseInt(parts[index++]), result.cache.count);
        for (int i = 0; i < 3; ++i) {
            assertEquals(Integer.parseInt(parts[index++]), Byte.toUnsignedInt(result.cache.indexA[i]));
        }
        for (int i = 0; i < 3; ++i) {
            assertEquals(Integer.parseInt(parts[index++]), Byte.toUnsignedInt(result.cache.indexB[i]));
        }
        assertEquals(Float.parseFloat(parts[index++]), result.output.distance, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), result.output.normal.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), result.output.normal.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), result.output.pointA.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), result.output.pointA.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), result.output.pointB.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index++]), result.output.pointB.y, 0.0f);
        assertEquals(Integer.parseInt(parts[index++]), result.output.iterations);
        assertEquals(Integer.parseInt(parts[index++]), result.output.simplexCount);
        return index;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_shape_distance_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_shape_distance_sample_probe.c").toString());
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
