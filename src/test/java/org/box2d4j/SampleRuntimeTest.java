package org.box2d4j;

import org.box2d4j.samples.SampleRuntime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SampleRuntimeTest {
    @Test
    void controlsAreScopedSnappedAndAppliedExplicitly() {
        List<SampleRuntime.Binding> bindings = new ArrayList<>();
        List<String> applied = new ArrayList<>();
        assertFalse(SampleRuntime.isActive());

        try (SampleRuntime.Scope ignored = SampleRuntime.install(bindings::add)) {
            assertTrue(SampleRuntime.isActive());
            SampleRuntime.hold("fire", "Fire", "SPACE", value -> applied.add("fire:" + value));
            SampleRuntime.slider("speed", "Speed", 5.0f, -10.0f, 10.0f, 0.5f,
                value -> applied.add("speed:" + value));
        }

        assertFalse(SampleRuntime.isActive());
        assertEquals(2, bindings.size());
        bindings.get(0).setPressed(true);
        bindings.get(0).apply();
        bindings.get(1).setFloatValue(7.26f);
        bindings.get(1).apply();
        assertEquals(List.of("fire:true", "speed:7.5"), applied);
    }

    @Test
    void toggleAndHeldSliderKeysApplyOnlyWhenTheirStateChanges() {
        List<SampleRuntime.Binding> bindings = new ArrayList<>();
        List<String> applied = new ArrayList<>();
        try (SampleRuntime.Scope ignored = SampleRuntime.install(bindings::add)) {
            SampleRuntime.toggle("motor", "Motor", true, value -> applied.add("motor:" + value));
            SampleRuntime.slider("speed", "Speed", 0.0f, -0.3f, 0.3f, 0.1f, "A", "D",
                value -> applied.add("speed:" + Math.round(10.0f * value)));
        }

        SampleRuntime.Binding motor = bindings.get(0);
        motor.apply();
        motor.apply();
        motor.setPressed(false);
        motor.apply();

        SampleRuntime.Binding speed = bindings.get(1);
        speed.setKeyPressed("D", true);
        speed.apply();
        speed.apply();
        speed.setKeyPressed("D", false);
        speed.apply();

        assertEquals(List.of("motor:true", "motor:false", "speed:1", "speed:2"), applied);
    }

    @Test
    void actionsIntegersChoicesAndStepHooksAreHostDriven() {
        List<SampleRuntime.Binding> bindings = new ArrayList<>();
        List<Runnable> beforeSteps = new ArrayList<>();
        List<Runnable> afterSteps = new ArrayList<>();
        List<String> applied = new ArrayList<>();
        AtomicReference<SampleRuntime.PointerHandler> pointer = new AtomicReference<>();
        SampleRuntime.Context context = new SampleRuntime.Context() {
            @Override
            public void register(SampleRuntime.Binding binding) {
                bindings.add(binding);
            }

            @Override
            public void registerBeforeStep(Runnable action) {
                beforeSteps.add(action);
            }

            @Override
            public void registerAfterStep(Runnable action) {
                afterSteps.add(action);
            }

            @Override
            public void registerPointerHandler(SampleRuntime.PointerHandler handler) {
                pointer.set(handler);
            }
        };

        try (SampleRuntime.Scope ignored = SampleRuntime.install(context)) {
            SampleRuntime.action("launch", "Launch", "B", () -> applied.add("launch"));
            SampleRuntime.integer("rows", "Rows", 4, 1, 10, 1,
                value -> applied.add("rows:" + value));
            SampleRuntime.choice("shape", "Shape", 0, new String[] {"Box", "Circle"},
                value -> applied.add("shape:" + value));
            SampleRuntime.beforeStep(() -> applied.add("before"));
            SampleRuntime.afterStep(() -> applied.add("after"));
            SampleRuntime.pointer(new SampleRuntime.PointerHandler() {
                @Override
                public void down(float worldX, float worldY, int button) {
                    applied.add("down:" + Math.round(worldX) + ":" + Math.round(worldY) + ":" + button);
                }
            });
        }

        bindings.get(0).setKeyPressed("B", true);
        bindings.get(0).setKeyPressed("B", false);
        bindings.get(0).apply();
        bindings.get(1).setIntValue(8);
        bindings.get(1).apply();
        bindings.get(2).setIntValue(1);
        bindings.get(2).apply();
        beforeSteps.get(0).run();
        afterSteps.get(0).run();
        pointer.get().down(2.0f, 3.0f, 0);

        assertEquals(List.of("launch", "rows:8", "shape:1", "before", "after", "down:2:3:0"), applied);
    }
}
