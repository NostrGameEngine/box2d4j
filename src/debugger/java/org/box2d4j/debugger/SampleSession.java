package org.box2d4j.debugger;

import org.box2d4j.B2DebugHooks;
import org.box2d4j.b2Counters;
import org.box2d4j.b2WorldId;
import org.box2d4j.samples.SampleCatalog;
import org.box2d4j.samples.SampleRuntime;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.box2d4j.B2.*;

final class SampleSession implements AutoCloseable {
    private final SampleCatalog.Entry entry;
    private final SimulationClock clock = new SimulationClock();
    private final LatestFrameExchange frameExchange = new LatestFrameExchange();
    private final AtomicReference<b2WorldId> worldId = new AtomicReference<>();
    private final AtomicReference<Object> result = new AtomicReference<>();
    private final AtomicReference<b2WorldId> retainedWorld = new AtomicReference<>();
    private final AtomicLong version = new AtomicLong();
    private final AtomicLong bindingsVersion = new AtomicLong();
    private final AtomicInteger stepCount = new AtomicInteger();
    private final List<b2WorldId> worlds = new CopyOnWriteArrayList<>();
    private final List<SampleRuntime.Binding> bindings = new CopyOnWriteArrayList<>();
    private final List<Runnable> beforeStepActions = new CopyOnWriteArrayList<>();
    private final List<Runnable> afterStepActions = new CopyOnWriteArrayList<>();
    private final List<SampleRuntime.PointerHandler> pointerHandlers = new CopyOnWriteArrayList<>();
    private final List<SampleRuntime.CameraTarget> cameraTargets = new CopyOnWriteArrayList<>();
    private final ConcurrentLinkedQueue<PointerEvent> pointerEvents = new ConcurrentLinkedQueue<>();
    private final Thread thread;
    private final int workerCount;

    private volatile boolean cancelled;
    private volatile boolean finished;
    private volatile boolean finalFrame;
    private volatile Throwable error;
    private volatile float simulationTimeStep = 1.0f / 60.0f;
    private volatile int simulationSubStepCount = 4;
    private final MouseDragController mouseDragController = new MouseDragController();
    private volatile SampleRuntime.CameraPosition cameraPosition;
    private volatile Supplier<?> snapshotSupplier;
    private volatile Boolean drawBounds;
    private final AtomicReference<CaptureSettings> captureSettings = new AtomicReference<>(
        new CaptureSettings(new WorldDrawBatch.DrawOptions(), 0.1f));

    SampleSession(SampleCatalog.Entry entry, int workerCount) {
        this.entry = entry;
        this.workerCount = workerCount;
        thread = new Thread(this::runSample, "box2d4j-" + entry.type.getSimpleName());
        thread.setDaemon(true);
        thread.start();
    }

    SampleCatalog.Entry entry() {
        return entry;
    }

    b2WorldId worldId() {
        b2WorldId value = worldId.get();
        return value == null ? null : new b2WorldId(value.index1, value.generation);
    }

    long version() {
        return version.get();
    }

    int stepCount() {
        return stepCount.get();
    }

    int workerCount() {
        return workerCount;
    }

    boolean isPlaying() {
        return clock.isPlaying();
    }

    boolean isFinished() {
        return finished;
    }

    boolean isFinalFrame() {
        return finalFrame;
    }

    Throwable error() {
        return error;
    }

    Object result() {
        return result.get();
    }

    SampleRuntime.CameraPosition cameraPosition() {
        return cameraPosition;
    }

    Boolean drawBounds() {
        return drawBounds;
    }

    List<SampleRuntime.Binding> bindings() {
        return new ArrayList<>(bindings);
    }

    long bindingsVersion() {
        long value = bindingsVersion.get();
        for (int i = 0, count = bindings.size(); i < count; ++i) {
            value += bindings.get(i).revision();
        }
        return value;
    }

    void setBindingPressed(String id, boolean pressed) {
        for (SampleRuntime.Binding binding : bindings) {
            if (binding.id.equals(id) && binding.kind == SampleRuntime.Kind.HOLD
                && binding.isPressed() != pressed) {
                binding.setPressed(pressed);
                bindingsVersion.incrementAndGet();
            }
        }
    }

    void setKeyPressed(String keyName, boolean pressed) {
        for (SampleRuntime.Binding binding : bindings) {
            long previousRevision = binding.revision();
            binding.setKeyPressed(keyName, pressed);
            if (binding.revision() != previousRevision) {
                bindingsVersion.incrementAndGet();
            }
        }
    }

    void triggerBinding(String id) {
        for (SampleRuntime.Binding binding : bindings) {
            if (binding.id.equals(id) && binding.kind == SampleRuntime.Kind.ACTION) {
                binding.trigger();
                bindingsVersion.incrementAndGet();
            }
        }
    }

    void toggleBinding(String id) {
        for (SampleRuntime.Binding binding : bindings) {
            if (binding.id.equals(id) && binding.kind == SampleRuntime.Kind.TOGGLE) {
                binding.setPressed(!binding.isPressed());
                bindingsVersion.incrementAndGet();
            }
        }
    }

    void adjustBinding(String id, float delta) {
        for (SampleRuntime.Binding binding : bindings) {
            if (binding.id.equals(id) && binding.kind == SampleRuntime.Kind.FLOAT) {
                float previous = binding.getFloatValue();
                binding.setFloatValue(previous + delta);
                if (Float.compare(previous, binding.getFloatValue()) != 0) {
                    bindingsVersion.incrementAndGet();
                }
            } else if (binding.id.equals(id) && binding.kind == SampleRuntime.Kind.INTEGER) {
                int previous = binding.getIntValue();
                binding.setIntValue(previous + Math.round(delta));
                if (previous != binding.getIntValue()) {
                    bindingsVersion.incrementAndGet();
                }
            }
        }
    }

    void cycleBinding(String id, int delta) {
        for (SampleRuntime.Binding binding : bindings) {
            if (binding.id.equals(id) && binding.kind == SampleRuntime.Kind.CHOICE) {
                int count = binding.getOptions().length;
                binding.setIntValue((binding.getIntValue() + delta + count) % count);
                bindingsVersion.incrementAndGet();
            }
        }
    }

    void pointerDown(float worldX, float worldY, int button) {
        pointerEvents.add(new PointerEvent(PointerEvent.DOWN, worldX, worldY, button));
    }

    void pointerUp(float worldX, float worldY, int button) {
        pointerEvents.add(new PointerEvent(PointerEvent.UP, worldX, worldY, button));
    }

    void pointerMove(float worldX, float worldY) {
        pointerEvents.add(new PointerEvent(PointerEvent.MOVE, worldX, worldY, -1));
    }

    float targetHz() {
        return clock.targetHz();
    }

    void setTargetHz(float targetHz) {
        clock.setTargetHz(targetHz);
    }

    void togglePlaying() {
        clock.togglePlaying();
    }

    void setPlaying(boolean playing) {
        clock.setPlaying(playing);
    }

    void stepOnce() {
        if (!finished) {
            clock.stepOnce();
        }
    }

    void setCaptureSettings(WorldDrawBatch.DrawOptions options, float pointSize) {
        CaptureSettings current = captureSettings.get();
        if (current.options.sameAs(options) && Float.compare(current.pointSize, pointSize) == 0) {
            return;
        }
        captureSettings.set(new CaptureSettings(options, pointSize));
        clock.requestRefresh();
    }

    DebugFrame pollLatestFrame() {
        return frameExchange.pollLatest();
    }

    void releaseFrame(DebugFrame frame) {
        frameExchange.release(frame);
    }

    private void runSample() {
        DebuggerTaskScheduler taskScheduler = new DebuggerTaskScheduler(workerCount);
        SampleRuntime.Scope runtimeScope = SampleRuntime.install(new SampleRuntime.Context() {
            @Override
            public void register(SampleRuntime.Binding binding) {
                registerBinding(binding);
            }

            @Override
            public void registerBeforeStep(Runnable action) {
                beforeStepActions.add(action);
            }

            @Override
            public void registerAfterStep(Runnable action) {
                afterStepActions.add(action);
            }

            @Override
            public void registerPointerHandler(SampleRuntime.PointerHandler handler) {
                pointerHandlers.add(handler);
            }

            @Override
            public void registerCameraTarget(SampleRuntime.CameraTarget target) {
                cameraTargets.add(target);
            }

            @Override
            public void registerSnapshotSupplier(Supplier<?> supplier) {
                if (snapshotSupplier != null) {
                    throw new IllegalStateException("Only one interactive snapshot supplier is supported");
                }
                snapshotSupplier = supplier;
            }

            @Override
            public void setTargetHz(float nextTargetHz) {
                SampleSession.this.setTargetHz(nextTargetHz);
            }

            @Override
            public void setDrawBounds(boolean enabled) {
                drawBounds = enabled;
            }
        });
        B2DebugHooks.Scope scope = B2DebugHooks.install(new B2DebugHooks.Listener() {
            @Override
            public void beforeWorldCreated(org.box2d4j.b2WorldDef worldDef) {
                worldDef.setTaskScheduler(taskScheduler);
            }

            @Override
            public void onWorldCreated(b2WorldId createdWorldId) {
                worlds.add(createdWorldId);
            }

            @Override
            public void beforeWorldStep(b2WorldId steppedWorldId, float timeStep, int subStepCount) {
                awaitStepPermission(steppedWorldId);
                simulationTimeStep = timeStep;
                simulationSubStepCount = subStepCount;
                drainPointerEvents(steppedWorldId);
                for (SampleRuntime.Binding binding : bindings) {
                    binding.apply();
                }
                for (Runnable action : beforeStepActions) {
                    action.run();
                }
            }

            @Override
            public void onStepComplete(b2WorldId steppedWorldId) {
                for (Runnable action : afterStepActions) {
                    action.run();
                }
                cameraPosition = null;
                for (SampleRuntime.CameraTarget target : cameraTargets) {
                    SampleRuntime.CameraPosition next = target.get();
                    if (next != null) {
                        cameraPosition = next;
                    }
                }
                stepCount.incrementAndGet();
                publishFrame(steppedWorldId);
            }

            @Override
            public void beforeWorldDestroyed(b2WorldId destroyedWorldId) {
                if (!cancelled) {
                    b2WorldId retained = copy(destroyedWorldId);
                    retainedWorld.set(retained);
                }
            }

            @Override
            public boolean retainWorldOnDestroy(b2WorldId destroyedWorldId) {
                b2WorldId retained = retainedWorld.get();
                return !cancelled && retained != null && retained.index1 == destroyedWorldId.index1
                    && retained.generation == destroyedWorldId.generation;
            }
        });

        try {
            result.set(entry.type.getMethod("run").invoke(null));
            if (retainedWorld.get() != null) {
                continueRetainedWorld();
            } else {
                continueInteractiveSnapshot();
            }
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (!(cause instanceof SessionCancelled)) {
                error = cause;
            }
        } catch (SessionCancelled ignored) {
        } catch (ReflectiveOperationException | RuntimeException exception) {
            error = exception;
        } finally {
            scope.close();
            runtimeScope.close();
            retainedWorld.set(null);
            for (b2WorldId createdWorldId : worlds) {
                if (b2World_IsValid(createdWorldId)) {
                    b2DestroyWorld(createdWorldId);
                }
            }
            worldId.set(null);
            taskScheduler.close();
            finished = true;
            version.incrementAndGet();
        }
    }

    private void continueRetainedWorld() {
        while (!cancelled) {
            b2WorldId retained = retainedWorld.get();
            if (retained == null || !b2World_IsValid(retained)) {
                return;
            }
            b2World_Step(retained, simulationTimeStep, simulationSubStepCount);
        }
    }

    private void continueInteractiveSnapshot() {
        Supplier<?> supplier = snapshotSupplier;
        if (supplier == null) {
            return;
        }
        while (!cancelled) {
            try {
                int action = clock.awaitAction();
                if (action == SimulationClock.CANCELLED) {
                    return;
                }
                if (action == SimulationClock.REFRESH) {
                    version.incrementAndGet();
                    continue;
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                cancelled = true;
                return;
            }
            if (cancelled) {
                return;
            }
            drainSnapshotPointerEvents();
            for (SampleRuntime.Binding binding : bindings) {
                binding.apply();
            }
            for (Runnable action : beforeStepActions) {
                action.run();
            }
            for (Runnable action : afterStepActions) {
                action.run();
            }
            result.set(supplier.get());
            stepCount.incrementAndGet();
            version.incrementAndGet();
        }
    }

    private void registerBinding(SampleRuntime.Binding binding) {
        for (SampleRuntime.Binding existing : bindings) {
            if (existing.id.equals(binding.id)) {
                throw new IllegalArgumentException("Duplicate sample binding: " + binding.id);
            }
        }
        bindings.add(binding);
        bindingsVersion.incrementAndGet();
    }

    private void drainPointerEvents(b2WorldId steppedWorldId) {
        PointerEvent event;
        while ((event = pointerEvents.poll()) != null) {
            if (pointerHandlers.isEmpty()) {
                handleDefaultPointer(steppedWorldId, event);
                continue;
            }
            for (SampleRuntime.PointerHandler handler : pointerHandlers) {
                if (event.type == PointerEvent.DOWN) {
                    handler.down(event.worldX, event.worldY, event.button);
                } else if (event.type == PointerEvent.UP) {
                    handler.up(event.worldX, event.worldY, event.button);
                } else {
                    handler.move(event.worldX, event.worldY);
                }
            }
        }
    }

    private void drainSnapshotPointerEvents() {
        PointerEvent event;
        while ((event = pointerEvents.poll()) != null) {
            for (SampleRuntime.PointerHandler handler : pointerHandlers) {
                if (event.type == PointerEvent.DOWN) {
                    handler.down(event.worldX, event.worldY, event.button);
                } else if (event.type == PointerEvent.UP) {
                    handler.up(event.worldX, event.worldY, event.button);
                } else {
                    handler.move(event.worldX, event.worldY);
                }
            }
        }
    }

    private void handleDefaultPointer(b2WorldId steppedWorldId, PointerEvent event) {
        mouseDragController.handle(steppedWorldId, event.type, event.worldX, event.worldY, event.button);
    }

    private void awaitStepPermission(b2WorldId steppedWorldId) {
        try {
            int action;
            while ((action = clock.awaitAction()) == SimulationClock.REFRESH) {
                publishFrame(steppedWorldId);
            }
            if (action == SimulationClock.CANCELLED) {
                throw new SessionCancelled();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            cancelled = true;
            throw new SessionCancelled();
        }
    }

    private void publishFrame(b2WorldId nextWorldId) {
        if (cancelled || !b2World_IsValid(nextWorldId)) {
            return;
        }
        DebugFrame frame = frameExchange.acquireWritable();
        if (frame == null) {
            return;
        }
        try {
            CaptureSettings settings = captureSettings.get();
            frame.batch.captureInto(nextWorldId, settings.options, settings.pointSize);
            b2Counters counters = b2World_GetCounters(nextWorldId);
            frame.stepCount = stepCount.get();
            frame.bodyCount = counters.bodyCount;
            frame.shapeCount = counters.shapeCount;
            frame.contactCount = counters.contactCount;
            frame.jointCount = counters.jointCount;
            frame.awakeBodyCount = b2World_GetAwakeBodyCount(nextWorldId);
            frame.version = version.incrementAndGet();
            worldId.set(copy(nextWorldId));
            frameExchange.publish(frame);
        } catch (RuntimeException | Error exception) {
            frameExchange.release(frame);
            throw exception;
        }
    }

    private static b2WorldId copy(b2WorldId worldId) {
        return new b2WorldId(worldId.index1, worldId.generation);
    }

    @Override
    public void close() {
        cancelled = true;
        clock.cancel();
        thread.interrupt();
        try {
            thread.join(2000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class SessionCancelled extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private static final class CaptureSettings {
        final WorldDrawBatch.DrawOptions options;
        final float pointSize;

        CaptureSettings(WorldDrawBatch.DrawOptions options, float pointSize) {
            this.options = new WorldDrawBatch.DrawOptions(options);
            this.pointSize = pointSize;
        }
    }

    private static final class PointerEvent {
        static final int DOWN = 0;
        static final int UP = 1;
        static final int MOVE = 2;

        final int type;
        final float worldX;
        final float worldY;
        final int button;

        PointerEvent(int type, float worldX, float worldY, int button) {
            this.type = type;
            this.worldX = worldX;
            this.worldY = worldY;
            this.button = button;
        }
    }
}
