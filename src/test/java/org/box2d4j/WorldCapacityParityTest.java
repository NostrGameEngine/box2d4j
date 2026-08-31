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

final class WorldCapacityParityTest {
    @Test
    void exhaustedWorldPoolReturnsNullAndRecyclesFreedSlot() throws Exception {
        assertEquals(runProbe(), runJava());
    }

    private static String runJava() {
        b2WorldId[] worlds = new b2WorldId[B2_MAX_WORLDS];
        for (int i = 0; i < worlds.length; ++i) {
            worlds[i] = b2CreateWorld(b2DefaultWorldDef());
        }
        b2WorldId overflow = b2CreateWorld(b2DefaultWorldDef());
        b2WorldId old = worlds[37];
        b2DestroyWorld(old);
        b2WorldId recycled = b2CreateWorld(b2DefaultWorldDef());
        int generationIncrement = (recycled.generation - old.generation) & 0xFFFF;

        String output = String.format("worldCapacity %d %d %d %d %d %d %d",
            overflow.index1, overflow.generation, b2World_IsValid(overflow) ? 1 : 0,
            recycled.index1, generationIncrement, b2World_IsValid(recycled) ? 1 : 0,
            b2World_IsValid(old) ? 1 : 0);
        for (int i = 0; i < worlds.length; ++i) {
            if (i != 37) {
                b2DestroyWorld(worlds[i]);
            }
        }
        b2DestroyWorld(recycled);
        return output;
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_world_capacity_probe");

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
        command.add(root.resolve("tools/parity/box2d_world_capacity_probe.c").toString());
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
