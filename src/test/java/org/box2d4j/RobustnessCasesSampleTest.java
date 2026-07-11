package org.box2d4j;

import org.box2d4j.samples.Cart;
import org.box2d4j.samples.HighMassRatio1;
import org.box2d4j.samples.HighMassRatio2;
import org.box2d4j.samples.HighMassRatio3;
import org.box2d4j.samples.OverlapRecovery;
import org.box2d4j.samples.RobustnessSampleResult;
import org.box2d4j.samples.TinyPyramid;
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

final class RobustnessCasesSampleTest {
    @Test
    void samplesMatchUpstreamCAtEveryCheckpoint() throws Exception {
        Map<String, RobustnessSampleResult> results = new LinkedHashMap<>();
        results.put("highMassRatio1", HighMassRatio1.run());
        results.put("highMassRatio2", HighMassRatio2.run());
        results.put("highMassRatio3", HighMassRatio3.run());
        results.put("overlapRecovery", OverlapRecovery.run());
        results.put("tinyPyramid", TinyPyramid.run());
        results.put("cart", Cart.run());

        String[] lines = runProbe().split("\\R");
        assertEquals(results.size(), lines.length);
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            RobustnessSampleResult result = results.remove(parts[0]);
            assertResult(parts, result);
        }
        assertEquals(0, results.size());
    }

    private static void assertResult(String[] parts, RobustnessSampleResult result) {
        assertEquals(parts[0], result.name);
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.jointCount);
        assertEquals(Integer.parseInt(parts[5]), result.islandCount);
        assertEquals(Integer.parseInt(parts[6]), result.awakeBodyCount);
        assertEquals(Integer.parseInt(parts[7]), result.dynamicBodyCount);
        assertEquals(Integer.parseInt(parts[8]), result.checkpoints.length);

        int index = 9;
        for (RobustnessSampleResult.Checkpoint checkpoint : result.checkpoints) {
            assertEquals(Integer.parseInt(parts[index++]), checkpoint.step);
            assertEquals(Integer.parseInt(parts[index++]), checkpoint.contactCount);
            assertEquals(Integer.parseInt(parts[index++]), checkpoint.islandCount);
            assertEquals(Integer.parseInt(parts[index++]), checkpoint.awakeBodyCount);
            assertEquals(Integer.parseInt(parts[index++]), checkpoint.colorCounts.length);
            for (int colorCount : checkpoint.colorCounts) {
                assertEquals(Integer.parseInt(parts[index++]), colorCount);
            }
            assertEquals(Long.parseUnsignedLong(parts[index++]), checkpoint.stateHash);
            assertEquals(Integer.parseInt(parts[index++]), checkpoint.representatives.length);
            for (RobustnessSampleResult.BodyState body : checkpoint.representatives) {
                assertEquals(Integer.parseInt(parts[index++]), body.index);
                assertEquals(Float.parseFloat(parts[index++]), body.x, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), body.y, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), body.rotationCos, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), body.rotationSin, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), body.velocityX, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), body.velocityY, 0.0f);
                assertEquals(Float.parseFloat(parts[index++]), body.angularVelocity, 0.0f);
                assertEquals(Integer.parseInt(parts[index++]), body.contactCount);
                assertEquals(Integer.parseInt(parts[index++]) != 0, body.awake);
            }
        }
        assertEquals(parts.length, index);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_robustness_cases_probe");

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
        command.add(root.resolve("tools/parity/box2d_robustness_cases_probe.c").toString());
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
