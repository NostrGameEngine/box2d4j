package org.box2d4j;

import org.box2d4j.samples.Mover;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MoverSampleTest {
    @Test
    void sampleMatchesUpstreamCMover() throws Exception {
        String[] parts = runProbe().split("\\s+");
        assertEquals("mover", parts[0]);

        Mover.Result result = Mover.run();
        int index = 1;
        assertEquals(Integer.parseInt(parts[index++]), result.bodyCount);
        assertEquals(Integer.parseInt(parts[index++]), result.shapeCount);
        assertEquals(Integer.parseInt(parts[index++]), result.contactCount);
        assertEquals(Integer.parseInt(parts[index++]), result.jointCount);
        assertEquals(Integer.parseInt(parts[index++]), result.awakeBodyCount);
        index = assertFloat(parts, index, result.moverX);
        index = assertFloat(parts, index, result.moverY);
        index = assertFloat(parts, index, result.moverCos);
        index = assertFloat(parts, index, result.moverSin);
        index = assertFloat(parts, index, result.velocityX);
        index = assertFloat(parts, index, result.velocityY);
        assertEquals(Integer.parseInt(parts[index++]) != 0, result.onGround);
        index = assertFloat(parts, index, result.pogoVelocity);
        assertEquals(Integer.parseInt(parts[index++]), result.totalIterations);
        index = assertFloat(parts, index, result.time);

        assertEquals(Integer.parseInt(parts[index++]) != 0, result.cast.hit);
        index = assertFloat(parts, index, result.cast.fraction);
        index = assertFloat(parts, index, result.cast.pointX);
        index = assertFloat(parts, index, result.cast.pointY);
        index = assertFloat(parts, index, result.cast.normalX);
        index = assertFloat(parts, index, result.cast.normalY);
        assertEquals(Integer.parseInt(parts[index++]) != 0, result.cast.bodyValid);

        int planeCount = Integer.parseInt(parts[index++]);
        assertEquals(planeCount, result.planes.length);
        for (int i = 0; i < planeCount; ++i) {
            Mover.PlaneState plane = result.planes[i];
            index = assertFloat(parts, index, plane.normalX);
            index = assertFloat(parts, index, plane.normalY);
            index = assertFloat(parts, index, plane.offset);
            index = assertFloat(parts, index, plane.pushLimit);
            index = assertFloat(parts, index, plane.push);
            assertEquals(Integer.parseInt(parts[index++]) != 0, plane.clipVelocity);
        }

        index = assertBody(parts, index, result.ball);
        index = assertBody(parts, index, result.elevator);
        index = assertBody(parts, index, result.bridgeFirst);
        index = assertBody(parts, index, result.bridgeMiddle);
        index = assertBody(parts, index, result.bridgeLast);
        assertEquals(Integer.parseInt(parts[index++]) != 0, result.ballShapeValid);
        assertEquals(parts.length, index);
    }

    private static int assertBody(String[] parts, int index, Mover.BodyState body) {
        index = assertFloat(parts, index, body.x);
        index = assertFloat(parts, index, body.y);
        index = assertFloat(parts, index, body.cos);
        index = assertFloat(parts, index, body.sin);
        index = assertFloat(parts, index, body.velocityX);
        index = assertFloat(parts, index, body.velocityY);
        index = assertFloat(parts, index, body.angularVelocity);
        assertEquals(Integer.parseInt(parts[index++]), body.contactCapacity);
        return index;
    }

    private static int assertFloat(String[] parts, int index, float actual) {
        assertEquals(Float.parseFloat(parts[index]), actual, 0.0f);
        return index + 1;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_mover_sample_probe");

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
        command.add(root.resolve("tools/parity/box2d_mover_sample_probe.c").toString());
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
