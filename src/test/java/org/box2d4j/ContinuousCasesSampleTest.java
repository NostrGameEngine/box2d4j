package org.box2d4j;

import org.box2d4j.samples.BounceHouse;
import org.box2d4j.samples.BounceHumans;
import org.box2d4j.samples.ContinuousSampleResult;
import org.box2d4j.samples.ChainDrop;
import org.box2d4j.samples.ChainSlide;
import org.box2d4j.samples.Drop;
import org.box2d4j.samples.GhostBumps;
import org.box2d4j.samples.PixelImperfect;
import org.box2d4j.samples.Pinball;
import org.box2d4j.samples.RestitutionThreshold;
import org.box2d4j.samples.SegmentSlide;
import org.box2d4j.samples.SkinnyBox;
import org.box2d4j.samples.SpeculativeFallback;
import org.box2d4j.samples.SpeculativeGhost;
import org.box2d4j.samples.SpeculativeSliver;
import org.box2d4j.samples.Wedge;
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

final class ContinuousCasesSampleTest {
    @Test
    void samplesMatchUpstreamCAtEveryCheckpoint() throws Exception {
        Map<String, ContinuousSampleResult> results = new LinkedHashMap<>();
        results.put("bounceHouse", BounceHouse.run());
        results.put("bounceHumans", BounceHumans.run());
        results.put("ghostBumps", GhostBumps.run());
        results.put("drop", Drop.run());
        results.put("chainDrop", ChainDrop.run());
        results.put("chainSlide", ChainSlide.run());
        results.put("segmentSlide", SegmentSlide.run());
        results.put("skinnyBox", SkinnyBox.run());
        results.put("speculativeFallback", SpeculativeFallback.run());
        results.put("speculativeSliver", SpeculativeSliver.run());
        results.put("speculativeGhost", SpeculativeGhost.run());
        results.put("pixelImperfect", PixelImperfect.run());
        results.put("restitutionThreshold", RestitutionThreshold.run());
        results.put("wedge", Wedge.run());
        results.put("pinball", Pinball.run());

        String[] lines = runProbe().split("\\R");
        assertEquals(results.size(), lines.length);
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            ContinuousSampleResult result = results.remove(parts[0]);
            assertResult(parts, result);
        }
        assertEquals(0, results.size());
    }

    private static void assertResult(String[] parts, ContinuousSampleResult result) {
        assertEquals(parts[0], result.name);
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.islandCount);
        assertEquals(Integer.parseInt(parts[5]), result.awakeBodyCount);
        assertEquals(Integer.parseInt(parts[6]), result.states.length);

        int index = 7;
        for (ContinuousSampleResult.BodyState state : result.states) {
            assertEquals(Integer.parseInt(parts[index++]), state.step);
            assertEquals(Float.parseFloat(parts[index++]), state.x, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), state.y, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), state.rotationCos, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), state.rotationSin, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), state.velocityX, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), state.velocityY, 0.0f);
            assertEquals(Float.parseFloat(parts[index++]), state.angularVelocity, 0.0f);
            assertEquals(Integer.parseInt(parts[index++]), state.contactCount);
            assertEquals(Integer.parseInt(parts[index++]) != 0, state.awake);
        }
        assertEquals(parts.length, index);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_continuous_cases_probe");

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
        command.add("-I" + root.resolve("vendor/box2d/shared"));
        command.addAll(sources);
        command.add(root.resolve("vendor/box2d/shared/random.c").toString());
        command.add(root.resolve("vendor/box2d/shared/human.c").toString());
        command.add(root.resolve("tools/parity/box2d_continuous_cases_probe.c").toString());
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
