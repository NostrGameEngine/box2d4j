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

final class BitSetTableParityTest {
    private static final int COUNT = 169;
    private static final int SET_SPAN = 317;
    private static final int ITEM_COUNT = (SET_SPAN * SET_SPAN - SET_SPAN) / 2;

    @Test
    void upstreamBitSetAndTableSlicesMatchC() throws Exception {
        String[] expected = runProbe().split("\\R");
        assertEquals(2, expected.length);
        assertEquals(normalizeNumericTokens(expected[0]), normalizeNumericTokens(runBitSet()));
        assertEquals(normalizeNumericTokens(expected[1]), normalizeNumericTokens(runTable()));
    }

    private static String runBitSet() {
        b2BitSet bitSet = b2CreateBitSet(COUNT);
        b2SetBitCountAndClear(bitSet, COUNT);
        boolean[] values = new boolean[COUNT];

        int i1 = 0;
        int i2 = 1;
        b2SetBit(bitSet, i1);
        values[i1] = true;

        while (i2 < COUNT) {
            b2SetBit(bitSet, i2);
            values[i2] = true;
            int next = i1 + i2;
            i1 = i2;
            i2 = next;
        }

        int mismatchCount = 0;
        int setCount = 0;
        int hash = B2_HASH_INIT;
        for (int i = 0; i < COUNT; ++i) {
            boolean value = b2GetBit(bitSet, i);
            if (value) {
                setCount += 1;
            }
            if (value != values[i]) {
                mismatchCount += 1;
            }
            hash = b2Hash(hash, new byte[] {(byte) (value ? 1 : 0)}, 1);
        }

        String line = format("bitset %d %d %d %d %d %d", bitSet.blockCapacity, bitSet.blockCount,
            b2GetBitSetBytes(bitSet), setCount, hash, mismatchCount);
        b2DestroyBitSet(bitSet);
        return line;
    }

    private static String runTable() {
        int power = b2BoundingPowerOf2(3008);
        int nextPowerOf2 = b2RoundUpPowerOf2(3008);

        int n = SET_SPAN;
        int itemCount = ITEM_COUNT;
        boolean[] removed = new boolean[ITEM_COUNT];
        b2HashSet set = b2CreateSet(16);

        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                b2AddKey(set, B2_SHAPE_PAIR_KEY(i, j));
            }
        }

        int filledCount = set.count;
        int filledCapacity = set.capacity;
        int filledBytes = b2GetHashSetBytes(set);

        int k = 0;
        int removeCount = 0;
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                if (j == i + 1) {
                    b2RemoveKey(set, B2_SHAPE_PAIR_KEY(i, j));
                    removed[k++] = true;
                    removeCount += 1;
                } else {
                    removed[k++] = false;
                }
            }
        }

        int afterRemoveCount = set.count;
        int containsCount = 0;
        int removedObservedCount = 0;
        int missCount = 0;
        k = 0;
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                long key = B2_SHAPE_PAIR_KEY(j, i);
                boolean contains = b2ContainsKey(set, key);
                if (contains) {
                    containsCount += 1;
                }
                if (removed[k]) {
                    removedObservedCount += 1;
                }
                if (!contains && !removed[k]) {
                    missCount += 1;
                }
                k += 1;
            }
        }

        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                b2RemoveKey(set, B2_SHAPE_PAIR_KEY(i, j));
            }
        }

        String line = format("table %d %d %d %d %d %d %d %d %d %d %d %d", power, nextPowerOf2, itemCount,
            filledCount, filledCapacity, filledBytes, removeCount, afterRemoveCount, containsCount, removedObservedCount,
            missCount, set.count);
        b2DestroySet(set);
        return line;
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
        Path probe = outputDir.resolve("box2d_bitset_table_probe");

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
        command.add(root.resolve("tools/parity/box2d_bitset_table_probe.c").toString());
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
