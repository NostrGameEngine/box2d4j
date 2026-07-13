package org.box2d4j.debugger;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.FrameBuffer;
import org.box2d4j.b2Counters;
import org.box2d4j.b2WorldId;
import org.box2d4j.samples.SampleCatalog;
import org.box2d4j.samples.SampleRuntime;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.box2d4j.B2.b2World_GetAwakeBodyCount;
import static org.box2d4j.B2.b2World_GetCounters;
import static org.box2d4j.B2.b2World_IsValid;

public final class Box2D4JDebugger extends SimpleApplication {
    private static final float SIDEBAR_WIDTH = 360.0f;
    private static final float TOOLBAR_HEIGHT = 58.0f;
    private static final float STATUS_HEIGHT = 32.0f;
    private static final float LIST_ROW_HEIGHT = 27.0f;
    private static final int VISIBLE_SAMPLE_CONTROLS = 5;
    private static final String SAMPLE_KEY_PREFIX = "SampleKey.";
    private static final String[] SAMPLE_KEY_NAMES = {
        "SPACE", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
    };
    private static final int[] SAMPLE_KEY_CODES = {
        KeyInput.KEY_SPACE, KeyInput.KEY_A, KeyInput.KEY_B, KeyInput.KEY_C, KeyInput.KEY_D, KeyInput.KEY_E,
        KeyInput.KEY_F, KeyInput.KEY_G, KeyInput.KEY_H, KeyInput.KEY_I, KeyInput.KEY_J, KeyInput.KEY_K,
        KeyInput.KEY_L, KeyInput.KEY_M, KeyInput.KEY_N, KeyInput.KEY_O, KeyInput.KEY_P, KeyInput.KEY_Q,
        KeyInput.KEY_R, KeyInput.KEY_S, KeyInput.KEY_T, KeyInput.KEY_U, KeyInput.KEY_V, KeyInput.KEY_W,
        KeyInput.KEY_X, KeyInput.KEY_Y, KeyInput.KEY_Z, KeyInput.KEY_0, KeyInput.KEY_1, KeyInput.KEY_2,
        KeyInput.KEY_3, KeyInput.KEY_4, KeyInput.KEY_5, KeyInput.KEY_6, KeyInput.KEY_7, KeyInput.KEY_8,
        KeyInput.KEY_9
    };

    private static final ColorRGBA BACKGROUND = color(0x151616);
    private static final ColorRGBA PANEL = color(0x202221);
    private static final ColorRGBA PANEL_ALT = color(0x292D2A);
    private static final ColorRGBA PANEL_HOVER = color(0x334039);
    private static final ColorRGBA TEXT = color(0xE8F0F2);
    private static final ColorRGBA MUTED = color(0x8FA3AA);
    private static final ColorRGBA CYAN = color(0x41C7D9);
    private static final ColorRGBA GREEN = color(0x65D48A);
    private static final ColorRGBA AMBER = color(0xF0B44D);
    private static final ColorRGBA RED = color(0xED6A5E);

    private final Node uiNode = new Node("Debugger UI");
    private final List<HitTarget> hitTargets = new ArrayList<>();
    private final WorldDrawBatch.DrawOptions drawOptions = new WorldDrawBatch.DrawOptions();
    private final JmeDebugRenderer.ViewTransform view = new JmeDebugRenderer.ViewTransform();

    private BitmapFont font;
    private JmeDebugRenderer debugRenderer;
    private SampleSession session;
    private WorldDrawBatch lastBatch;
    private BitmapText statsText;
    private BitmapText stateText;
    private BitmapText tooltipText;
    private int selectedSample;
    private int listOffset;
    private int viewportWidth;
    private int viewportHeight;
    private long renderedVersion = -1L;
    private long renderedBindingsVersion = -1L;
    private boolean autoFitPending = true;
    private boolean renderDirty = true;
    private boolean uiDirty = true;
    private boolean panning;
    private boolean pointing;
    private float previousMouseX;
    private float previousMouseY;
    private int lastStepCount;
    private int lastBodyCount;
    private int lastShapeCount;
    private int lastContactCount;
    private int lastJointCount;
    private int lastAwakeBodyCount;
    private boolean resultSnapshot;
    private String screenshotPath;
    private CompletingScreenshotAppState screenshotState;
    private int screenshotCountdown = 8;
    private int initialSample;
    private int workerCount = DebuggerTaskScheduler.defaultWorkerCount();
    private HitTarget pressedTarget;
    private int controlOffset;

    public static void main(String[] args) {
        Box2D4JDebugger app = new Box2D4JDebugger();
        app.screenshotPath = System.getProperty("box2d4j.debugger.screenshot");
        app.initialSample = Integer.getInteger("box2d4j.debugger.sample", 0);
        app.workerCount = Math.max(1, Math.min(64,
            Integer.getInteger("box2d4j.debugger.workers", DebuggerTaskScheduler.defaultWorkerCount())));
        AppSettings settings = new AppSettings(true);
        settings.setTitle("box2d4j Debugger");
        settings.setResolution(1440, 900);
        settings.setResizable(true);
        settings.setVSync(true);
        settings.setSamples(0);
        settings.setAudioRenderer(null);
        app.setShowSettings(false);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayFps(false);
        setDisplayStatView(false);
        setPauseOnLostFocus(false);
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);
        viewPort.setBackgroundColor(BACKGROUND);
        font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        debugRenderer = new JmeDebugRenderer(assetManager);
        guiNode.attachChild(debugRenderer.fillGeometry());
        guiNode.attachChild(debugRenderer.lineGeometry());
        guiNode.attachChild(debugRenderer.labelNode());
        guiNode.attachChild(uiNode);
        configureInput();
        configureScreenshot();
        viewportWidth = cam.getWidth();
        viewportHeight = cam.getHeight();
        startSample(initialSample);
    }

    @Override
    public void simpleUpdate(float timePerFrame) {
        int width = cam.getWidth();
        int height = cam.getHeight();
        if (width != viewportWidth || height != viewportHeight) {
            viewportWidth = width;
            viewportHeight = height;
            uiDirty = true;
            renderDirty = true;
        }

        if (panning) {
            Vector2f cursor = inputManager.getCursorPosition();
            float dx = cursor.x - previousMouseX;
            float dy = cursor.y - previousMouseY;
            if (dx != 0.0f || dy != 0.0f) {
                view.centerX -= dx / view.pixelsPerMeter;
                view.centerY -= dy / view.pixelsPerMeter;
                previousMouseX = cursor.x;
                previousMouseY = cursor.y;
                renderDirty = true;
            }
        }
        if (pointing && session != null) {
            Vector2f cursor = inputManager.getCursorPosition();
            Vector2f world = screenToWorld(cursor);
            session.pointerMove(world.x, world.y);
        }

        if (session != null && session.bindingsVersion() != renderedBindingsVersion) {
            renderedBindingsVersion = session.bindingsVersion();
            ensureSelectionVisible();
            uiDirty = true;
        }

        if (uiDirty) {
            rebuildUi();
            uiDirty = false;
        }

        if (session != null) {
            Boolean sampleDrawBounds = session.drawBounds();
            if (sampleDrawBounds != null && drawOptions.bounds != sampleDrawBounds) {
                drawOptions.bounds = sampleDrawBounds;
                renderDirty = true;
                uiDirty = true;
            }
            long version = session.version();
            if (version != renderedVersion) {
                renderSession(version);
            } else if (renderDirty && lastBatch != null) {
                debugRenderer.upload(lastBatch, view, contentWidth(), viewportHeight);
                renderDirty = false;
            }
            updateStatus();
            updateCameraTarget();
            session.advance(timePerFrame);
        }
        updateScreenshot();
        updateTooltip();
    }

    @Override
    public void destroy() {
        if (session != null) {
            session.close();
            session = null;
        }
        super.destroy();
    }

    private void renderSession(long version) {
        b2WorldId worldId = session.worldId();
        if (worldId != null && b2World_IsValid(worldId)) {
            lastBatch = debugRenderer.capture(worldId, drawOptions, view);
            resultSnapshot = false;
            b2Counters counters = b2World_GetCounters(worldId);
            lastStepCount = session.stepCount();
            lastBodyCount = counters.bodyCount;
            lastShapeCount = counters.shapeCount;
            lastContactCount = counters.contactCount;
            lastJointCount = counters.jointCount;
            lastAwakeBodyCount = b2World_GetAwakeBodyCount(worldId);
            if (autoFitPending && lastBatch.bounds.isValid()) {
                view.fit(lastBatch.bounds, contentWidth(), viewportHeight - TOOLBAR_HEIGHT - STATUS_HEIGHT);
                autoFitPending = false;
            }
            debugRenderer.upload(lastBatch, view, contentWidth(), viewportHeight);
            renderDirty = false;
        } else if (session.result() != null) {
            WorldDrawBatch resultBatch = SampleResultDraw.capture(session.result());
            if (resultBatch != null) {
                lastBatch = resultBatch;
                resultSnapshot = true;
                if (autoFitPending && lastBatch.bounds.isValid()) {
                    view.fit(lastBatch.bounds, contentWidth(), viewportHeight - TOOLBAR_HEIGHT - STATUS_HEIGHT);
                    autoFitPending = false;
                }
                debugRenderer.upload(lastBatch, view, contentWidth(), viewportHeight);
                renderDirty = false;
            }
        } else {
            debugRenderer.clear();
            lastBatch = null;
        }
        renderedVersion = version;
    }

    private void updateStatus() {
        String state;
        if (session.error() != null) {
            state = "ERROR  " + safeMessage(session.error());
            stateText.setColor(RED);
        } else if (session.isFinished()) {
            state = "COMPLETE";
            stateText.setColor(GREEN);
        } else if (session.isFinalFrame()) {
            state = "FINAL FRAME";
            stateText.setColor(AMBER);
        } else if (session.isPlaying()) {
            state = "RUNNING";
            stateText.setColor(GREEN);
        } else {
            state = "PAUSED";
            stateText.setColor(AMBER);
        }
        stateText.setText(state);

        if (lastBatch != null && resultSnapshot) {
            statsText.setText("Result snapshot   No persistent b2World");
        } else if (lastBatch != null) {
            statsText.setText("Step " + lastStepCount
                + "   Bodies " + lastBodyCount
                + "   Shapes " + lastShapeCount
                + "   Contacts " + lastContactCount
                + "   Joints " + lastJointCount
                + "   Awake " + lastAwakeBodyCount
                + "   Workers " + session.workerCount());
        } else {
            statsText.setText("Step " + session.stepCount() + "   Waiting for drawable world");
        }
    }

    private void configureInput() {
        inputManager.addMapping("TogglePlay", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Step", new KeyTrigger(KeyInput.KEY_N));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("Fit", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping("PreviousSample", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("NextSample", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Primary", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Pan", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        inputManager.addMapping("ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        List<String> actionMappings = new ArrayList<>();
        actionMappings.add("TogglePlay");
        actionMappings.add("Step");
        actionMappings.add("Reset");
        actionMappings.add("Fit");
        actionMappings.add("PreviousSample");
        actionMappings.add("NextSample");
        actionMappings.add("Primary");
        actionMappings.add("Pan");
        for (int i = 0; i < SAMPLE_KEY_NAMES.length; ++i) {
            String mapping = SAMPLE_KEY_PREFIX + SAMPLE_KEY_NAMES[i];
            inputManager.addMapping(mapping, new KeyTrigger(SAMPLE_KEY_CODES[i]));
            actionMappings.add(mapping);
        }

        ActionListener actions = (name, pressed, timePerFrame) -> {
            if ("Primary".equals(name)) {
                handlePrimary(pressed);
            } else if ("Pan".equals(name)) {
                handlePan(pressed);
            } else if (name.startsWith(SAMPLE_KEY_PREFIX)) {
                if (session != null) {
                    session.setKeyPressed(sampleKeyName(name), pressed);
                }
            } else if (!pressed) {
                handleCommand(name);
            }
        };
        inputManager.addListener(actions, actionMappings.toArray(new String[0]));

        AnalogListener zoom = (name, value, timePerFrame) -> {
            Vector2f cursor = inputManager.getCursorPosition();
            if (cursor.x >= contentWidth()) {
                scrollSamples("ZoomIn".equals(name) ? -2 : 2);
                return;
            }
            view.zoom("ZoomIn".equals(name) ? 1.12f : 1.0f / 1.12f,
                cursor.x, cursor.y, contentWidth(), viewportHeight);
            renderDirty = true;
        };
        inputManager.addListener(zoom, "ZoomIn", "ZoomOut");
    }

    private void configureScreenshot() {
        if (screenshotPath == null || screenshotPath.isEmpty()) {
            return;
        }
        try {
            Path output = Paths.get(screenshotPath).toAbsolutePath();
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            String fileName = output.getFileName().toString();
            if (fileName.endsWith(".png")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            String directory = parent == null ? "" : parent.toString() + java.io.File.separator;
            screenshotState = new CompletingScreenshotAppState(directory, fileName);
            screenshotState.setIsNumbered(false);
            stateManager.attach(screenshotState);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot prepare debugger screenshot", exception);
        }
    }

    private void updateScreenshot() {
        if (screenshotState == null || lastBatch == null || !lastBatch.bounds.isValid()) {
            return;
        }
        if (screenshotCountdown > 0) {
            screenshotCountdown -= 1;
        } else if (screenshotCountdown == 0) {
            screenshotState.takeScreenshot();
            screenshotCountdown = -1;
            Thread shutdownThread = new Thread(() -> {
                try {
                    screenshotState.awaitWritten(10L, TimeUnit.SECONDS);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
                stop(false);
                try {
                    Thread.sleep(750L);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
                System.exit(0);
            }, "box2d4j-screenshot-shutdown");
            shutdownThread.setDaemon(true);
            shutdownThread.start();
        }
    }

    private void handlePrimary(boolean pressed) {
        Vector2f cursor = inputManager.getCursorPosition();
        if (pressed) {
            HitTarget target = targetAt(cursor.x, cursor.y);
            if (target != null) {
                pressedTarget = target;
                target.onPress.run();
                return;
            }
            if (cursor.x < contentWidth() && cursor.y < viewportHeight - TOOLBAR_HEIGHT
                && cursor.y > STATUS_HEIGHT) {
                pointing = true;
                Vector2f world = screenToWorld(cursor);
                if (session != null) {
                    session.pointerDown(world.x, world.y, MouseInput.BUTTON_LEFT);
                }
            }
        } else {
            if (pressedTarget != null) {
                pressedTarget.onRelease.run();
                pressedTarget = null;
            }
            if (pointing && session != null) {
                Vector2f world = screenToWorld(cursor);
                session.pointerUp(world.x, world.y, MouseInput.BUTTON_LEFT);
            }
            pointing = false;
        }
    }

    private void handlePan(boolean pressed) {
        panning = pressed;
        if (pressed) {
            Vector2f cursor = inputManager.getCursorPosition();
            previousMouseX = cursor.x;
            previousMouseY = cursor.y;
        }
    }

    private void handleCommand(String name) {
        if (session == null) {
            return;
        }
        switch (name) {
            case "TogglePlay":
                session.togglePlaying();
                uiDirty = true;
                break;
            case "Step":
                session.stepOnce();
                uiDirty = true;
                break;
            case "Reset":
                startSample(selectedSample);
                break;
            case "Fit":
                fitView();
                break;
            case "PreviousSample":
                selectRelative(-1);
                break;
            case "NextSample":
                selectRelative(1);
                break;
            default:
                break;
        }
    }

    private void startSample(int index) {
        if (session != null) {
            session.close();
        }
        selectedSample = Math.max(0, Math.min(SampleCatalog.entries().size() - 1, index));
        ensureSelectionVisible();
        session = new SampleSession(SampleCatalog.entries().get(selectedSample), workerCount);
        renderedVersion = -1L;
        renderedBindingsVersion = -1L;
        pressedTarget = null;
        controlOffset = 0;
        pointing = false;
        panning = false;
        lastBatch = null;
        resultSnapshot = false;
        autoFitPending = true;
        renderDirty = true;
        uiDirty = true;
        debugRenderer.clear();
    }

    private void selectRelative(int delta) {
        int count = SampleCatalog.entries().size();
        startSample((selectedSample + delta + count) % count);
    }

    private void fitView() {
        if (lastBatch != null && lastBatch.bounds.isValid()) {
            view.fit(lastBatch.bounds, contentWidth(), viewportHeight - TOOLBAR_HEIGHT - STATUS_HEIGHT);
            renderDirty = true;
        }
    }

    private void updateCameraTarget() {
        SampleRuntime.CameraPosition target = session.cameraPosition();
        if (target == null) {
            return;
        }
        if (autoFitPending) {
            view.pixelsPerMeter = Math.max(2.0f, Math.min(350.0f, target.initialPixelsPerMeter));
            autoFitPending = false;
        }
        if (Float.compare(view.centerX, target.x) != 0 || Float.compare(view.centerY, target.y) != 0) {
            view.centerX = target.x;
            view.centerY = target.y;
            renderDirty = true;
        }
    }

    private void rebuildUi() {
        uiNode.detachAllChildren();
        hitTargets.clear();
        float contentWidth = contentWidth();
        float toolbarY = viewportHeight - TOOLBAR_HEIGHT;
        addRect(0.0f, toolbarY, contentWidth, TOOLBAR_HEIGHT, PANEL, 10.0f);
        addRect(0.0f, 0.0f, contentWidth, STATUS_HEIGHT, PANEL, 10.0f);
        addRect(contentWidth, 0.0f, SIDEBAR_WIDTH, viewportHeight, PANEL, 10.0f);
        addRect(contentWidth, viewportHeight - 72.0f, SIDEBAR_WIDTH, 72.0f, PANEL_ALT, 11.0f);

        addButton(14.0f, toolbarY + 9.0f, 42.0f, 40.0f, session != null && session.isPlaying() ? "II" : ">",
            "Play / Pause", () -> handleCommand("TogglePlay"), CYAN);
        addButton(62.0f, toolbarY + 9.0f, 42.0f, 40.0f, ">|", "Single Step",
            () -> handleCommand("Step"), GREEN);
        addButton(110.0f, toolbarY + 9.0f, 42.0f, 40.0f, "R", "Reset Sample",
            () -> handleCommand("Reset"), AMBER);
        addButton(158.0f, toolbarY + 9.0f, 42.0f, 40.0f, "F", "Fit World",
            () -> handleCommand("Fit"), TEXT);
        addButton(214.0f, toolbarY + 9.0f, 32.0f, 40.0f, "-", "Slower", () -> changeSpeed(-15.0f), MUTED);
        addButton(314.0f, toolbarY + 9.0f, 32.0f, 40.0f, "+", "Faster", () -> changeSpeed(15.0f), MUTED);
        String hz = session == null ? "60 Hz" : Math.round(session.targetHz()) + " Hz";
        addText(hz, 255.0f, toolbarY + 19.0f, 15.0f, TEXT, 20.0f);

        SampleCatalog.Entry selected = SampleCatalog.entries().get(selectedSample);
        addText(selected.qualifiedName(), 368.0f, toolbarY + 20.0f, 17.0f, TEXT, 20.0f);
        tooltipText = addText("", Math.max(650.0f, contentWidth - 220.0f), toolbarY + 20.0f,
            14.0f, MUTED, 20.0f);
        stateText = addText("", 14.0f, 8.0f, 14.0f, GREEN, 20.0f);
        statsText = addText("", 132.0f, 8.0f, 14.0f, TEXT, 20.0f);

        addText("box2d4j", contentWidth + 18.0f, viewportHeight - 32.0f, 22.0f, TEXT, 20.0f);
        addText("SAMPLE DEBUGGER", contentWidth + 18.0f, viewportHeight - 54.0f, 12.0f, CYAN, 20.0f);

        float optionsTop = viewportHeight - 88.0f;
        addToggle(contentWidth + 16.0f, optionsTop - 28.0f, 158.0f, "Shapes", drawOptions.shapes,
            () -> toggleOption("shapes"));
        addToggle(contentWidth + 184.0f, optionsTop - 28.0f, 158.0f, "Joints", drawOptions.joints,
            () -> toggleOption("joints"));
        addToggle(contentWidth + 16.0f, optionsTop - 60.0f, 158.0f, "Contacts", drawOptions.contacts,
            () -> toggleOption("contacts"));
        addToggle(contentWidth + 184.0f, optionsTop - 60.0f, 158.0f, "AABBs", drawOptions.bounds,
            () -> toggleOption("bounds"));
        addToggle(contentWidth + 16.0f, optionsTop - 92.0f, 158.0f, "Joint Extras", drawOptions.jointExtras,
            () -> toggleOption("jointExtras"));
        addToggle(contentWidth + 184.0f, optionsTop - 92.0f, 158.0f, "Islands", drawOptions.islands,
            () -> toggleOption("islands"));
        addToggle(contentWidth + 16.0f, optionsTop - 124.0f, 326.0f, "Graph Colors", drawOptions.graphColors,
            () -> toggleOption("graph"));

        List<SampleRuntime.Binding> bindings = session == null ? new ArrayList<>() : session.bindings();
        if (!bindings.isEmpty()) {
            float controlsTop = optionsTop - 144.0f;
            int maxOffset = Math.max(0, bindings.size() - VISIBLE_SAMPLE_CONTROLS);
            controlOffset = Math.max(0, Math.min(maxOffset, controlOffset));
            int controlEnd = Math.min(bindings.size(), controlOffset + VISIBLE_SAMPLE_CONTROLS);
            String controlTitle = "SAMPLE CONTROLS  " + (controlOffset + 1) + "-" + controlEnd
                + " / " + bindings.size();
            addText(controlTitle, contentWidth + 16.0f, controlsTop - 18.0f, 11.0f, CYAN, 20.0f);
            if (bindings.size() > VISIBLE_SAMPLE_CONTROLS) {
                addButton(contentWidth + 286.0f, controlsTop - 27.0f, 25.0f, 22.0f, "<",
                    "Previous controls", () -> pageControls(-VISIBLE_SAMPLE_CONTROLS), MUTED);
                addButton(contentWidth + 317.0f, controlsTop - 27.0f, 25.0f, 22.0f, ">",
                    "Next controls", () -> pageControls(VISIBLE_SAMPLE_CONTROLS), MUTED);
            }
            float controlY = controlsTop - 58.0f;
            for (int i = controlOffset; i < controlEnd; ++i) {
                addSampleControl(contentWidth + 16.0f, controlY, 326.0f, bindings.get(i));
                controlY -= 40.0f;
            }
        }

        float listTop = sampleListTop();
        int visibleRows = Math.max(1, (int) ((listTop - 18.0f) / LIST_ROW_HEIGHT));
        int last = Math.min(SampleCatalog.entries().size(), listOffset + visibleRows);
        float y = listTop - LIST_ROW_HEIGHT;
        for (int index = listOffset; index < last; ++index) {
            SampleCatalog.Entry entry = SampleCatalog.entries().get(index);
            boolean selectedRow = index == selectedSample;
            addRect(contentWidth + 8.0f, y, SIDEBAR_WIDTH - 16.0f, LIST_ROW_HEIGHT - 2.0f,
                selectedRow ? PANEL_HOVER : (index % 2 == 0 ? PANEL : PANEL_ALT), 11.0f);
            if (selectedRow) {
                addRect(contentWidth + 8.0f, y, 4.0f, LIST_ROW_HEIGHT - 2.0f, GREEN, 12.0f);
            }
            addText(truncate(entry.category + " / " + entry.name, 42), contentWidth + 20.0f, y + 7.0f,
                13.5f, selectedRow ? TEXT : MUTED, 20.0f);
            final int sampleIndex = index;
            hitTargets.add(new HitTarget(contentWidth + 8.0f, y, SIDEBAR_WIDTH - 16.0f,
                LIST_ROW_HEIGHT - 2.0f, entry.qualifiedName(), () -> startSample(sampleIndex)));
            y -= LIST_ROW_HEIGHT;
        }
    }

    private void addSampleControl(float x, float y, float width, SampleRuntime.Binding binding) {
        if (binding.kind == SampleRuntime.Kind.ACTION) {
            addRect(x, y, width, 34.0f, PANEL_ALT, 11.0f);
            addRect(x, y, 4.0f, 34.0f, AMBER, 12.0f);
            addText(binding.label, x + 14.0f, y + 9.0f, 14.0f, TEXT, 20.0f);
            if (binding.keyName != null) {
                addText(binding.keyName, x + width - 58.0f, y + 10.0f, 11.0f, MUTED, 20.0f);
            }
            hitTargets.add(new HitTarget(x, y, width, 34.0f, binding.label,
                () -> session.triggerBinding(binding.id)));
            return;
        }
        if (binding.kind == SampleRuntime.Kind.HOLD) {
            boolean pressed = binding.isPressed();
            addRect(x, y, width, 34.0f, pressed ? PANEL_HOVER : PANEL_ALT, 11.0f);
            addRect(x, y, 4.0f, 34.0f, pressed ? GREEN : AMBER, 12.0f);
            addText(binding.label, x + 14.0f, y + 9.0f, 14.0f, TEXT, 20.0f);
            if (binding.keyName != null) {
                addText(binding.keyName, x + width - 58.0f, y + 10.0f, 11.0f, MUTED, 20.0f);
            }
            hitTargets.add(new HitTarget(x, y, width, 34.0f, binding.label,
                () -> session.setBindingPressed(binding.id, true),
                () -> session.setBindingPressed(binding.id, false)));
            return;
        }

        if (binding.kind == SampleRuntime.Kind.TOGGLE) {
            boolean enabled = binding.isPressed();
            addRect(x, y, width, 34.0f, enabled ? PANEL_HOVER : PANEL_ALT, 11.0f);
            addRect(x + 10.0f, y + 10.0f, 14.0f, 14.0f, enabled ? GREEN : MUTED, 12.0f);
            addText(binding.label, x + 34.0f, y + 9.0f, 14.0f, enabled ? TEXT : MUTED, 20.0f);
            if (binding.keyName != null) {
                addText(binding.keyName, x + width - 38.0f, y + 10.0f, 11.0f, MUTED, 20.0f);
            }
            hitTargets.add(new HitTarget(x, y, width, 34.0f, binding.label,
                () -> session.toggleBinding(binding.id)));
            return;
        }

        if (binding.kind == SampleRuntime.Kind.CHOICE) {
            addRect(x, y, width, 34.0f, PANEL_ALT, 11.0f);
            addText(binding.label, x + 12.0f, y + 9.0f, 13.5f, TEXT, 20.0f);
            addText(truncate(binding.getChoiceLabel(), 16), x + width - 168.0f, y + 9.0f, 13.0f, CYAN, 20.0f);
            addRect(x + width - 70.0f, y + 3.0f, 30.0f, 28.0f, PANEL_HOVER, 12.0f);
            addText("<", x + width - 59.0f, y + 8.0f, 16.0f, TEXT, 20.0f);
            addRect(x + width - 34.0f, y + 3.0f, 30.0f, 28.0f, PANEL_HOVER, 12.0f);
            addText(">", x + width - 25.0f, y + 8.0f, 16.0f, TEXT, 20.0f);
            hitTargets.add(new HitTarget(x + width - 70.0f, y + 3.0f, 30.0f, 28.0f,
                "Previous " + binding.label, () -> session.cycleBinding(binding.id, -1)));
            hitTargets.add(new HitTarget(x + width - 34.0f, y + 3.0f, 30.0f, 28.0f,
                "Next " + binding.label, () -> session.cycleBinding(binding.id, 1)));
            return;
        }

        addRect(x, y, width, 34.0f, PANEL_ALT, 11.0f);
        addText(binding.label, x + 12.0f, y + 9.0f, 13.5f, TEXT, 20.0f);
        if (binding.decreaseKeyName != null && binding.increaseKeyName != null) {
            addText(binding.decreaseKeyName + " / " + binding.increaseKeyName,
                x + width - 178.0f, y + 10.0f, 10.5f, MUTED, 20.0f);
        }
        String value = binding.kind == SampleRuntime.Kind.INTEGER
            ? Integer.toString(binding.getIntValue())
            : String.format(Locale.ROOT, "%.2f", binding.getFloatValue());
        addText(value, x + width - 118.0f, y + 9.0f, 13.5f, CYAN, 20.0f);
        addRect(x + width - 70.0f, y + 3.0f, 30.0f, 28.0f, PANEL_HOVER, 12.0f);
        addText("-", x + width - 59.0f, y + 8.0f, 16.0f, TEXT, 20.0f);
        addRect(x + width - 34.0f, y + 3.0f, 30.0f, 28.0f, PANEL_HOVER, 12.0f);
        addText("+", x + width - 25.0f, y + 8.0f, 16.0f, TEXT, 20.0f);
        hitTargets.add(new HitTarget(x + width - 70.0f, y + 3.0f, 30.0f, 28.0f,
            "Decrease " + binding.label, () -> session.adjustBinding(binding.id,
                binding.kind == SampleRuntime.Kind.INTEGER ? -binding.intStep : -binding.step)));
        hitTargets.add(new HitTarget(x + width - 34.0f, y + 3.0f, 30.0f, 28.0f,
            "Increase " + binding.label, () -> session.adjustBinding(binding.id,
                binding.kind == SampleRuntime.Kind.INTEGER ? binding.intStep : binding.step)));
    }

    private void addToggle(float x, float y, float width, String label, boolean enabled, Runnable action) {
        addRect(x, y, width, 27.0f, enabled ? PANEL_HOVER : PANEL_ALT, 11.0f);
        addRect(x + 8.0f, y + 7.0f, 12.0f, 12.0f, enabled ? GREEN : MUTED, 12.0f);
        addText(label, x + 28.0f, y + 7.0f, 13.5f, enabled ? TEXT : MUTED, 20.0f);
        hitTargets.add(new HitTarget(x, y, width, 27.0f, label, action));
    }

    private void addButton(float x, float y, float width, float height, String label, String tooltip,
                           Runnable action, ColorRGBA accent) {
        addRect(x, y, width, height, PANEL_ALT, 11.0f);
        addRect(x, y, 3.0f, height, accent, 12.0f);
        BitmapText text = addText(label, x + 0.5f * width, y + 11.0f, 17.0f, TEXT, 20.0f);
        text.setBox(new com.jme3.font.Rectangle(-0.5f * width, 0.0f, width, height));
        text.setAlignment(BitmapFont.Align.Center);
        hitTargets.add(new HitTarget(x, y, width, height, tooltip, action));
    }

    private BitmapText addText(String value, float x, float y, float size, ColorRGBA color, float z) {
        BitmapText text = new BitmapText(font);
        text.setText(value);
        text.setSize(size);
        text.setColor(color);
        text.setLocalTranslation(x, y + size, z);
        uiNode.attachChild(text);
        return text;
    }

    private void addRect(float x, float y, float width, float height, ColorRGBA color, float z) {
        Geometry geometry = new Geometry("panel", new Quad(Math.max(0.0f, width), Math.max(0.0f, height)));
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", color);
        geometry.setMaterial(material);
        geometry.setLocalTranslation(x, y, z);
        uiNode.attachChild(geometry);
    }

    private void toggleOption(String option) {
        switch (option) {
            case "shapes":
                drawOptions.shapes = !drawOptions.shapes;
                break;
            case "joints":
                drawOptions.joints = !drawOptions.joints;
                break;
            case "contacts":
                drawOptions.contacts = !drawOptions.contacts;
                break;
            case "bounds":
                drawOptions.bounds = !drawOptions.bounds;
                break;
            case "jointExtras":
                drawOptions.jointExtras = !drawOptions.jointExtras;
                drawOptions.joints = drawOptions.joints || drawOptions.jointExtras;
                break;
            case "islands":
                drawOptions.islands = !drawOptions.islands;
                break;
            case "graph":
                drawOptions.graphColors = !drawOptions.graphColors;
                drawOptions.contacts = drawOptions.contacts || drawOptions.graphColors;
                break;
            default:
                break;
        }
        renderedVersion = -1L;
        uiDirty = true;
    }

    private void changeSpeed(float delta) {
        if (session != null) {
            session.setTargetHz(session.targetHz() + delta);
            uiDirty = true;
        }
    }

    private void scrollSamples(int rows) {
        int visibleRows = visibleSampleRows();
        int maxOffset = Math.max(0, SampleCatalog.entries().size() - visibleRows);
        listOffset = Math.max(0, Math.min(maxOffset, listOffset + rows));
        uiDirty = true;
    }

    private void pageControls(int delta) {
        int bindingCount = session == null ? 0 : session.bindings().size();
        controlOffset = Math.max(0, Math.min(Math.max(0, bindingCount - VISIBLE_SAMPLE_CONTROLS),
            controlOffset + delta));
        uiDirty = true;
    }

    private void ensureSelectionVisible() {
        int visibleRows = visibleSampleRows();
        if (selectedSample < listOffset) {
            listOffset = selectedSample;
        } else if (selectedSample >= listOffset + visibleRows) {
            listOffset = selectedSample - visibleRows + 1;
        }
    }

    private float sampleListTop() {
        int bindingCount = session == null ? 0
            : Math.min(VISIBLE_SAMPLE_CONTROLS, session.bindings().size());
        float base = viewportHeight - 200.0f;
        return bindingCount == 0 ? base : base - 28.0f - 40.0f * bindingCount;
    }

    private int visibleSampleRows() {
        return Math.max(1, (int) ((sampleListTop() - 18.0f) / LIST_ROW_HEIGHT));
    }

    private void updateTooltip() {
        if (tooltipText == null) {
            return;
        }
        Vector2f cursor = inputManager.getCursorPosition();
        HitTarget target = targetAt(cursor.x, cursor.y);
        tooltipText.setText(target == null ? "" : target.tooltip);
    }

    private HitTarget targetAt(float x, float y) {
        for (int i = hitTargets.size() - 1; i >= 0; --i) {
            HitTarget target = hitTargets.get(i);
            if (target.contains(x, y)) {
                return target;
            }
        }
        return null;
    }

    private float contentWidth() {
        return Math.max(320.0f, viewportWidth - SIDEBAR_WIDTH);
    }

    private static String safeMessage(Throwable error) {
        String message = error.getMessage();
        return truncate(message == null ? error.getClass().getSimpleName() : message, 80);
    }

    private static String sampleKeyName(String mapping) {
        return mapping.substring(SAMPLE_KEY_PREFIX.length());
    }

    private Vector2f screenToWorld(Vector2f screen) {
        return new Vector2f(
            view.centerX + (screen.x - 0.5f * contentWidth()) / view.pixelsPerMeter,
            view.centerY + (screen.y - 0.5f * viewportHeight) / view.pixelsPerMeter);
    }

    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static ColorRGBA color(int rgb) {
        return new ColorRGBA(((rgb >>> 16) & 0xFF) / 255.0f, ((rgb >>> 8) & 0xFF) / 255.0f,
            (rgb & 0xFF) / 255.0f, 1.0f);
    }

    private static final class CompletingScreenshotAppState extends ScreenshotAppState {
        private final CountDownLatch written = new CountDownLatch(1);

        private CompletingScreenshotAppState(String filePath, String fileName) {
            super(filePath, fileName);
        }

        @Override
        protected void writeImageFile(File file) throws IOException {
            try {
                super.writeImageFile(file);
            } finally {
                written.countDown();
            }
        }

        @Override
        public void postFrame(FrameBuffer out) {
            GL11.glFinish();
            super.postFrame(out);
        }

        private boolean awaitWritten(long timeout, TimeUnit unit) throws InterruptedException {
            return written.await(timeout, unit);
        }
    }

    private static final class HitTarget {
        final float x;
        final float y;
        final float width;
        final float height;
        final String tooltip;
        final Runnable onPress;
        final Runnable onRelease;

        HitTarget(float x, float y, float width, float height, String tooltip, Runnable action) {
            this(x, y, width, height, tooltip, action, () -> {
            });
        }

        HitTarget(float x, float y, float width, float height, String tooltip, Runnable onPress,
                  Runnable onRelease) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.tooltip = tooltip;
            this.onPress = onPress;
            this.onRelease = onRelease;
        }

        boolean contains(float px, float py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }
    }
}
