package org.box2d4j;

import org.box2d4j.samples.Platformer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlatformerSampleTest {
    @Test
    void sampleMatchesUpstreamCPlatformer() throws Exception {
        String upstream = runProbe();
        String[] parts = upstream.split("\\s+");
        assertEquals("platformer", parts[0]);

        Platformer.Result result = Platformer.run();
        assertEquals(Integer.parseInt(parts[1]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[2]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[3]), result.contactCount);
        assertEquals(Integer.parseInt(parts[4]), result.jointCount);
        assertEquals(Integer.parseInt(parts[5]), result.awakeBodyCount);
        assertEquals(Integer.parseInt(parts[6]), result.preSolveCalls);
        assertEquals(Integer.parseInt(parts[7]), result.preSolveDisabled);
        assertEquals(Integer.parseInt(parts[8]) != 0, result.canJump);
        assertEquals(Integer.parseInt(parts[9]) != 0, result.jumping);
        assertEquals(Float.parseFloat(parts[10]), result.jumpDelay, 0.0f);
        assertContact(parts, 11, result.movingContact);
        assertContact(parts, 15, result.playerContact);
        assertBody(parts, 19, result.playerBody);
        assertBody(parts, 28, result.movingPlatformBody);
        assertEquals(Integer.parseInt(parts[37]) != 0, result.playerShapeValid);
        assertEquals(parts.length, 38);
    }

    private static void assertContact(String[] parts, int index, Platformer.ContactState contact) {
        assertEquals(Integer.parseInt(parts[index]), contact.capacity);
        assertEquals(Integer.parseInt(parts[index + 1]), contact.count);
        assertEquals(Integer.parseInt(parts[index + 2]), contact.pointCount);
        assertEquals(Integer.parseInt(parts[index + 3]), contact.normalChecksum);
    }

    private static void assertBody(String[] parts, int index, Platformer.BodyState body) {
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

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_platformer_sample_probe");

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
        command.add("-I" + root.resolve("vendor/box2d/shared"));
        command.addAll(sources);
        command.add(root.resolve("tools/parity/box2d_platformer_sample_probe.c").toString());
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
