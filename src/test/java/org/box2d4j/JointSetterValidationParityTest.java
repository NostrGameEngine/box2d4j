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

final class JointSetterValidationParityTest {
    @Test
    void jointSetterAssertionsMatchUpstream() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId bodyA = b2CreateBody(worldId, b2DefaultBodyDef());
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);

        b2PrismaticJointDef prismaticDef = b2DefaultPrismaticJointDef();
        prismaticDef.bodyIdA = bodyA;
        prismaticDef.bodyIdB = bodyB;
        b2JointId prismatic = b2CreatePrismaticJoint(worldId, prismaticDef);
        b2MouseJointDef mouseDef = b2DefaultMouseJointDef();
        mouseDef.bodyIdA = bodyA;
        mouseDef.bodyIdB = bodyB;
        b2JointId mouse = b2CreateMouseJoint(worldId, mouseDef);
        b2WeldJointDef weldDef = b2DefaultWeldJointDef();
        weldDef.bodyIdA = bodyA;
        weldDef.bodyIdB = bodyB;
        b2JointId weld = b2CreateWeldJoint(worldId, weldDef);
        b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
        revoluteDef.bodyIdA = bodyA;
        revoluteDef.bodyIdB = bodyB;
        b2JointId revolute = b2CreateRevoluteJoint(worldId, revoluteDef);
        b2WheelJointDef wheelDef = b2DefaultWheelJointDef();
        wheelDef.bodyIdA = bodyA;
        wheelDef.bodyIdB = bodyB;
        b2JointId wheel = b2CreateWheelJoint(worldId, wheelDef);

        List<Runnable> cases = List.of(
            () -> b2Joint_SetLocalAnchorA(prismatic, new b2Vec2(Float.NaN, 0.0f)),
            () -> b2Joint_SetLocalAnchorB(prismatic, new b2Vec2(0.0f, Float.POSITIVE_INFINITY)),
            () -> b2Joint_SetReferenceAngle(prismatic, Float.NaN),
            () -> b2Joint_SetLocalAxisA(prismatic, new b2Vec2(Float.NaN, 0.0f)),
            () -> b2Joint_SetLocalAxisA(prismatic, new b2Vec2(2.0f, 0.0f)),
            () -> b2MouseJoint_SetTarget(mouse, new b2Vec2(Float.NaN, 0.0f)),
            () -> b2MouseJoint_SetSpringHertz(mouse, -1.0f),
            () -> b2MouseJoint_SetSpringDampingRatio(mouse, Float.NaN),
            () -> b2MouseJoint_SetMaxForce(mouse, -1.0f),
            () -> b2WeldJoint_SetLinearHertz(weld, -1.0f),
            () -> b2WeldJoint_SetLinearDampingRatio(weld, -1.0f),
            () -> b2WeldJoint_SetAngularHertz(weld, Float.NaN),
            () -> b2WeldJoint_SetAngularDampingRatio(weld, -1.0f),
            () -> b2PrismaticJoint_SetLimits(prismatic, 1.0f, -1.0f),
            () -> b2RevoluteJoint_SetLimits(revolute, 1.0f, -1.0f),
            () -> b2RevoluteJoint_SetLimits(revolute, -B2_PI, 0.0f),
            () -> b2RevoluteJoint_SetLimits(revolute, 0.0f, B2_PI),
            () -> b2WheelJoint_SetLimits(wheel, 1.0f, -1.0f),
            () -> b2Joint_SetConstraintTuning(prismatic, -1.0f, 1.0f),
            () -> b2Joint_SetConstraintTuning(prismatic, 1.0f, Float.NaN)
        );

        StringBuilder output = new StringBuilder("jointSetterValidation");
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
        Path probe = outputDir.resolve("box2d_joint_setter_validation_probe");

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
        command.add(root.resolve("tools/parity/box2d_joint_setter_validation_probe.c").toString());
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
