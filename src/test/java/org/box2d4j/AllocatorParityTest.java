package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

final class AllocatorParityTest {
    @Test
    void allocatorHookMatchesUpstreamC() throws Exception {
        String[] lines = runProbe().split("\\R");
        AtomicInteger allocCount = new AtomicInteger();
        AtomicInteger lastSize = new AtomicInteger();
        AtomicInteger lastAlignment = new AtomicInteger();
        int baseByteCount = b2GetByteCount();

        try {
            b2SetAllocator((size, alignment) -> {
                allocCount.incrementAndGet();
                lastSize.set(size);
                lastAlignment.set(alignment);
                return ByteBuffer.allocate(size);
            });

            assertZeroLine(lines[0], allocCount.get(), b2GetByteCount() - baseByteCount);

            ByteBuffer buffer = b2Alloc(65);
            assertNotNull(buffer);
            assertAllocLine(lines[1], buffer.capacity(), lastSize.get(), lastAlignment.get(), allocCount.get(),
                b2GetByteCount() - baseByteCount);
            b2Free(buffer, 65);
            assertFreeLine(lines[2], 1, b2GetByteCount() - baseByteCount);

            buffer = b2Alloc(32);
            assertNotNull(buffer);
            assertAllocLine(lines[3], buffer.capacity(), lastSize.get(), lastAlignment.get(), allocCount.get(),
                b2GetByteCount() - baseByteCount);
            b2Free(buffer, 32);
            assertFreeLine(lines[4], 2, b2GetByteCount() - baseByteCount);

            AtomicInteger intFunctionSize = new AtomicInteger();
            IntFunction<ByteBuffer> intFunctionAllocator = size -> {
                intFunctionSize.set(size);
                return ByteBuffer.allocate(size);
            };
            b2SetAllocator(intFunctionAllocator);
            buffer = b2Alloc(33);
            assertEquals(64, intFunctionSize.get());
            assertEquals(64, buffer.capacity());
            b2Free(buffer, 33);
            assertEquals(baseByteCount, b2GetByteCount());
        } finally {
            b2SetAllocator(ByteBuffer::allocateDirect);
            assertEquals(baseByteCount, b2GetByteCount());
        }
    }

    private static void assertZeroLine(String line, int allocCount, int byteCount) {
        String[] parts = line.split("\\s+");
        assertEquals("zero", parts[0]);
        assertEquals(1, Integer.parseInt(parts[1]));
        assertNull(b2Alloc(0));
        assertEquals(Integer.parseInt(parts[2]), allocCount);
        assertEquals(Integer.parseInt(parts[3]), byteCount);
    }

    private static void assertAllocLine(String line, int capacity, int size, int alignment, int allocCount, int byteCount) {
        String[] parts = line.split("\\s+");
        assertEquals(1, Integer.parseInt(parts[1]));
        assertEquals(Integer.parseInt(parts[2]), capacity);
        assertEquals(Integer.parseInt(parts[2]), size);
        assertEquals(Integer.parseInt(parts[3]), alignment);
        assertEquals(Integer.parseInt(parts[4]), allocCount);
        assertEquals(Integer.parseInt(parts[5]), byteCount);
    }

    private static void assertFreeLine(String line, int freeCount, int byteCount) {
        String[] parts = line.split("\\s+");
        assertEquals(freeCount, Integer.parseInt(parts[1]));
        assertEquals(Integer.parseInt(parts[2]), byteCount);
    }

    private static String runProbe() throws Exception {
        Path root = new File(".").getCanonicalFile().toPath();
        Path outputDir = root.resolve("build/parity");
        Files.createDirectories(outputDir);
        Path probe = outputDir.resolve("box2d_allocator_probe");

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
        command.add(root.resolve("tools/parity/box2d_allocator_probe.c").toString());
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
