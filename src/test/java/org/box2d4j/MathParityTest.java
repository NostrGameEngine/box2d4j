package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class MathParityTest {
    @Test
    void selectedMathOutputsMatchUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        List<String> actual = new ArrayList<>();

        float[][] atanInputs = {
            {1.0f, 0.0f},
            {-1.0f, 0.0f},
            {0.0f, 1.0f},
            {0.0f, -1.0f},
            {0.0f, 0.0f},
            {0.75f, -0.25f},
            {-0.33f, 0.91f},
        };
        for (int i = 0; i < atanInputs.length; ++i) {
            actual.add(format("atan %d %.9g", i, b2Atan2(atanInputs[i][0], atanInputs[i][1])));
        }

        float[] angles = {-10.0f * B2_PI, -3.25f, -1.0f, 0.0f, 0.5f * B2_PI, 2.75f, 10.0f * B2_PI};
        for (int i = 0; i < angles.length; ++i) {
            b2Rot q = b2MakeRot(angles[i]);
            actual.add(format("rot %d %.9g %.9g %.9g", i, q.c, q.s, b2UnwindAngle(angles[i])));
        }

        b2Vec2 zero = b2Vec2_zero.copy();
        b2Vec2 one = new b2Vec2(1.0f, 1.0f);
        b2Vec2 two = new b2Vec2(2.0f, 2.0f);
        actual.add(format("add %.9g %.9g", b2Add(one, two).x, b2Add(one, two).y));
        actual.add(format("sub %.9g %.9g", b2Sub(zero, two).x, b2Sub(zero, two).y));

        b2Transform transform1 = new b2Transform(new b2Vec2(-2.0f, 3.0f), b2MakeRot(1.0f));
        b2Transform transform2 = new b2Transform(new b2Vec2(1.0f, 0.0f), b2MakeRot(-2.0f));
        b2Transform transform = b2MulTransforms(transform2, transform1);
        addVec(actual, "transformNested", b2TransformPoint(transform2, b2TransformPoint(transform1, two)));
        addVec(actual, "transformMul", b2TransformPoint(transform, two));
        addVec(actual, "invTransform", b2InvTransformPoint(transform1, b2TransformPoint(transform1, two)));

        b2Vec2 v = b2Normalize(new b2Vec2(0.2f, -0.5f));
        b2Vec2[] unitVectors = {
            b2Normalize(new b2Vec2(1.0f, 0.25f)),
            b2Normalize(new b2Vec2(-0.2f, 0.9f)),
            b2Normalize(new b2Vec2(-0.7f, -0.3f)),
        };
        for (int i = 0; i < unitVectors.length; ++i) {
            b2Rot r = b2ComputeRotationBetweenUnitVectors(v, unitVectors[i]);
            b2Vec2 w = b2RotateVector(r, v);
            actual.add(format("between %d %.9g %.9g %.9g %.9g", i, r.c, r.s, w.x, w.y));
        }

        b2Rot q1 = b2Rot_identity.copy();
        b2Rot q2 = b2MakeRot(0.5f * B2_PI);
        float[] alphas = {0.0f, 0.1f, 0.5f, 0.9f, 1.0f};
        for (float alpha : alphas) {
            b2Rot q = b2NLerp(q1, q2, alpha);
            actual.add(format("nlerp %.9g %.9g %.9g", q.c, q.s, b2Rot_GetAngle(q)));
        }

        assertEquals(normalizeNumericTokens(String.join("\n", lines)), normalizeNumericTokens(String.join("\n", actual)));
    }

    private static void addVec(List<String> lines, String label, b2Vec2 v) {
        lines.add(format("%s %.9g %.9g", label, v.x, v.y));
    }

    private static String format(String pattern, Object... args) {
        return String.format(Locale.ROOT, pattern, args);
    }

    private static String normalizeNumericTokens(String value) {
        String[] lines = value.split("\\R");
        List<String> normalizedLines = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            for (int i = 0; i < parts.length; ++i) {
                if (parts[i].matches("[-+]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][-+]?\\d+)?")) {
                    parts[i] = Float.toString(Float.parseFloat(parts[i]));
                }
            }
            normalizedLines.add(String.join(" ", parts));
        }
        return String.join("\n", normalizedLines);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_math_probe");

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
        command.add(root.resolve("tools/parity/box2d_math_probe.c").toString());
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
