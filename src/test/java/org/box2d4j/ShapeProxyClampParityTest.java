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

final class ShapeProxyClampParityTest {
    @Test
    void proxyFactoriesClampOversizedPointCounts() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        b2Vec2[] points = new b2Vec2[10];
        for (int i = 0; i < points.length; ++i) {
            points[i] = new b2Vec2(i + 0.25f, -2.0f * i);
        }
        b2ShapeProxy plain = b2MakeProxy(points, points.length, 0.3f);
        b2ShapeProxy offset = b2MakeOffsetProxy(points, points.length, 0.4f,
            new b2Vec2(2.0f, -1.0f), b2MakeRot(0.25f));
        return String.format("shapeProxyClamp %d %08x %08x %08x %d %08x %08x %08x",
            plain.count, Float.floatToRawIntBits(plain.points[7].x), Float.floatToRawIntBits(plain.points[7].y),
            Float.floatToRawIntBits(plain.radius), offset.count, Float.floatToRawIntBits(offset.points[7].x),
            Float.floatToRawIntBits(offset.points[7].y), Float.floatToRawIntBits(offset.radius));
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_shape_proxy_clamp_probe");

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
        command.add(root.resolve("tools/parity/box2d_shape_proxy_clamp_probe.c").toString());
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
