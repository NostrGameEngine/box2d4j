package org.box2d4j;

import org.box2d4j.samples.OffsetShapes;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class OffsetShapesSampleTest {
    @Test
    void sampleMatchesUpstreamCOffsetShapes() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("offsetShapes", parts[0]);

        OffsetShapes.Result result = OffsetShapes.run();
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        int bodyCount = Integer.parseInt(parts[4]);
        assertEquals(bodyCount, result.states.length);
        for (int i = 0; i < bodyCount; ++i) {
            assertBody(parts, 5 + 21 * i, result.states[i]);
        }
        assertEquals(upstream, result.toLine());
    }

    private static void assertBody(String[] parts, int index, OffsetShapes.BodyState body) {
        assertEquals(Float.parseFloat(parts[index]), body.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 1]), body.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 2]), body.angle, 0.0f);
        assertEquals(Integer.parseInt(parts[index + 3]), body.shapeCount);
        assertMass(parts, index + 4, body.massData);
        assertAabb(parts, index + 8, body.bodyAabb);
        assertEquals(Integer.parseInt(parts[index + 12]), body.shapeType);
        assertMass(parts, index + 13, body.shapeMass);
        assertAabb(parts, index + 17, body.shapeAabb);
    }

    private static void assertMass(String[] parts, int index, b2MassData massData) {
        assertEquals(Float.parseFloat(parts[index]), massData.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 1]), massData.center.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 2]), massData.center.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 3]), massData.rotationalInertia, 0.0f);
    }

    private static void assertAabb(String[] parts, int index, b2AABB aabb) {
        assertEquals(Float.parseFloat(parts[index]), aabb.lowerBound.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 1]), aabb.lowerBound.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 2]), aabb.upperBound.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 3]), aabb.upperBound.y, 0.0f);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_offset_shapes_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_offset_shapes_sample_probe.c").toString());
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
