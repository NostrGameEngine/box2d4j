package org.box2d4j;

import org.box2d4j.samples.SensorTypes;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SensorTypesSampleTest {
    @Test
    void sampleMatchesUpstreamCSensorTypes() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("sensorTypes", parts[0]);

        SensorTypes.Result result = SensorTypes.run();
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.jointCount);
        assertEquals(Integer.parseInt(parts[5]), result.awakeBodyCount);
        assertEquals(Integer.parseInt(parts[6]), result.beginTotal);
        assertEquals(Integer.parseInt(parts[7]), result.endTotal);
        assertSensor(parts, 8, result.staticSensor);
        assertSensor(parts, 12, result.kinematicSensor);
        assertSensor(parts, 16, result.dynamicSensor);
        assertBody(parts, 20, result.kinematicBody);
        assertBody(parts, 29, result.dynamicBody);
        assertBody(parts, 38, result.ballBody);
        assertRay(parts, 47, result.ray);
        assertEquals(parts.length, 57);
    }

    private static void assertSensor(String[] parts, int index, SensorTypes.SensorState sensor) {
        assertEquals(Integer.parseInt(parts[index]), sensor.capacity);
        assertEquals(Integer.parseInt(parts[index + 1]), sensor.count);
        assertEquals(Integer.parseInt(parts[index + 2]), sensor.indexSum);
        assertEquals(Integer.parseInt(parts[index + 3]), sensor.generationSum);
    }

    private static void assertBody(String[] parts, int index, SensorTypes.BodyState body) {
        assertEquals(Float.parseFloat(parts[index]), body.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 1]), body.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 2]), body.cos, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 3]), body.sin, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 4]), body.velocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 5]), body.velocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 6]), body.angularVelocity, 0.0f);
        assertEquals(Integer.parseInt(parts[index + 7]), body.shapeCount);
        assertEquals(Integer.parseInt(parts[index + 8]), body.contactCapacity);
    }

    private static void assertRay(String[] parts, int index, SensorTypes.RayState ray) {
        assertEquals(Integer.parseInt(parts[index]) != 0, ray.hit);
        assertEquals(Float.parseFloat(parts[index + 1]), ray.fraction, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 2]), ray.pointX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 3]), ray.pointY, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 4]), ray.normalX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 5]), ray.normalY, 0.0f);
        assertEquals(Integer.parseInt(parts[index + 6]), ray.shapeIndex);
        assertEquals(Integer.parseInt(parts[index + 7]), ray.shapeGeneration);
        assertEquals(Integer.parseInt(parts[index + 8]), ray.nodeVisits);
        assertEquals(Integer.parseInt(parts[index + 9]), ray.leafVisits);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_sensor_types_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_sensor_types_sample_probe.c").toString());
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
