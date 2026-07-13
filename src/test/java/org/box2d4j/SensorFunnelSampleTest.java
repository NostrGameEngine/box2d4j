package org.box2d4j;

import org.box2d4j.samples.SensorFunnel;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SensorFunnelSampleTest {
    private static final int STEP_COUNT = 2400;

    @Test
    void sampleMatchesUpstreamCSensorFunnel() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("sensorFunnel", parts[0]);

        SensorFunnel.Result result = SensorFunnel.run(STEP_COUNT);
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.jointCount);
        assertEquals(Integer.parseInt(parts[5]), result.awakeBodyCount);
        assertEquals(Integer.parseInt(parts[6]), result.type);
        assertEquals(Integer.parseInt(parts[7]), result.createdTotal);
        assertEquals(Integer.parseInt(parts[8]), result.destroyedTotal);
        assertEquals(Integer.parseInt(parts[9]), result.activeCount);
        assertEquals(Long.parseLong(parts[10]), result.activeMask);
        assertEquals(Integer.parseInt(parts[11]), result.beginTotal);
        assertEquals(Integer.parseInt(parts[12]), result.endTotal);
        assertEquals(Integer.parseInt(parts[13]), result.lastBeginCount);
        assertEquals(Float.parseFloat(parts[14]), result.wait, 0.0f);
        assertEquals(Float.parseFloat(parts[15]), result.side, 0.0f);
        int bodyCount = Integer.parseInt(parts[16]);
        assertEquals(bodyCount, result.bodies.length);

        int index = 17;
        for (int i = 0; i < bodyCount; ++i) {
            assertBody(parts, index, result.bodies[i]);
            index += 11;
        }
        assertEquals(parts.length, index);
    }

    private static void assertBody(String[] parts, int index, SensorFunnel.BodyState body) {
        assertEquals(Integer.parseInt(parts[index]), body.elementIndex);
        assertEquals(Integer.parseInt(parts[index + 1]), body.boneIndex);
        assertEquals(Float.parseFloat(parts[index + 2]), body.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 3]), body.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 4]), body.cos, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 5]), body.sin, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 6]), body.velocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 7]), body.velocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 8]), body.angularVelocity, 0.0f);
        assertEquals(Integer.parseInt(parts[index + 9]), body.shapeCount);
        assertEquals(Integer.parseInt(parts[index + 10]), body.contactCapacity);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_sensor_funnel_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_sensor_funnel_sample_probe.c").toString());
        command.add("-lm");
        command.add("-o");
        command.add(probe.toString());

        Process compile = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String compileOutput = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, compile.waitFor(), compileOutput);

        Process run = new ProcessBuilder(probe.toString(), Integer.toString(STEP_COUNT))
            .directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertEquals(0, run.waitFor(), output);
        return output;
    }
}
