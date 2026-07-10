package org.box2d4j.samples;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/** Optional thread-local runtime used by graphical sample hosts. */
public final class SampleRuntime {
    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    private SampleRuntime() {
    }

    public enum Kind {
        ACTION,
        HOLD,
        TOGGLE,
        FLOAT,
        INTEGER,
        CHOICE
    }

    @FunctionalInterface
    public interface BooleanAction {
        void accept(boolean value);
    }

    @FunctionalInterface
    public interface FloatAction {
        void accept(float value);
    }

    @FunctionalInterface
    public interface IntAction {
        void accept(int value);
    }

    public interface PointerHandler {
        default void down(float worldX, float worldY, int button) {
        }

        default void up(float worldX, float worldY, int button) {
        }

        default void move(float worldX, float worldY) {
        }
    }

    @FunctionalInterface
    public interface CameraTarget {
        CameraPosition get();
    }

    public static final class CameraPosition {
        public final float x;
        public final float y;
        public final float initialPixelsPerMeter;

        public CameraPosition(float x, float y, float initialPixelsPerMeter) {
            this.x = x;
            this.y = y;
            this.initialPixelsPerMeter = initialPixelsPerMeter;
        }
    }

    @FunctionalInterface
    public interface Context {
        void register(Binding binding);

        default void registerBeforeStep(Runnable action) {
        }

        default void registerAfterStep(Runnable action) {
        }

        default void registerPointerHandler(PointerHandler handler) {
        }

        default void registerCameraTarget(CameraTarget target) {
        }

        default void registerSnapshotSupplier(Supplier<?> supplier) {
        }

        default void setTargetHz(float targetHz) {
        }

        default void setDrawBounds(boolean enabled) {
        }
    }

    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }

    public static Scope install(Context context) {
        Objects.requireNonNull(context, "context");
        Context previous = CONTEXT.get();
        CONTEXT.set(context);
        return () -> {
            if (previous == null) {
                CONTEXT.remove();
            } else {
                CONTEXT.set(previous);
            }
        };
    }

    public static boolean isActive() {
        return CONTEXT.get() != null;
    }

    public static void beforeStep(Runnable action) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.registerBeforeStep(Objects.requireNonNull(action, "action"));
        }
    }

    public static void afterStep(Runnable action) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.registerAfterStep(Objects.requireNonNull(action, "action"));
        }
    }

    public static void pointer(PointerHandler handler) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.registerPointerHandler(Objects.requireNonNull(handler, "handler"));
        }
    }

    public static void camera(CameraTarget target) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.registerCameraTarget(Objects.requireNonNull(target, "target"));
        }
    }

    public static void snapshot(Supplier<?> supplier) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.registerSnapshotSupplier(Objects.requireNonNull(supplier, "supplier"));
        }
    }

    public static void targetHz(float targetHz) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.setTargetHz(targetHz);
        }
    }

    public static void drawBounds(boolean enabled) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.setDrawBounds(enabled);
        }
    }

    public static void action(String id, String label, Runnable action) {
        action(id, label, null, action);
    }

    public static void action(String id, String label, String keyName, Runnable action) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.register(Binding.action(id, label, keyName, action));
        }
    }

    public static void hold(String id, String label, String keyName, BooleanAction action) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.register(Binding.hold(id, label, keyName, action));
        }
    }

    public static void toggle(String id, String label, boolean initialValue, BooleanAction action) {
        toggle(id, label, null, initialValue, action);
    }

    public static void toggle(String id, String label, String keyName, boolean initialValue, BooleanAction action) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.register(Binding.toggle(id, label, keyName, initialValue, action));
        }
    }

    public static Binding slider(String id, String label, float initialValue, float minValue, float maxValue,
                                 float step, FloatAction action) {
        return slider(id, label, initialValue, minValue, maxValue, step, null, null, action);
    }

    public static Binding slider(String id, String label, float initialValue, float minValue, float maxValue,
                                 float step, String decreaseKeyName, String increaseKeyName, FloatAction action) {
        Context context = CONTEXT.get();
        if (context != null) {
            Binding binding = Binding.slider(id, label, initialValue, minValue, maxValue, step,
                decreaseKeyName, increaseKeyName, action);
            context.register(binding);
            return binding;
        }
        return null;
    }

    public static void integer(String id, String label, int initialValue, int minValue, int maxValue,
                               int step, IntAction action) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.register(Binding.integer(id, label, initialValue, minValue, maxValue, step,
                Objects.requireNonNull(action, "action")));
        }
    }

    public static void choice(String id, String label, int initialIndex, String[] options, IntAction action) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.register(Binding.choice(id, label, initialIndex, options,
                Objects.requireNonNull(action, "action")));
        }
    }

    public static final class Binding {
        public final String id;
        public final String label;
        public final String keyName;
        public final String decreaseKeyName;
        public final String increaseKeyName;
        public final Kind kind;
        public final float minValue;
        public final float maxValue;
        public final float step;
        public final int minIntValue;
        public final int maxIntValue;
        public final int intStep;

        private final Runnable runnableAction;
        private final BooleanAction booleanAction;
        private final FloatAction floatAction;
        private final IntAction intAction;
        private final String[] options;
        private final AtomicLong revision = new AtomicLong();
        private final AtomicInteger pendingActions = new AtomicInteger();
        private volatile boolean pressed;
        private volatile boolean decreasePressed;
        private volatile boolean increasePressed;
        private volatile boolean keyPressed;
        private volatile float floatValue;
        private volatile int intValue;
        private long appliedRevision = -1L;

        private Binding(String id, String label, String keyName, String decreaseKeyName, String increaseKeyName,
                        Kind kind, boolean initialBooleanValue, float initialValue,
                        float minValue, float maxValue, float step, int initialIntValue, int minIntValue,
                        int maxIntValue, int intStep, String[] options, Runnable runnableAction,
                        BooleanAction booleanAction, FloatAction floatAction, IntAction intAction) {
            this.id = Objects.requireNonNull(id, "id");
            this.label = Objects.requireNonNull(label, "label");
            this.keyName = keyName;
            this.decreaseKeyName = decreaseKeyName;
            this.increaseKeyName = increaseKeyName;
            this.kind = kind;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.step = step;
            this.minIntValue = minIntValue;
            this.maxIntValue = maxIntValue;
            this.intStep = intStep;
            this.options = options == null ? new String[0] : options.clone();
            this.runnableAction = runnableAction;
            this.booleanAction = booleanAction;
            this.floatAction = floatAction;
            this.intAction = intAction;
            this.pressed = initialBooleanValue;
            this.floatValue = clampAndSnap(initialValue);
            this.intValue = clampInt(initialIntValue);
        }

        private static Binding action(String id, String label, String keyName, Runnable action) {
            return new Binding(id, label, keyName, null, null, Kind.ACTION, false,
                0.0f, 0.0f, 1.0f, 1.0f, 0, 0, 0, 1, null,
                Objects.requireNonNull(action, "action"), null, null, null);
        }

        private static Binding hold(String id, String label, String keyName, BooleanAction action) {
            return new Binding(id, label, keyName, null, null, Kind.HOLD, false,
                0.0f, 0.0f, 1.0f, 1.0f, 0, 0, 0, 1, null, null,
                Objects.requireNonNull(action, "action"), null, null);
        }

        private static Binding toggle(String id, String label, String keyName, boolean initialValue,
                                      BooleanAction action) {
            return new Binding(id, label, keyName, null, null, Kind.TOGGLE, initialValue,
                0.0f, 0.0f, 1.0f, 1.0f, 0, 0, 0, 1, null, null,
                Objects.requireNonNull(action, "action"), null, null);
        }

        private static Binding slider(String id, String label, float initialValue, float minValue,
                                      float maxValue, float step, String decreaseKeyName,
                                      String increaseKeyName, FloatAction action) {
            if (!(minValue < maxValue) || !(step > 0.0f)) {
                throw new IllegalArgumentException("Invalid slider range or step");
            }
            return new Binding(id, label, null, decreaseKeyName, increaseKeyName, Kind.FLOAT, false,
                initialValue, minValue, maxValue, step, 0, 0, 0, 1, null, null, null,
                Objects.requireNonNull(action, "action"), null);
        }

        private static Binding integer(String id, String label, int initialValue, int minValue, int maxValue,
                                       int step, IntAction action) {
            if (minValue >= maxValue || step <= 0) {
                throw new IllegalArgumentException("Invalid integer range or step");
            }
            return new Binding(id, label, null, null, null, Kind.INTEGER, false,
                0.0f, 0.0f, 1.0f, 1.0f, initialValue, minValue, maxValue, step, null, null, null, null,
                action);
        }

        private static Binding choice(String id, String label, int initialIndex, String[] options,
                                      IntAction action) {
            Objects.requireNonNull(options, "options");
            if (options.length == 0) {
                throw new IllegalArgumentException("A choice needs at least one option");
            }
            for (String option : options) {
                Objects.requireNonNull(option, "option");
            }
            return new Binding(id, label, null, null, null, Kind.CHOICE, false,
                0.0f, 0.0f, 1.0f, 1.0f, initialIndex, 0, options.length - 1, 1, options, null, null,
                null, action);
        }

        public boolean isPressed() {
            return pressed;
        }

        public void setPressed(boolean pressed) {
            if (this.pressed != pressed) {
                this.pressed = pressed;
                revision.incrementAndGet();
            }
        }

        public void trigger() {
            if (kind == Kind.ACTION) {
                pendingActions.incrementAndGet();
                revision.incrementAndGet();
            }
        }

        public void setKeyPressed(String keyName, boolean pressed) {
            if (this.keyName != null && this.keyName.equals(keyName)) {
                if (kind == Kind.ACTION && pressed && !this.pressed) {
                    trigger();
                }
                if (kind == Kind.TOGGLE && pressed && !keyPressed) {
                    setPressed(!this.pressed);
                }
                if (kind == Kind.TOGGLE) {
                    keyPressed = pressed;
                }
                if ((kind == Kind.ACTION || kind == Kind.HOLD) && this.pressed != pressed) {
                    this.pressed = pressed;
                    if (kind == Kind.HOLD) {
                        revision.incrementAndGet();
                    }
                }
            }
            if (keyName.equals(decreaseKeyName) && decreasePressed != pressed) {
                decreasePressed = pressed;
            }
            if (keyName.equals(increaseKeyName) && increasePressed != pressed) {
                increasePressed = pressed;
            }
        }

        public float getFloatValue() {
            return floatValue;
        }

        public void setFloatValue(float value) {
            float next = clampAndSnap(value);
            if (Float.compare(floatValue, next) != 0) {
                floatValue = next;
                revision.incrementAndGet();
            }
        }

        public int getIntValue() {
            return intValue;
        }

        public void setIntValue(int value) {
            int next = clampInt(value);
            if (intValue != next) {
                intValue = next;
                revision.incrementAndGet();
            }
        }

        public String getChoiceLabel() {
            return options.length == 0 ? "" : options[intValue];
        }

        public String[] getOptions() {
            return options.clone();
        }

        public long revision() {
            return revision.get();
        }

        public void apply() {
            if (kind == Kind.FLOAT && decreasePressed != increasePressed) {
                setFloatValue(floatValue + (increasePressed ? step : -step));
            }
            if (kind == Kind.INTEGER && decreasePressed != increasePressed) {
                setIntValue(intValue + (increasePressed ? intStep : -intStep));
            }
            if (kind == Kind.ACTION) {
                int count = pendingActions.getAndSet(0);
                for (int i = 0; i < count; ++i) {
                    runnableAction.run();
                }
                appliedRevision = revision.get();
                return;
            }
            long currentRevision = revision.get();
            if (appliedRevision == currentRevision) {
                return;
            }
            if (kind == Kind.HOLD || kind == Kind.TOGGLE) {
                booleanAction.accept(pressed);
            } else if (kind == Kind.FLOAT) {
                floatAction.accept(floatValue);
            } else {
                intAction.accept(intValue);
            }
            appliedRevision = currentRevision;
        }

        private float clampAndSnap(float value) {
            float clamped = Math.max(minValue, Math.min(maxValue, value));
            if (kind != Kind.FLOAT) {
                return clamped;
            }
            float snapped = minValue + Math.round((clamped - minValue) / step) * step;
            if (Math.abs(snapped) < 0.5f * step) {
                return 0.0f;
            }
            return Math.max(minValue, Math.min(maxValue, snapped));
        }

        private int clampInt(int value) {
            int clamped = Math.max(minIntValue, Math.min(maxIntValue, value));
            if (kind != Kind.INTEGER) {
                return clamped;
            }
            int snapped = minIntValue + Math.round((float) (clamped - minIntValue) / intStep) * intStep;
            return Math.max(minIntValue, Math.min(maxIntValue, snapped));
        }
    }
}
