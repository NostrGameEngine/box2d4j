package org.box2d4j;

import org.box2d4j.samples.Weeble;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WeebleSampleTest {
    private static final int STEP_COUNT = 2400;

    @Test
    void sampleMatchesUpstreamCWeeble() throws Exception {
        String upstream = runProbe(STEP_COUNT);
        String[] parts = upstream.split("\\s+");
        assertEquals("weeble", parts[0]);

        Weeble.Result result = Weeble.run(STEP_COUNT);
        assertEquals(Float.parseFloat(parts[1]), result.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), result.y, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), result.angle, 0.0f);
        assertEquals(Float.parseFloat(parts[4]), result.velocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[5]), result.velocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[6]), result.angularVelocity, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), result.worldPointX, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), result.worldPointY, 0.0f);
        assertEquals(Float.parseFloat(parts[9]), result.localVelocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[10]), result.localVelocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[11]), result.worldVelocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[12]), result.worldVelocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[13]), result.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[14]), result.centerY, 0.0f);
        assertEquals(Float.parseFloat(parts[15]), result.rotationalInertia, 0.0f);
        assertEquals(Integer.parseInt(parts[16]), result.contactCount);
        assertEquals(Integer.parseInt(parts[17]), result.awakeBodyCount);
        assertEquals(upstream, result.toLine());
    }

    private static String runProbe(int stepCount) throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_weeble_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_weeble_sample_probe.c").toString());
        command.add("-lm");
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString(), Integer.toString(stepCount)).directory(root.toFile())
            .redirectErrorStream(true).start();
        String output = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertEquals(0, run.waitFor(), output);
        return output;
    }
}
