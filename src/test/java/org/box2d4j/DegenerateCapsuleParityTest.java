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

final class DegenerateCapsuleParityTest {
    @Test
    void shortCapsuleCreationFallsBackToCircle() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        b2ShapeId shortId = b2CreateCapsuleShape(bodyId, shapeDef,
            new b2Capsule(new b2Vec2(1.25f, -0.5f), new b2Vec2(1.25f, -0.5f), 0.7f));
        b2Circle circle = b2Shape_GetCircle(shortId);
        b2MassData mass = b2Shape_GetMassData(shortId);

        b2ShapeId capsuleId = b2CreateCapsuleShape(bodyId, shapeDef,
            new b2Capsule(new b2Vec2(-0.01f, 0.0f), new b2Vec2(0.01f, 0.0f), 0.2f));
        String output = String.format("degenerateCapsule %d %08x %08x %08x %08x %08x %d %d",
            b2Shape_GetType(shortId), Float.floatToRawIntBits(circle.center.x),
            Float.floatToRawIntBits(circle.center.y), Float.floatToRawIntBits(circle.radius),
            Float.floatToRawIntBits(mass.mass), Float.floatToRawIntBits(mass.rotationalInertia),
            b2Shape_GetType(capsuleId), b2Body_GetShapeCount(bodyId));
        b2DestroyWorld(worldId);
        return output;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_degenerate_capsule_probe");

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
        command.add(root.resolve("tools/parity/box2d_degenerate_capsule_probe.c").toString());
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
