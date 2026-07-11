package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlaneSolverParityTest {
    @Test
    void planeSolverAndClipVectorMatchUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");

        b2CollisionPlane[] planes = new b2CollisionPlane[] {
            plane(new b2Vec2(1.0f, 0.0f), 0.25f, Float.MAX_VALUE, true),
            plane(new b2Vec2(0.0f, 1.0f), -0.1f, 0.2f, true),
            plane(new b2Vec2(-0.707106769f, 0.707106769f), -0.15f, 0.5f, false)
        };

        b2PlaneSolverResult result = b2SolvePlanes(new b2Vec2(-0.6f, -0.7f), planes, 3);
        assertVecLine(lines[0], "translation", result.translation);
        assertIntLine(lines[1], "iterations", result.iterationCount);
        assertPushLine(lines[2], planes);
        assertVecLine(lines[3], "clip", b2ClipVector(new b2Vec2(-2.0f, -3.0f), planes, 3));

        b2CollisionPlane[] inactive = new b2CollisionPlane[] {
            plane(new b2Vec2(1.0f, 0.0f), 0.0f, 1.0f, true),
            plane(new b2Vec2(0.0f, 1.0f), 0.0f, 1.0f, false)
        };
        inactive[1].push = 1.0f;
        assertVecLine(lines[4], "clipInactive", b2ClipVector(new b2Vec2(-2.0f, -3.0f), inactive, 2));
    }

    private static b2CollisionPlane plane(b2Vec2 normal, float offset, float pushLimit, boolean clipVelocity) {
        b2CollisionPlane plane = new b2CollisionPlane();
        plane.plane = new b2Plane(normal, offset);
        plane.pushLimit = pushLimit;
        plane.clipVelocity = clipVelocity;
        return plane;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_plane_solver_probe");

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
        command.add(root.resolve("tools/parity/box2d_plane_solver_probe.c").toString());
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

    private static void assertVecLine(String line, String label, b2Vec2 value) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Float.parseFloat(parts[1]), value.x, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), value.y, 0.0f);
    }

    private static void assertIntLine(String line, String label, int value) {
        String[] parts = line.split("\\s+");
        assertEquals(label, parts[0]);
        assertEquals(Integer.parseInt(parts[1]), value);
    }

    private static void assertPushLine(String line, b2CollisionPlane[] planes) {
        String[] parts = line.split("\\s+");
        assertEquals("push", parts[0]);
        assertEquals(Float.parseFloat(parts[1]), planes[0].push, 0.0f);
        assertEquals(Float.parseFloat(parts[2]), planes[1].push, 0.0f);
        assertEquals(Float.parseFloat(parts[3]), planes[2].push, 0.0f);
    }
}
