package org.box2d4j;

import org.box2d4j.samples.RoundedShapes;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class RoundedShapesSampleTest {
    @Test
    void sampleMatchesUpstreamCRoundedShapes() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("roundedShapes", parts[0]);

        RoundedShapes.Result result = RoundedShapes.run();
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.awakeBodyCount);
        int sampleCount = Integer.parseInt(parts[5]);
        assertEquals(sampleCount, result.bodies.length);
        assertEquals(sampleCount, result.shapes.length);

        int index = 6;
        for (int i = 0; i < sampleCount; ++i) {
            assertBody(parts, index, result.bodies[i]);
            index += 6;
            int vertexCount = assertShape(parts, index, result.shapes[i]);
            index += 2 + 2 * vertexCount;
        }
        assertEquals(upstream, result.toLine());
    }

    private static void assertBody(String[] parts, int index, RoundedShapes.BodyState body) {
        assertEquals(Float.parseFloat(parts[index]), body.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 1]), body.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 2]), body.angle, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 3]), body.velocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 4]), body.velocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 5]), body.angularVelocity, 0.0f);
    }

    private static int assertShape(String[] parts, int index, RoundedShapes.ShapeState shape) {
        int count = Integer.parseInt(parts[index]);
        assertEquals(count, shape.count);
        assertEquals(Float.parseFloat(parts[index + 1]), shape.radius, 0.0f);
        for (int i = 0; i < count; ++i) {
            assertEquals(Float.parseFloat(parts[index + 2 + 2 * i]), shape.vertices[i].x, 0.0f);
            assertEquals(Float.parseFloat(parts[index + 3 + 2 * i]), shape.vertices[i].y, 0.0f);
        }
        return count;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_rounded_shapes_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_rounded_shapes_sample_probe.c").toString());
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
