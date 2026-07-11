package org.box2d4j;

import org.box2d4j.samples.BenchmarkBarrel;
import org.box2d4j.samples.BenchmarkCast;
import org.box2d4j.samples.BenchmarkCompound;
import org.box2d4j.samples.BenchmarkCreateDestroy;
import org.box2d4j.samples.BenchmarkJointGrid;
import org.box2d4j.samples.BenchmarkKinematic;
import org.box2d4j.samples.BenchmarkLargePyramid;
import org.box2d4j.samples.BenchmarkManyPyramids;
import org.box2d4j.samples.BenchmarkManyTumblers;
import org.box2d4j.samples.BenchmarkRain;
import org.box2d4j.samples.BenchmarkSampleResult;
import org.box2d4j.samples.BenchmarkSmash;
import org.box2d4j.samples.BenchmarkSleep;
import org.box2d4j.samples.BenchmarkSensor;
import org.box2d4j.samples.BenchmarkShapeDistance;
import org.box2d4j.samples.BenchmarkSpinner;
import org.box2d4j.samples.BenchmarkTumbler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class BenchmarkCoreSampleTest {
    @Test
    void samplesMatchUpstreamCExactly() throws Exception {
        Map<String, BenchmarkSampleResult> results = new LinkedHashMap<>();
        results.put("benchmarkBarrel", BenchmarkBarrel.run());
        results.put("benchmarkTumbler", BenchmarkTumbler.run());
        results.put("benchmarkLargePyramid", BenchmarkLargePyramid.run());
        results.put("benchmarkManyPyramids", BenchmarkManyPyramids.run());
        results.put("benchmarkJointGrid", BenchmarkJointGrid.run());
        results.put("benchmarkSmash", BenchmarkSmash.run());
        results.put("benchmarkSpinner", BenchmarkSpinner.run());
        results.put("benchmarkManyTumblers", BenchmarkManyTumblers.run());
        results.put("benchmarkCreateDestroy", BenchmarkCreateDestroy.run());
        results.put("benchmarkSleep", BenchmarkSleep.run());
        results.put("benchmarkCompound", BenchmarkCompound.run());
        results.put("benchmarkKinematic", BenchmarkKinematic.run());
        results.put("benchmarkCast", BenchmarkCast.run());
        results.put("benchmarkRain", BenchmarkRain.run());
        results.put("benchmarkShapeDistance", BenchmarkShapeDistance.run());
        results.put("benchmarkSensor", BenchmarkSensor.run());

        String[] lines = runProbe().split("\\R");
        assertEquals(results.size(), lines.length);
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            BenchmarkSampleResult result = results.remove(parts[0]);
            assertEquals(parts[0], result.name);
            int index = assertCheckpoint(parts, 1, result.initial);
            index = assertCheckpoint(parts, index, result.result);
            assertEquals(Long.parseUnsignedLong(parts[index++]), result.workloadHash);
            assertEquals(parts.length, index);
        }
        assertEquals(0, results.size());
    }

    private static int assertCheckpoint(String[] parts, int index, BenchmarkSampleResult.Checkpoint checkpoint) {
        assertEquals(Integer.parseInt(parts[index++]), checkpoint.step);
        assertEquals(Integer.parseInt(parts[index++]), checkpoint.bodyCount);
        assertEquals(Integer.parseInt(parts[index++]), checkpoint.shapeCount);
        assertEquals(Integer.parseInt(parts[index++]), checkpoint.contactCount);
        assertEquals(Integer.parseInt(parts[index++]), checkpoint.jointCount);
        assertEquals(Integer.parseInt(parts[index++]), checkpoint.islandCount);
        assertEquals(Integer.parseInt(parts[index++]), checkpoint.awakeBodyCount);
        assertEquals(Integer.parseInt(parts[index++]), checkpoint.validBodyCount);
        assertEquals(Integer.parseInt(parts[index++]), checkpoint.colorCounts.length);
        for (int colorCount : checkpoint.colorCounts) {
            assertEquals(Integer.parseInt(parts[index++]), colorCount);
        }
        assertEquals(Long.parseUnsignedLong(parts[index++]), checkpoint.bodyHash);
        return index;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_benchmark_core_probe");

        List<String> sources = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted()
                .forEach(path -> sources.add(path.toString()));
        }
        sources.add(root.resolve("vendor/box2d/shared/benchmarks.c").toString());
        sources.add(root.resolve("vendor/box2d/shared/human.c").toString());
        sources.add(root.resolve("vendor/box2d/shared/random.c").toString());

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        command.add("-I" + root.resolve("vendor/box2d/shared"));
        command.addAll(sources);
        command.add(root.resolve("tools/parity/box2d_benchmark_core_probe.c").toString());
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
