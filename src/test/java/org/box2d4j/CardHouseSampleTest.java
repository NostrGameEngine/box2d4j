package org.box2d4j;

import org.box2d4j.samples.CardHouse;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CardHouseSampleTest {
    private static final float DEFAULT_STEP_TOLERANCE = 2.0e-6f;

    @Test
    void sampleMatchesUpstreamCCardHouse() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("cardHouse", parts[0]);

        CardHouse.Result result = CardHouse.run();
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.jointCount);
        assertEquals(Integer.parseInt(parts[5]), result.awakeBodyCount);
        int bodyCount = Integer.parseInt(parts[6]);
        assertEquals(bodyCount, result.bodies.length);

        int index = 7;
        for (int i = 0; i < bodyCount; ++i) {
            assertBody(parts, index, result.bodies[i], DEFAULT_STEP_TOLERANCE);
            index += 9;
        }
        assertEquals(parts.length, index);
    }

    @Test
    void shortHorizonMatchesUpstreamCCardHouseExactly() throws Exception {
        String upstream = runProbe(2);
        String[] parts = upstream.split("\\s+");
        assertEquals("cardHouse", parts[0]);

        CardHouse.Result result = CardHouse.run(2);
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.jointCount);
        assertEquals(Integer.parseInt(parts[5]), result.awakeBodyCount);
        int bodyCount = Integer.parseInt(parts[6]);
        assertEquals(bodyCount, result.bodies.length);

        int index = 7;
        for (int i = 0; i < bodyCount; ++i) {
            assertBody(parts, index, result.bodies[i], 0.0f);
            index += 9;
        }
        assertEquals(parts.length, index);
    }

    private static void assertBody(String[] parts, int index, CardHouse.BodyState body, float tolerance) {
        assertEquals(Float.parseFloat(parts[index]), body.x, tolerance);
        assertEquals(Float.parseFloat(parts[index + 1]), body.y, tolerance);
        assertEquals(Float.parseFloat(parts[index + 2]), body.cos, tolerance);
        assertEquals(Float.parseFloat(parts[index + 3]), body.sin, tolerance);
        assertEquals(Float.parseFloat(parts[index + 4]), body.velocityX, tolerance);
        assertEquals(Float.parseFloat(parts[index + 5]), body.velocityY, tolerance);
        assertEquals(Float.parseFloat(parts[index + 6]), body.angularVelocity, tolerance);
        assertEquals(Integer.parseInt(parts[index + 7]), body.shapeCount);
        assertEquals(Integer.parseInt(parts[index + 8]), body.contactCapacity);
    }

    private static String runProbe() throws Exception {
        return runProbe(null);
    }

    private static String runProbe(Integer stepCount) throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_card_house_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_card_house_sample_probe.c").toString());
        command.add("-lm");
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        List<String> runCommand = new ArrayList<>();
        runCommand.add(probe.toString());
        if (stepCount != null) {
            runCommand.add(Integer.toString(stepCount));
        }
        Process run = new ProcessBuilder(runCommand).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertEquals(0, run.waitFor(), output);
        return output;
    }
}
