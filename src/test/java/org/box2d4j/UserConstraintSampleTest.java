package org.box2d4j;

import org.box2d4j.samples.UserConstraint;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class UserConstraintSampleTest {
    @Test
    void sampleMatchesUpstreamCUserConstraint() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("userConstraint", parts[0]);

        UserConstraint.Result result = UserConstraint.run();
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.jointCount);
        assertEquals(Integer.parseInt(parts[5]), result.awakeBodyCount);
        assertEquals(Float.parseFloat(parts[6]), result.x, 0.0f);
        assertEquals(Float.parseFloat(parts[7]), result.y, 0.0f);
        assertEquals(Float.parseFloat(parts[8]), result.cos, 0.0f);
        assertEquals(Float.parseFloat(parts[9]), result.sin, 0.0f);
        assertEquals(Float.parseFloat(parts[10]), result.velocityX, 0.0f);
        assertEquals(Float.parseFloat(parts[11]), result.velocityY, 0.0f);
        assertEquals(Float.parseFloat(parts[12]), result.angularVelocity, 0.0f);
        assertEquals(Float.parseFloat(parts[13]), result.mass, 0.0f);
        assertEquals(Float.parseFloat(parts[14]), result.rotationalInertia, 0.0f);
        assertEquals(Float.parseFloat(parts[15]), result.centerX, 0.0f);
        assertEquals(Float.parseFloat(parts[16]), result.centerY, 0.0f);
        assertEquals(Float.parseFloat(parts[17]), result.impulse0, 0.0f);
        assertEquals(Float.parseFloat(parts[18]), result.impulse1, 0.0f);
        assertEquals(Float.parseFloat(parts[19]), result.force0, 0.0f);
        assertEquals(Float.parseFloat(parts[20]), result.force1, 0.0f);
        assertEquals(parts.length, 21);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_user_constraint_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_user_constraint_sample_probe.c").toString());
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
