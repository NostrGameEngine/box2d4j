package org.box2d4j;

import org.box2d4j.samples.MotorJoint;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MotorJointSampleTest {
    @Test
    void sampleMatchesUpstreamCMotorJoint() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("motorJoint", parts[0]);

        MotorJoint.Result result = MotorJoint.run();
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.jointCount);
        assertEquals(Integer.parseInt(parts[5]), result.awakeBodyCount);
        assertEquals(Float.parseFloat(parts[6]), result.time, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), result.targetX, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), result.targetY, 0.0f);
        assertEquals(Float.parseFloat(parts[9]), result.angularOffset, 0.0f);
        assertEquals(Float.parseFloat(parts[10]), result.forceX, 0.0f);
        assertEquals(Float.parseFloat(parts[11]), result.forceY, 0.0f);
        assertEquals(Float.parseFloat(parts[12]), result.torque, 0.0f);
        assertBody(parts, 13, result.body);
        assertEquals(upstream, result.toLine());
    }

    private static void assertBody(String[] parts, int index, MotorJoint.BodyState body) {
        assertEquals(Float.parseFloat(parts[index]), body.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 1]), body.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 2]), body.cos, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 3]), body.sin, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 4]), body.velocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 5]), body.velocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 6]), body.angularVelocity, 0.0f);
        assertEquals(Integer.parseInt(parts[index + 7]), body.contactCapacity);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_motor_joint_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_motor_joint_sample_probe.c").toString());
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
