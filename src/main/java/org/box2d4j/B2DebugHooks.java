package org.box2d4j;

import java.util.Objects;

/** Thread-local lifecycle hooks used by interactive debuggers and tooling. */
public final class B2DebugHooks {
    private static final ThreadLocal<Listener> LISTENER = new ThreadLocal<>();

    private B2DebugHooks() {
    }

    public interface Listener {
        default void beforeWorldCreated(b2WorldDef worldDef) {
        }

        default void onWorldCreated(b2WorldId worldId) {
        }

        default void beforeWorldStep(b2WorldId worldId, float timeStep, int subStepCount) {
        }

        default void onStepComplete(b2WorldId worldId) {
        }

        default void beforeWorldDestroyed(b2WorldId worldId) {
        }

        default boolean retainWorldOnDestroy(b2WorldId worldId) {
            return false;
        }
    }

    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }

    public static Scope install(Listener listener) {
        Objects.requireNonNull(listener, "listener");
        Listener previous = LISTENER.get();
        LISTENER.set(listener);
        return () -> {
            if (previous == null) {
                LISTENER.remove();
            } else {
                LISTENER.set(previous);
            }
        };
    }

    static void worldCreated(b2WorldId worldId) {
        Listener listener = LISTENER.get();
        if (listener != null) {
            listener.onWorldCreated(copy(worldId));
        }
    }

    static void beforeWorldCreated(b2WorldDef worldDef) {
        Listener listener = LISTENER.get();
        if (listener != null) {
            listener.beforeWorldCreated(worldDef);
        }
    }

    static void stepComplete(b2WorldId worldId) {
        Listener listener = LISTENER.get();
        if (listener != null) {
            listener.onStepComplete(copy(worldId));
        }
    }

    static void beforeWorldStep(b2WorldId worldId, float timeStep, int subStepCount) {
        Listener listener = LISTENER.get();
        if (listener != null) {
            listener.beforeWorldStep(copy(worldId), timeStep, subStepCount);
        }
    }

    static boolean beforeWorldDestroyed(b2WorldId worldId) {
        Listener listener = LISTENER.get();
        if (listener == null) {
            return false;
        }
        b2WorldId copy = copy(worldId);
        listener.beforeWorldDestroyed(copy);
        return listener.retainWorldOnDestroy(copy);
    }

    private static b2WorldId copy(b2WorldId worldId) {
        return new b2WorldId(worldId.index1, worldId.generation);
    }
}
