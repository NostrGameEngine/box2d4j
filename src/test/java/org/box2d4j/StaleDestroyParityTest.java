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

final class StaleDestroyParityTest {
    @Test
    void destroyingStaleHandlesMatchesUpstreamAssertions() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        List<Runnable> cases = new ArrayList<>();

        b2WorldId staleWorld = b2CreateWorld(b2DefaultWorldDef());
        b2DestroyWorld(staleWorld);
        cases.add(() -> b2DestroyWorld(staleWorld));

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId bodyA = b2CreateBody(worldId, b2DefaultBodyDef());
        b2BodyDef dynamicDef = b2DefaultBodyDef();
        dynamicDef.type = b2_dynamicBody;
        b2BodyId bodyB = b2CreateBody(worldId, dynamicDef);

        b2ShapeId staleShape = b2CreateCircleShape(bodyB, b2DefaultShapeDef(),
            new b2Circle(new b2Vec2(), 0.5f));
        b2DestroyShape(staleShape, true);
        cases.add(() -> b2DestroyShape(staleShape, true));

        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = new b2Vec2[] {
            new b2Vec2(-2.0f, 0.0f), new b2Vec2(-1.0f, 0.0f),
            new b2Vec2(0.0f, 0.0f), new b2Vec2(1.0f, 0.0f)
        };
        chainDef.count = chainDef.points.length;
        b2ChainId staleChain = b2CreateChain(bodyA, chainDef);
        b2DestroyChain(staleChain);
        cases.add(() -> b2DestroyChain(staleChain));

        b2DistanceJointDef jointDef = b2DefaultDistanceJointDef();
        jointDef.bodyIdA = bodyA;
        jointDef.bodyIdB = bodyB;
        jointDef.length = 1.0f;
        b2JointId staleJoint = b2CreateDistanceJoint(worldId, jointDef);
        b2DestroyJoint(staleJoint);
        cases.add(() -> b2DestroyJoint(staleJoint));

        b2BodyId staleBody = b2CreateBody(worldId, dynamicDef);
        b2DestroyBody(staleBody);
        cases.add(() -> b2DestroyBody(staleBody));

        StringBuilder output = new StringBuilder("staleDestroy");
        for (Runnable testCase : cases) {
            output.append(' ').append(asserts(testCase));
        }
        b2DestroyWorld(worldId);
        return output.toString();
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
        Path probe = outputDir.resolve("box2d_stale_destroy_probe");

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
        command.add(root.resolve("tools/parity/box2d_stale_destroy_probe.c").toString());
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
