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

final class JointSolverSetParityTest {
    @Test
    void jointsEnterDisabledSleepingAndAwakeSetsLikeUpstream() throws Exception {
        String[] expected = runProbe().split("\\R");
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2();

        b2WorldId worldId = b2CreateWorld(worldDef);
        b2BodyId bodyA = makeBody(worldId);
        b2BodyId bodyB = makeBody(worldId);
        b2Body_Disable(bodyA);
        b2JointId jointId = makeJoint(worldId, bodyA, bodyB);
        assertEquals(expected[0], String.format("disabled %d %d %d", bool(b2Joint_IsValid(jointId)),
            graphCount(worldId), b2World_GetCounters(worldId).jointCount));
        b2Body_Enable(bodyA);
        assertEquals(expected[1], String.format("enabled %d %d %d", bool(b2Body_IsAwake(bodyA)),
            bool(b2Body_IsAwake(bodyB)), graphCount(worldId)));
        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(worldDef);
        bodyA = makeBody(worldId);
        bodyB = makeBody(worldId);
        b2Body_SetAwake(bodyA, false);
        b2Body_SetAwake(bodyB, false);
        makeJoint(worldId, bodyA, bodyB);
        assertEquals(expected[2], String.format("sleeping %d %d %d", bool(b2Body_IsAwake(bodyA)),
            bool(b2Body_IsAwake(bodyB)), graphCount(worldId)));
        b2Body_SetAwake(bodyA, true);
        assertEquals(expected[3], String.format("woken %d %d %d", bool(b2Body_IsAwake(bodyA)),
            bool(b2Body_IsAwake(bodyB)), graphCount(worldId)));
        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(worldDef);
        bodyA = b2CreateBody(worldId, b2DefaultBodyDef());
        bodyB = makeBody(worldId);
        b2Body_Disable(bodyA);
        makeJoint(worldId, bodyA, bodyB);
        b2Body_SetAwake(bodyB, false);
        b2Body_Enable(bodyA);
        assertEquals(expected[4], String.format("staticEnable %d %d %d", bool(b2Body_IsAwake(bodyA)),
            bool(b2Body_IsAwake(bodyB)), graphCount(worldId)));
        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(worldDef);
        bodyA = makeBody(worldId);
        bodyB = makeBody(worldId);
        jointId = makeJoint(worldId, bodyA, bodyB);
        b2Body_SetAwake(bodyA, false);
        b2Joint_WakeBodies(jointId);
        assertEquals(expected[5], String.format("jointWake %d %d %d", bool(b2Body_IsAwake(bodyA)),
            bool(b2Body_IsAwake(bodyB)), graphCount(worldId)));
        b2DestroyWorld(worldId);
    }

    private static b2BodyId makeBody(b2WorldId worldId) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        return b2CreateBody(worldId, bodyDef);
    }

    private static b2JointId makeJoint(b2WorldId worldId, b2BodyId bodyA, b2BodyId bodyB) {
        b2DistanceJointDef jointDef = b2DefaultDistanceJointDef();
        jointDef.bodyIdA = bodyA;
        jointDef.bodyIdB = bodyB;
        jointDef.length = 1.0f;
        return b2CreateDistanceJoint(worldId, jointDef);
    }

    private static int graphCount(b2WorldId worldId) {
        int count = 0;
        for (int colorCount : b2World_GetCounters(worldId).colorCounts) {
            count += colorCount;
        }
        return count;
    }

    private static int bool(boolean value) {
        return value ? 1 : 0;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_joint_set_probe");

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-Wall");
        command.add("-Wextra");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted()
                .map(Path::toString)
                .forEach(command::add);
        }
        command.add(root.resolve("tools/parity/box2d_joint_set_probe.c").toString());
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
