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

final class DebugDiagnosticsParityTest {
    @Test
    void jointExtrasAndIslandBoundsMatchUpstreamC() throws Exception {
        List<String> lines = new ArrayList<>();
        b2DebugDraw draw = b2DefaultDebugDraw();
        draw.DrawPolygonFcn = (vertices, count, color) -> lines.add(format(
            "polygon %d %.9g %.9g %.9g %.9g %06x", count,
            vertices[0].x, vertices[0].y, vertices[2].x, vertices[2].y, color));
        draw.DrawCircleFcn = (center, radius, color) -> lines.add(format(
            "circle %.9g %.9g %.9g %06x", center.x, center.y, radius, color));
        draw.DrawSegmentFcn = (p1, p2, color) -> lines.add(format(
            "segment %.9g %.9g %.9g %.9g %06x", p1.x, p1.y, p2.x, p2.y, color));
        draw.DrawPointFcn = (point, size, color) -> lines.add(format(
            "point %.9g %.9g %.9g %06x", point.x, point.y, size, color));
        draw.DrawStringFcn = (point, text, color) -> lines.add(format(
            "string %.9g %.9g %s %06x", point.x, point.y, text, color));

        lines.add("joints");
        b2WorldId jointWorld = createJointWorld();
        draw.drawJoints = true;
        draw.drawJointExtras = true;
        b2World_Draw(jointWorld, draw);
        b2DestroyWorld(jointWorld);

        lines.add("islands");
        b2WorldId islandWorld = createIslandWorld();
        draw.drawJoints = false;
        draw.drawJointExtras = false;
        draw.drawIslands = true;
        b2World_Draw(islandWorld, draw);
        b2DestroyWorld(islandWorld);

        assertEquals(normalizeNumericTokens(runProbe()), normalizeNumericTokens(String.join("\n", lines)));
    }

    private static b2WorldId createJointWorld() {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = new b2Vec2(-1.0f, 0.0f);
        bodyDef.rotation = b2MakeRot(-0.15f);
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(2.0f, 1.0f);
        bodyDef.rotation = b2MakeRot(0.35f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);

        b2DistanceJointDef distance = b2DefaultDistanceJointDef();
        distance.bodyIdA = bodyA;
        distance.bodyIdB = bodyB;
        distance.localAnchorA = new b2Vec2(0.1f, 0.2f);
        distance.localAnchorB = new b2Vec2(-0.2f, 0.3f);
        distance.length = 3.0f;
        distance.hertz = 2.0f;
        distance.enableSpring = true;
        distance.enableLimit = true;
        distance.minLength = 1.0f;
        distance.maxLength = 4.0f;
        b2CreateDistanceJoint(worldId, distance);

        b2FilterJointDef filter = b2DefaultFilterJointDef();
        filter.bodyIdA = bodyA;
        filter.bodyIdB = bodyB;
        b2CreateFilterJoint(worldId, filter);

        b2MotorJointDef motor = b2DefaultMotorJointDef();
        motor.bodyIdA = bodyA;
        motor.bodyIdB = bodyB;
        b2CreateMotorJoint(worldId, motor);

        b2MouseJointDef mouse = b2DefaultMouseJointDef();
        mouse.bodyIdA = bodyA;
        mouse.bodyIdB = bodyB;
        mouse.target = new b2Vec2(1.0f, 2.0f);
        mouse.maxForce = 10.0f;
        b2CreateMouseJoint(worldId, mouse);

        b2PrismaticJointDef prismatic = b2DefaultPrismaticJointDef();
        prismatic.bodyIdA = bodyA;
        prismatic.bodyIdB = bodyB;
        prismatic.localAxisA = new b2Vec2(1.0f, 0.25f);
        prismatic.enableLimit = true;
        prismatic.lowerTranslation = -1.0f;
        prismatic.upperTranslation = 2.0f;
        b2CreatePrismaticJoint(worldId, prismatic);

        b2RevoluteJointDef revolute = b2DefaultRevoluteJointDef();
        revolute.bodyIdA = bodyA;
        revolute.bodyIdB = bodyB;
        revolute.referenceAngle = 0.2f;
        revolute.enableLimit = true;
        revolute.lowerAngle = -0.5f;
        revolute.upperAngle = 0.7f;
        revolute.drawSize = 0.4f;
        b2CreateRevoluteJoint(worldId, revolute);

        b2WeldJointDef weld = b2DefaultWeldJointDef();
        weld.bodyIdA = bodyA;
        weld.bodyIdB = bodyB;
        b2CreateWeldJoint(worldId, weld);

        b2WheelJointDef wheel = b2DefaultWheelJointDef();
        wheel.bodyIdA = bodyA;
        wheel.bodyIdB = bodyB;
        wheel.localAxisA = new b2Vec2(1.0f, 0.25f);
        wheel.enableLimit = true;
        wheel.lowerTranslation = -1.0f;
        wheel.upperTranslation = 2.0f;
        b2CreateWheelJoint(worldId, wheel);
        return worldId;
    }

    private static b2WorldId createIslandWorld() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(-1.0f, 0.0f);
        b2BodyId bodyA = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyA, b2DefaultShapeDef(), b2MakeSquare(0.5f));
        bodyDef.position = new b2Vec2(2.0f, 1.0f);
        b2BodyId bodyB = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodyB, b2DefaultShapeDef(), b2MakeSquare(0.75f));
        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = bodyA;
        jointDef.bodyIdB = bodyB;
        b2CreateRevoluteJoint(worldId, jointDef);
        return worldId;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_debug_diagnostics_probe");
        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-D_POSIX_C_SOURCE=200809L");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c")).sorted()
                .forEach(path -> command.add(path.toString()));
        }
        command.add(root.resolve("tools/parity/box2d_debug_diagnostics_probe.c").toString());
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

    private static String format(String pattern, Object... args) {
        return String.format(Locale.ROOT, pattern, args);
    }

    private static String normalizeNumericTokens(String value) {
        List<String> normalized = new ArrayList<>();
        for (String line : value.split("\\R")) {
            String[] parts = line.split("\\s+");
            for (int i = 0; i < parts.length; ++i) {
                if (parts[i].matches("[-+]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][-+]?\\d+)?")) {
                    parts[i] = Float.toString(Float.parseFloat(parts[i]));
                }
            }
            normalized.add(String.join(" ", parts));
        }
        return String.join("\n", normalized);
    }
}
