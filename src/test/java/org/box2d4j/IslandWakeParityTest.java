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

final class IslandWakeParityTest {
    @Test
    void contactsAndJointsWakeConnectedSleepingIslandsLikeUpstream() throws Exception {
        String[] expected = runProbe().split("\\R");

        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2();

        b2WorldId worldId = b2CreateWorld(worldDef);
        b2BodyId bodyA = makeBody(worldId, new b2Vec2(), false);
        b2BodyId bodyB = makeBody(worldId, new b2Vec2(), false);
        b2Body_SetAwake(bodyB, false);
        makeFilterJoint(worldId, bodyA, bodyB);
        assertEquals(expected[0], String.format("createJoint %d %d", bool(b2Body_IsAwake(bodyA)),
            bool(b2Body_IsAwake(bodyB))));
        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(worldDef);
        bodyA = makeBody(worldId, new b2Vec2(), false);
        bodyB = makeBody(worldId, new b2Vec2(), false);
        makeFilterJoint(worldId, bodyA, bodyB);
        b2Body_Disable(bodyA);
        b2Body_SetAwake(bodyB, false);
        b2Body_Enable(bodyA);
        assertEquals(expected[1], String.format("enableBody %d %d", bool(b2Body_IsAwake(bodyA)),
            bool(b2Body_IsAwake(bodyB))));
        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(worldDef);
        bodyA = makeBody(worldId, new b2Vec2(3.0f, 0.0f), true);
        bodyB = makeBody(worldId, new b2Vec2(), true);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2Body_SetAwake(bodyB, false);
        b2Body_SetTransform(bodyA, new b2Vec2(1.5f, 0.0f), b2Rot_identity);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        assertEquals(expected[2], String.format("contact %d %d %d", bool(b2Body_IsAwake(bodyA)),
            bool(b2Body_IsAwake(bodyB)), b2World_GetCounters(worldId).contactCount));
        b2DestroyWorld(worldId);
    }

    private static b2BodyId makeBody(b2WorldId worldId, b2Vec2 position, boolean withShape) {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = position;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        if (withShape) {
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(), 1.0f));
        }
        return bodyId;
    }

    private static b2JointId makeFilterJoint(b2WorldId worldId, b2BodyId bodyA, b2BodyId bodyB) {
        b2FilterJointDef jointDef = b2DefaultFilterJointDef();
        jointDef.bodyIdA = bodyA;
        jointDef.bodyIdB = bodyB;
        return b2CreateFilterJoint(worldId, jointDef);
    }

    private static int bool(boolean value) {
        return value ? 1 : 0;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_island_wake_probe");

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
        command.add(root.resolve("tools/parity/box2d_island_wake_probe.c").toString());
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
