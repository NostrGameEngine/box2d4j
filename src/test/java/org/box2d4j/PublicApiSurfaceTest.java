package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PublicApiSurfaceTest {
    private static final Pattern EXPORTED_FUNCTION = Pattern.compile(
        "B2_API\\s+[^;{}]*?\\b(b2[A-Za-z0-9_]+)\\s*\\(", Pattern.DOTALL);
    private static final Pattern INLINE_FUNCTION = Pattern.compile(
        "B2_INLINE\\s+[^;{}]*?\\b(b2[A-Za-z0-9_]+)\\s*\\(", Pattern.DOTALL);
    private static final Pattern PUBLIC_STATIC_VALUE = Pattern.compile(
        "static\\s+const\\s+\\w+\\s+(b2_[A-Za-z0-9_]+)\\s*=");
    private static final Pattern PUBLIC_CALLBACK_TYPE = Pattern.compile(
        "typedef\\s+[^;{}()]+?\\b(b2[A-Za-z0-9_]+)\\s*\\([^;{}]*\\)\\s*;", Pattern.DOTALL);
    private static final Pattern PUBLIC_ENUM = Pattern.compile(
        "typedef\\s+enum\\s+(b2BodyType|b2ShapeType|b2JointType|b2TOIState|b2HexColor)\\s*\\{(.*?)\\}\\s*\\1",
        Pattern.DOTALL);
    private static final Pattern PUBLIC_STRUCT = Pattern.compile("typedef\\s+struct\\s+(b2[A-Za-z0-9_]+)");
    private static final Pattern PUBLIC_STRUCT_DEFINITION = Pattern.compile(
        "typedef\\s+struct\\s+(b2[A-Za-z0-9_]+)\\s*\\{(.*?)\\}\\s*\\1\\s*;", Pattern.DOTALL);
    private static final Pattern FUNCTION_POINTER_FIELD = Pattern.compile("\\(\\s*\\*\\s*([A-Za-z_]\\w*)\\s*\\)");
    private static final Pattern DATA_FIELD = Pattern.compile("([A-Za-z_]\\w*)\\s*(?:\\[[^]]*])?\\s*$");

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

    @Test
    void everyUpstreamInlineFunctionHasAJavaEntryPoint() throws Exception {
        Set<String> upstream = new TreeSet<>();
        Path includeDirectory = Path.of("vendor", "box2d", "include", "box2d");
        try (Stream<Path> headers = Files.list(includeDirectory)) {
            for (Path header : (Iterable<Path>) headers.filter(path -> path.toString().endsWith(".h"))::iterator) {
                Matcher matcher = INLINE_FUNCTION.matcher(Files.readString(header, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    upstream.add(matcher.group(1));
                }
            }
        }

        Set<String> javaApi = new TreeSet<>();
        for (Method method : B2.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
                javaApi.add(method.getName());
            }
        }

        Set<String> missing = new TreeSet<>(upstream);
        missing.removeAll(javaApi);
        assertEquals(73, upstream.size(), "unexpected Box2D v3.1.1 inline API count");
        assertTrue(missing.isEmpty(), "missing Java inline entry points: " + missing);
    }

    @Test
    void everyPublicStaticHeaderValueHasAJavaField() throws Exception {
        Set<String> upstream = new TreeSet<>();
        Path includeDirectory = Path.of("vendor", "box2d", "include", "box2d");
        try (Stream<Path> headers = Files.list(includeDirectory)) {
            for (Path header : (Iterable<Path>) headers.filter(path -> path.toString().endsWith(".h"))::iterator) {
                Matcher matcher = PUBLIC_STATIC_VALUE.matcher(Files.readString(header, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    upstream.add(matcher.group(1));
                }
            }
        }

        Set<String> missing = new TreeSet<>();
        for (String fieldName : upstream) {
            try {
                B2.class.getField(fieldName);
            } catch (NoSuchFieldException exception) {
                missing.add(fieldName);
            }
        }
        assertEquals(6, upstream.size(), "unexpected Box2D v3.1.1 public static value count");
        assertTrue(missing.isEmpty(), "missing Java public static values: " + missing);
    }

    @Test
    void everyPublicCallbackTypedefHasAJavaInterface() throws Exception {
        Set<String> upstream = new TreeSet<>();
        Path includeDirectory = Path.of("vendor", "box2d", "include", "box2d");
        try (Stream<Path> headers = Files.list(includeDirectory)) {
            for (Path header : (Iterable<Path>) headers.filter(path -> path.toString().endsWith(".h"))::iterator) {
                Matcher matcher = PUBLIC_CALLBACK_TYPE.matcher(Files.readString(header, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    upstream.add(matcher.group(1));
                }
            }
        }

        Set<String> missing = new TreeSet<>();
        for (String typeName : upstream) {
            try {
                Class<?> type = Class.forName("org.box2d4j." + typeName);
                if (!type.isInterface()) {
                    missing.add(typeName);
                }
            } catch (ClassNotFoundException exception) {
                missing.add(typeName);
            }
        }
        assertEquals(16, upstream.size(), "unexpected Box2D v3.1.1 public callback typedef count");
        assertTrue(missing.isEmpty(), "missing Java callback interfaces: " + missing);
    }

    @Test
    void everyPublicEnumValueHasAnExactJavaConstant() throws Exception {
        StringBuilder headers = new StringBuilder();
        Path includeDirectory = Path.of("vendor", "box2d", "include", "box2d");
        try (Stream<Path> paths = Files.list(includeDirectory)) {
            for (Path header : (Iterable<Path>) paths.filter(path -> path.toString().endsWith(".h"))::iterator) {
                headers.append(Files.readString(header, StandardCharsets.UTF_8)).append('\n');
            }
        }

        int enumCount = 0;
        Matcher enums = PUBLIC_ENUM.matcher(headers);
        while (enums.find()) {
            String body = enums.group(2).replaceAll("(?m)//.*$", "");
            int value = -1;
            for (String entry : body.split(",")) {
                Matcher enumerator = Pattern.compile("\\b(b2_[A-Za-z0-9_]+)\\b(?:\\s*=\\s*(0x[0-9A-Fa-f]+|-?\\d+))?")
                    .matcher(entry);
                if (!enumerator.find()) {
                    continue;
                }
                value = enumerator.group(2) == null ? value + 1 : Integer.decode(enumerator.group(2));
                int javaValue = B2.class.getField(enumerator.group(1)).getInt(null);
                assertEquals(value, javaValue, enumerator.group(1));
                enumCount += 1;
            }
        }
        assertEquals(168, enumCount, "unexpected Box2D v3.1.1 public enumerator count");
    }

    @Test
    void everyPublicStructHasAMatchingJavaType() throws Exception {
        Set<String> upstream = new TreeSet<>();
        Path includeDirectory = Path.of("vendor", "box2d", "include", "box2d");
        try (Stream<Path> headers = Files.list(includeDirectory)) {
            for (Path header : (Iterable<Path>) headers.filter(path -> path.toString().endsWith(".h"))::iterator) {
                Matcher matcher = PUBLIC_STRUCT.matcher(Files.readString(header, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    upstream.add(matcher.group(1));
                }
            }
        }

        Set<String> missing = new TreeSet<>();
        for (String typeName : upstream) {
            try {
                Class.forName("org.box2d4j." + typeName);
            } catch (ClassNotFoundException exception) {
                missing.add(typeName);
            }
        }
        assertEquals(71, upstream.size(), "unexpected Box2D v3.1.1 public struct count");
        assertTrue(missing.isEmpty(), "missing Java public types: " + missing);
    }

    @Test
    void everyPublicStructFieldIsRepresentedOrDocumentedAsPrivateInternalData() throws Exception {
        StringBuilder headers = new StringBuilder();
        Path includeDirectory = Path.of("vendor", "box2d", "include", "box2d");
        try (Stream<Path> paths = Files.list(includeDirectory)) {
            for (Path header : (Iterable<Path>) paths.filter(path -> path.toString().endsWith(".h"))::iterator) {
                headers.append(Files.readString(header, StandardCharsets.UTF_8)).append('\n');
            }
        }

        String source = headers.toString()
            .replaceAll("(?s)/\\*.*?\\*/", "")
            .replaceAll("(?m)//.*$", "");
        Map<String, Set<String>> missing = new TreeMap<>();
        Matcher structs = PUBLIC_STRUCT_DEFINITION.matcher(source);
        int structCount = 0;
        while (structs.find()) {
            String typeName = structs.group(1);
            Set<String> upstreamFields = parseStructFields(structs.group(2));
            Set<String> javaFields = new TreeSet<>();
            for (java.lang.reflect.Field field : Class.forName("org.box2d4j." + typeName).getFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    javaFields.add(field.getName());
                }
            }
            upstreamFields.removeAll(javaFields);
            if (!upstreamFields.isEmpty()) {
                missing.put(typeName, upstreamFields);
            }
            structCount += 1;
        }

        assertEquals(71, structCount, "unexpected Box2D v3.1.1 public struct definition count");
        assertEquals(Map.of("b2DynamicTree", Set.of("nodes", "leafIndices", "leafBoxes", "leafCenters", "binIndices")),
            missing, "unexpected missing public struct fields");
    }

    private static Set<String> parseStructFields(String body) {
        Set<String> fields = new TreeSet<>();
        for (String declaration : body.split(";")) {
            String normalized = declaration.replaceAll("(?m)^\\s*#.*$", " ").trim();
            if (normalized.isEmpty()) {
                continue;
            }
            Matcher functionPointer = FUNCTION_POINTER_FIELD.matcher(normalized);
            if (functionPointer.find()) {
                fields.add(functionPointer.group(1));
                continue;
            }
            Matcher dataField = DATA_FIELD.matcher(normalized);
            if (dataField.find()) {
                fields.add(dataField.group(1));
            }
        }
        return fields;
    }
}
