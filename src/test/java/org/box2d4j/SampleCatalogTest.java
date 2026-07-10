package org.box2d4j;

import org.box2d4j.samples.SampleCatalog;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class SampleCatalogTest {
    @Test
    void catalogContainsEveryHeadlessEntryPointOnce() throws Exception {
        assertEquals(111, SampleCatalog.entries().size());
        Set<Class<?>> types = new HashSet<>();
        for (SampleCatalog.Entry entry : SampleCatalog.entries()) {
            assertNotNull(entry.type.getMethod("run"));
            types.add(entry.type);
        }
        assertEquals(111, types.size());
    }
}
