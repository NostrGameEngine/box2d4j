package org.box2d4j;

import org.box2d4j.samples.ModifyGeometry;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ModifyGeometrySampleTest {
    @Test
    void sampleMatchesUpstreamCModifyGeometry() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("modifyGeometry", parts[0]);

        ModifyGeometry.Result result = ModifyGeometry.run();
        for (int i = 0; i < result.states.length; ++i) {
            assertState(parts, 1 + 19 * i, result.states[i]);
        }
        assertEquals(upstream, result.toLine());
    }

    private static void assertState(String[] parts, int index, ModifyGeometry.State state) {
        assertEquals(parts[index], state.label);
        assertEquals(Integer.parseInt(parts[index + 1]), state.bodyType);
        assertEquals(Integer.parseInt(parts[index + 2]), state.shapeType);
        assertEquals(Float.parseFloat(parts[index + 3]), state.x, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 4]), state.y, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 5]), state.angle, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 6]), state.velocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 7]), state.velocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 8]), state.angularVelocity, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 9]), state.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 10]), state.centerX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 11]), state.centerY, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 12]), state.rotationalInertia, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 13]), state.lowerX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 14]), state.lowerY, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 15]), state.upperX, 0.0f);
        assertEquals(Float.parseFloat(parts[index + 16]), state.upperY, 0.0f);
        assertEquals(Integer.parseInt(parts[index + 17]), state.contactCount);
        assertEquals(Integer.parseInt(parts[index + 18]), state.awakeBodyCount);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_modify_geometry_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_modify_geometry_sample_probe.c").toString());
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
