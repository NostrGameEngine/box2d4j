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

final class DistanceValidationParityTest {
    @Test
    void gjkShapeCastAndToiAssertionsMatchC() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        List<Runnable> cases = new ArrayList<>();
        cases.add(() -> {
            b2SimplexCache cache = new b2SimplexCache();
            cache.count = 4;
            b2ShapeDistance(distanceInput(), cache, null, 0);
        });
        cases.add(() -> mutateDistance(input -> input.proxyA.count = 0));
        cases.add(() -> mutateDistance(input -> input.proxyB.count = 0));
        cases.add(() -> mutateDistance(input -> input.proxyA.radius = -1.0f));
        cases.add(() -> mutateDistance(input -> input.proxyB.radius = -1.0f));
        cases.add(() -> {
            b2DistanceInput distance = distanceInput();
            b2ShapeCastPairInput cast = new b2ShapeCastPairInput();
            cast.proxyA = distance.proxyA;
            cast.proxyB = distance.proxyB;
            cast.proxyA.radius = -1.0f;
            cast.transformA = b2Transform_identity;
            cast.transformB = b2Transform_identity;
            cast.maxFraction = 1.0f;
            b2ShapeCast(cast);
        });
        cases.add(() -> mutateToi(input -> input.sweepA.q1 = new b2Rot()));
        cases.add(() -> mutateToi(input -> input.sweepA.q2 = new b2Rot()));
        cases.add(() -> mutateToi(input -> input.sweepB.q1 = new b2Rot()));
        cases.add(() -> mutateToi(input -> input.sweepB.q2 = new b2Rot()));
        cases.add(() -> b2TimeOfImpact(toiInput()));

        StringBuilder output = new StringBuilder("distanceValidation");
        for (Runnable testCase : cases) {
            output.append(' ').append(asserts(testCase));
        }
        return output.toString();
    }

    private static void mutateDistance(java.util.function.Consumer<b2DistanceInput> mutation) {
        b2DistanceInput input = distanceInput();
        mutation.accept(input);
        b2ShapeDistance(input, new b2SimplexCache(), null, 0);
    }

    private static void mutateToi(java.util.function.Consumer<b2TOIInput> mutation) {
        b2TOIInput input = toiInput();
        mutation.accept(input);
        b2TimeOfImpact(input);
    }

    private static b2DistanceInput distanceInput() {
        b2DistanceInput input = new b2DistanceInput();
        input.proxyA = b2MakeProxy(new b2Vec2[] {new b2Vec2()}, 1, 0.1f);
        input.proxyB = b2MakeProxy(new b2Vec2[] {new b2Vec2(2.0f, 0.0f)}, 1, 0.2f);
        input.transformA = b2Transform_identity;
        input.transformB = b2Transform_identity;
        return input;
    }

    private static b2TOIInput toiInput() {
        b2DistanceInput distance = distanceInput();
        b2TOIInput input = new b2TOIInput();
        input.proxyA = distance.proxyA;
        input.proxyB = distance.proxyB;
        input.sweepA.q1 = b2Rot_identity.copy();
        input.sweepA.q2 = b2Rot_identity.copy();
        input.sweepB.q1 = b2Rot_identity.copy();
        input.sweepB.q2 = b2Rot_identity.copy();
        input.maxFraction = 1.0f;
        return input;
    }

    private static int asserts(Runnable testCase) {
        try {
            testCase.run();
            return 0;
        } catch (AssertionError expected) {
            return 1;
        }
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_distance_validation_probe");

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted().map(Path::toString).forEach(command::add);
        }
        command.add(root.resolve("tools/parity/box2d_distance_validation_probe.c").toString());
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
