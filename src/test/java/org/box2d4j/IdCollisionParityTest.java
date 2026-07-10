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

final class IdCollisionParityTest {
    @Test
    void upstreamIdAndAabbSlicesMatchC() throws Exception {
        String[] expected = runProbe().split("\\R");
        List<String> actual = new ArrayList<>();

        int a = 0x01234567;
        b2WorldId worldId = b2LoadWorldId(a);
        actual.add(format("world %d %d %d", worldId.index1, worldId.generation, b2StoreWorldId(worldId)));

        long x = 0x0123456789ABCDEFL;
        b2BodyId bodyId = b2LoadBodyId(x);
        b2ShapeId shapeId = b2LoadShapeId(x);
        b2ChainId chainId = b2LoadChainId(x);
        b2JointId jointId = b2LoadJointId(x);
        actual.add(format("body %d %d %d %d", bodyId.index1, bodyId.world0, bodyId.generation, b2StoreBodyId(bodyId)));
        actual.add(format("shape %d %d %d %d", shapeId.index1, shapeId.world0, shapeId.generation, b2StoreShapeId(shapeId)));
        actual.add(format("chain %d %d %d %d", chainId.index1, chainId.world0, chainId.generation, b2StoreChainId(chainId)));
        actual.add(format("joint %d %d %d %d", jointId.index1, jointId.world0, jointId.generation, b2StoreJointId(jointId)));

        b2AABB box = new b2AABB(new b2Vec2(-1.0f, -1.0f), new b2Vec2(-2.0f, -2.0f));
        actual.add(format("aabbInvalid %d", b2IsValidAABB(box) ? 1 : 0));

        box.upperBound = new b2Vec2(1.0f, 1.0f);
        b2AABB other = new b2AABB(new b2Vec2(2.0f, 2.0f), new b2Vec2(4.0f, 4.0f));
        b2CastOutput output = b2AABB_RayCast(box, new b2Vec2(-2.0f, 0.0f), new b2Vec2(2.0f, 0.0f));
        actual.add(format("aabb %d %d %d %d %.9g %.9g %.9g",
            b2IsValidAABB(box) ? 1 : 0,
            b2AABB_Overlaps(box, other) ? 1 : 0,
            b2AABB_Contains(box, other) ? 1 : 0,
            output.hit ? 1 : 0,
            output.fraction,
            output.normal.x,
            output.normal.y));

        assertEquals(normalizeNumericTokens(String.join("\n", expected)), normalizeNumericTokens(String.join("\n", actual)));
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
        Path probe = outputDir.resolve("box2d_id_collision_probe");

        List<String> sources = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(root.resolve("vendor/box2d/src"))) {
            stream.filter(path -> path.getFileName().toString().endsWith(".c"))
                .sorted()
                .forEach(path -> sources.add(path.toString()));
        }

        List<String> command = new ArrayList<>();
        command.add("clang");
        command.add("-std=c17");
        command.add("-O2");
        command.add("-ffp-contract=off");
        command.add("-I" + root.resolve("vendor/box2d/include"));
        command.add("-I" + root.resolve("vendor/box2d/src"));
        command.addAll(sources);
        command.add(root.resolve("tools/parity/box2d_id_collision_probe.c").toString());
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
