package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PublicApiSurfaceTest {
    private static final Pattern EXPORTED_FUNCTION = Pattern.compile(
        "B2_API\\s+[^;{}]*?\\b(b2[A-Za-z0-9_]+)\\s*\\(", Pattern.DOTALL);

    @Test
    void everyUpstreamExportHasAJavaEntryPoint() throws Exception {
        Set<String> upstream = new TreeSet<>();
        Path includeDirectory = Path.of("vendor", "box2d", "include", "box2d");
        try (Stream<Path> headers = Files.list(includeDirectory)) {
            for (Path header : (Iterable<Path>) headers.filter(path -> path.toString().endsWith(".h"))::iterator) {
                String source = Files.readString(header, StandardCharsets.UTF_8);
                Matcher matcher = EXPORTED_FUNCTION.matcher(source);
                while (matcher.find()) {
                    upstream.add(matcher.group(1));
                }
            }
        }

        Set<String> javaApi = new TreeSet<>();
        for (Method method : B2.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())
                && method.getName().startsWith("b2")) {
                javaApi.add(method.getName());
            }
        }

        Set<String> missing = new TreeSet<>(upstream);
        missing.removeAll(javaApi);
        assertEquals(422, upstream.size(), "unexpected Box2D v3.1.1 exported API count");
        assertTrue(missing.isEmpty(), "missing Java entry points: " + missing);
    }
}
