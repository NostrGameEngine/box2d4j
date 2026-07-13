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

final class FactoryValidationParityTest {
    @Test
    void definitionCookiesAndJointCreationLimitsMatchUpstreamAssertions() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId bodyA = b2CreateBody(worldId, b2DefaultBodyDef());
        b2BodyId bodyB = b2CreateBody(worldId, b2DefaultBodyDef());
        b2Circle circle = new b2Circle(new b2Vec2(), 0.5f);

        List<Runnable> cases = new ArrayList<>();
        cases.add(() -> b2CreateWorld(new b2WorldDef()));
        cases.add(() -> b2CreateBody(worldId, new b2BodyDef()));
        cases.add(() -> b2CreateCircleShape(bodyA, new b2ShapeDef(), circle));
        cases.add(() -> b2CreateChain(bodyA, new b2ChainDef()));
        cases.add(() -> b2CreateDistanceJoint(worldId, new b2DistanceJointDef()));
        cases.add(() -> b2CreateMotorJoint(worldId, new b2MotorJointDef()));
        cases.add(() -> b2CreateMouseJoint(worldId, new b2MouseJointDef()));
        cases.add(() -> b2CreateFilterJoint(worldId, new b2FilterJointDef()));
        cases.add(() -> b2CreateRevoluteJoint(worldId, new b2RevoluteJointDef()));
        cases.add(() -> b2CreatePrismaticJoint(worldId, new b2PrismaticJointDef()));
        cases.add(() -> b2CreateWeldJoint(worldId, new b2WeldJointDef()));
        cases.add(() -> b2CreateWheelJoint(worldId, new b2WheelJointDef()));

        b2DistanceJointDef distance = b2DefaultDistanceJointDef();
        distance.bodyIdA = bodyA;
        distance.bodyIdB = bodyB;
        distance.length = 0.0f;
        cases.add(() -> b2CreateDistanceJoint(worldId, distance));

        b2RevoluteJointDef revoluteOrder = revoluteDef(bodyA, bodyB);
        revoluteOrder.lowerAngle = 1.0f;
        revoluteOrder.upperAngle = -1.0f;
        cases.add(() -> b2CreateRevoluteJoint(worldId, revoluteOrder));
        b2RevoluteJointDef revoluteLower = revoluteDef(bodyA, bodyB);
        revoluteLower.lowerAngle = -B2_PI;
        cases.add(() -> b2CreateRevoluteJoint(worldId, revoluteLower));
        b2RevoluteJointDef revoluteUpper = revoluteDef(bodyA, bodyB);
        revoluteUpper.upperAngle = B2_PI;
        cases.add(() -> b2CreateRevoluteJoint(worldId, revoluteUpper));

        b2PrismaticJointDef prismatic = b2DefaultPrismaticJointDef();
        prismatic.bodyIdA = bodyA;
        prismatic.bodyIdB = bodyB;
        prismatic.lowerTranslation = 1.0f;
        prismatic.upperTranslation = -1.0f;
        cases.add(() -> b2CreatePrismaticJoint(worldId, prismatic));
        b2WheelJointDef wheel = b2DefaultWheelJointDef();
        wheel.bodyIdA = bodyA;
        wheel.bodyIdB = bodyB;
        wheel.lowerTranslation = 1.0f;
        wheel.upperTranslation = -1.0f;
        cases.add(() -> b2CreateWheelJoint(worldId, wheel));

        StringBuilder output = new StringBuilder("factoryValidation");
        for (Runnable testCase : cases) {
            output.append(' ').append(asserts(testCase));
        }
        b2DestroyWorld(worldId);
        return output.toString();
    }

    private static b2RevoluteJointDef revoluteDef(b2BodyId bodyA, b2BodyId bodyB) {
        b2RevoluteJointDef def = b2DefaultRevoluteJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        return def;
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
        Path probe = outputDir.resolve("box2d_factory_validation_probe");

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
        command.add(root.resolve("tools/parity/box2d_factory_validation_probe.c").toString());
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
