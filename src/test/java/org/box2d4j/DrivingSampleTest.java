package org.box2d4j;

import org.box2d4j.samples.Driving;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DrivingSampleTest {
    private static final float WHEEL_FORCE_TOLERANCE = 2.0e-5f;

    @Test
    void sampleMatchesUpstreamCDriving() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("driving", parts[0]);

        Driving.Result result = Driving.run();
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.jointCount);
        assertEquals(Integer.parseInt(parts[5]), result.awakeBodyCount);
        int carBodyCount = Integer.parseInt(parts[6]);
        int bridgeBodyCount = Integer.parseInt(parts[7]);
        int boxCount = Integer.parseInt(parts[8]);
        int axleCount = Integer.parseInt(parts[9]);
        assertEquals(carBodyCount, result.carBodies.length);
        assertEquals(bridgeBodyCount, result.bridgeBodies.length);
        assertEquals(boxCount, result.boxes.length);
        assertEquals(axleCount, result.axles.length);

        int index = 10;
        assertBody(parts, index, result.teeter);
        index += 8;
        for (Driving.BodyState body : result.carBodies) {
            assertBody(parts, index, body);
            index += 8;
        }
        for (Driving.BodyState body : result.bridgeBodies) {
            assertBody(parts, index, body);
            index += 8;
        }
        for (Driving.BodyState body : result.boxes) {
            assertBody(parts, index, body);
            index += 8;
        }
        for (Driving.AxleState axle : result.axles) {
            assertAxle(parts, index, axle);
            index += 9;
        }
        assertEquals(parts.length, index);
    }

    private static void assertBody(String[] parts, int index, Driving.BodyState body) {
        assertEquals(Float.parseFloat(parts[index]), body.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 1]), body.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 2]), body.cos, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 3]), body.sin, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 4]), body.velocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 5]), body.velocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 6]), body.angularVelocity, 0.0f);
        assertEquals(Integer.parseInt(parts[index + 7]), body.contactCapacity);
    }

    private static void assertAxle(String[] parts, int index, Driving.AxleState axle) {
        assertEquals(Float.parseFloat(parts[index]), axle.motorSpeed, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 1]), axle.maxMotorTorque, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 2]), axle.motorTorque, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 3]), axle.springHertz, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 4]), axle.dampingRatio, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 5]), axle.linearSeparation, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 6]), axle.forceX, WHEEL_FORCE_TOLERANCE);
        assertEquals(Float.parseFloat(parts[index + 7]), axle.forceY, WHEEL_FORCE_TOLERANCE);
        assertEquals(Float.parseFloat(parts[index + 8]), axle.torque, 0.0f);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_driving_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_driving_sample_probe.c").toString());
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
