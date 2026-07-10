package org.box2d4j.samples;

import org.box2d4j.b2AABB;
import org.box2d4j.b2DynamicTree;
import org.box2d4j.b2RayCastInput;
import org.box2d4j.b2TreeStats;
import org.box2d4j.b2Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.box2d4j.B2.*;

public final class DynamicTreeSample {
    public static final int INCREMENTAL = 0;
    public static final int FULL_REBUILD = 1;
    public static final int PARTIAL_REBUILD = 2;
    private static final int ROW_COUNT = 100;
    private static final int COLUMN_COUNT = 100;
    private static final int STEP_COUNT = 3;

    private DynamicTreeSample() {
    }

    public static Result run() {
        if (SampleRuntime.isActive()) {
            InteractiveState state = new InteractiveState();
            SampleRuntime.integer("dynamicTree.rows", "Rows", state.parameters.rows, 0, 250, 1,
                value -> state.update(() -> state.parameters.rows = value));
            SampleRuntime.integer("dynamicTree.columns", "Columns", state.parameters.columns, 0, 250, 1,
                value -> state.update(() -> state.parameters.columns = value));
            SampleRuntime.slider("dynamicTree.fill", "Fill", state.parameters.fill, 0.0f, 1.0f, 0.01f,
                value -> state.update(() -> state.parameters.fill = value));
            SampleRuntime.slider("dynamicTree.grid", "Grid", state.parameters.grid, 0.5f, 2.0f, 0.01f,
                value -> state.update(() -> state.parameters.grid = value));
            SampleRuntime.slider("dynamicTree.ratio", "Ratio", state.parameters.ratio, 1.0f, 10.0f, 0.01f,
                value -> state.update(() -> state.parameters.ratio = value));
            SampleRuntime.slider("dynamicTree.move", "Move", state.parameters.moveFraction, 0.0f, 1.0f, 0.01f,
                value -> state.update(() -> state.parameters.moveFraction = value));
            SampleRuntime.slider("dynamicTree.delta", "Delta", state.parameters.moveDelta, 0.0f, 1.0f, 0.01f,
                value -> state.update(() -> state.parameters.moveDelta = value));
            SampleRuntime.choice("dynamicTree.update", "Update", state.parameters.updateType,
                new String[] {"Incremental", "Full Rebuild", "Partial Rebuild"},
                value -> state.update(() -> state.parameters.updateType = value));
            SampleRuntime.choice("dynamicTree.dragTool", "Drag Tool", 0, new String[] {"Ray", "Query"},
                value -> state.queryDrag = value == 1);
            SampleRuntime.pointer(state);
            SampleRuntime.snapshot(state::snapshot);
            return state.snapshot();
        }
        VariantResult[] variants = {
            runVariant(INCREMENTAL), runVariant(FULL_REBUILD), runVariant(PARTIAL_REBUILD)
        };
        return new Result(variants);
    }

    public static VariantResult runVariant(int updateType) {
        Parameters parameters = new Parameters();
        parameters.updateType = updateType;
        return runVariant(parameters);
    }

    private static VariantResult runVariant(Parameters parameters) {
        RandomState random = new RandomState(12345);
        b2DynamicTree tree = b2DynamicTree_Create();
        List<Proxy> proxies = new ArrayList<>();
        b2Vec2 margin = new b2Vec2(0.1f, 0.1f);
        float y = -4.0f;
        for (int row = 0; row < parameters.rows; ++row) {
            float x = -40.0f;
            for (int column = 0; column < parameters.columns; ++column) {
                if (random.range(0.0f, 1.0f) <= parameters.fill) {
                    Proxy proxy = new Proxy();
                    proxy.position = new b2Vec2(x, y);
                    float ratio = random.range(1.0f, parameters.ratio);
                    float width = random.range(0.1f, 0.5f);
                    if (random.signed() > 0.0f) {
                        proxy.width = new b2Vec2(ratio * width, width);
                    } else {
                        proxy.width = new b2Vec2(width, ratio * width);
                    }
                    proxy.box = new b2AABB(new b2Vec2(x, y),
                        new b2Vec2(x + proxy.width.x, y + proxy.width.y));
                    proxy.fatBox = new b2AABB(b2Sub(proxy.box.lowerBound, margin),
                        b2Add(proxy.box.upperBound, margin));
                    proxy.proxyId = b2DynamicTree_CreateProxy(tree, proxy.fatBox,
                        B2_DEFAULT_CATEGORY_BITS, proxies.size());
                    proxies.add(proxy);
                }
                x += parameters.grid;
            }
            y += parameters.grid;
        }

        int[] movedCounts = new int[STEP_COUNT];
        int[] rebuildCounts = new int[STEP_COUNT];
        for (int step = 0; step < STEP_COUNT; ++step) {
            int movedCount = 0;
            for (Proxy proxy : proxies) {
                if (parameters.moveFraction > random.range(0.0f, 1.0f)) {
                    float dx = parameters.moveDelta * random.signed();
                    float dy = parameters.moveDelta * random.signed();
                    proxy.position.x += dx;
                    proxy.position.y += dy;
                    proxy.box.lowerBound.x = proxy.position.x + dx;
                    proxy.box.lowerBound.y = proxy.position.y + dy;
                    proxy.box.upperBound.x = proxy.position.x + dx + proxy.width.x;
                    proxy.box.upperBound.y = proxy.position.y + dy + proxy.width.y;
                    if (!b2AABB_Contains(proxy.fatBox, proxy.box)) {
                        proxy.fatBox.lowerBound = b2Sub(proxy.box.lowerBound, margin);
                        proxy.fatBox.upperBound = b2Add(proxy.box.upperBound, margin);
                        proxy.moved = true;
                        movedCount += 1;
                    } else {
                        proxy.moved = false;
                    }
                } else {
                    proxy.moved = false;
                }
            }
            movedCounts[step] = movedCount;

            if (parameters.updateType == INCREMENTAL) {
                for (Proxy proxy : proxies) {
                    if (proxy.moved) {
                        b2DynamicTree_MoveProxy(tree, proxy.proxyId, proxy.fatBox);
                    }
                }
            } else {
                for (Proxy proxy : proxies) {
                    if (proxy.moved) {
                        b2DynamicTree_EnlargeProxy(tree, proxy.proxyId, proxy.fatBox);
                    }
                }
                rebuildCounts[step] = b2DynamicTree_Rebuild(tree, parameters.updateType == FULL_REBUILD);
            }
            b2DynamicTree_Validate(tree);
        }

        List<Long> queryHits = new ArrayList<>();
        b2TreeStats queryStats = b2DynamicTree_Query(tree,
            new b2AABB(b2Min(parameters.queryStart, parameters.queryEnd),
                b2Max(parameters.queryStart, parameters.queryEnd)),
            B2_DEFAULT_MASK_BITS, (proxyId, userData) -> {
                queryHits.add(userData);
                return true;
            });
        Collections.sort(queryHits);

        List<Long> rayHits = new ArrayList<>();
        b2RayCastInput rayInput = new b2RayCastInput(parameters.rayStart,
            b2Sub(parameters.rayEnd, parameters.rayStart), 1.0f);
        b2TreeStats rayStats = b2DynamicTree_RayCast(tree, rayInput, B2_DEFAULT_MASK_BITS,
            (input, proxyId, userData) -> {
                rayHits.add(userData);
                return input.maxFraction;
            });
        Collections.sort(rayHits);

        b2AABB root = b2DynamicTree_GetRootBounds(tree);
        long stateHash = stateHash(proxies);
        ProxyState[] representatives = proxies.isEmpty() ? new ProxyState[0] : new ProxyState[] {
            proxyState(proxies.get(0)), proxyState(proxies.get(proxies.size() / 2)),
            proxyState(proxies.get(proxies.size() - 1))
        };
        VariantResult result = new VariantResult(parameters.updateType, proxies.size(), movedCounts, rebuildCounts,
            b2DynamicTree_GetHeight(tree), b2DynamicTree_GetAreaRatio(tree), b2DynamicTree_GetByteCount(tree),
            root.lowerBound.x, root.lowerBound.y, root.upperBound.x, root.upperBound.y, stateHash,
            queryStats.nodeVisits, queryStats.leafVisits, toIntArray(queryHits),
            rayStats.nodeVisits, rayStats.leafVisits, toIntArray(rayHits), representatives,
            b2Min(parameters.queryStart, parameters.queryEnd), b2Max(parameters.queryStart, parameters.queryEnd),
            parameters.rayStart, parameters.rayEnd);
        b2DynamicTree_Destroy(tree);
        return result;
    }

    private static final class Parameters {
        int rows = ROW_COUNT;
        int columns = COLUMN_COUNT;
        int updateType = INCREMENTAL;
        float fill = 0.25f;
        float grid = 1.0f;
        float ratio = 5.0f;
        float moveFraction = 0.05f;
        float moveDelta = 0.1f;
        b2Vec2 queryStart = new b2Vec2(0.0f, 0.0f);
        b2Vec2 queryEnd = new b2Vec2(25.0f, 25.0f);
        b2Vec2 rayStart = new b2Vec2(-40.0f, 10.0f);
        b2Vec2 rayEnd = new b2Vec2(60.0f, 10.0f);
    }

    private static final class InteractiveState implements SampleRuntime.PointerHandler {
        final Parameters parameters = new Parameters();
        Result cached;
        boolean dirty = true;
        boolean dragging;
        boolean queryDrag;

        Result snapshot() {
            if (dirty) {
                cached = new Result(new VariantResult[] {runVariant(parameters)});
                dirty = false;
            }
            return cached;
        }

        void update(Runnable action) {
            action.run();
            dirty = true;
        }

        @Override
        public void down(float worldX, float worldY, int button) {
            if (button == 0) {
                if (queryDrag) {
                    parameters.queryStart = new b2Vec2(worldX, worldY);
                    parameters.queryEnd = new b2Vec2(worldX, worldY);
                } else {
                    parameters.rayStart = new b2Vec2(worldX, worldY);
                    parameters.rayEnd = new b2Vec2(worldX, worldY);
                }
                dragging = true;
                dirty = true;
            }
        }

        @Override
        public void up(float worldX, float worldY, int button) {
            if (button == 0) {
                dragging = false;
            }
        }

        @Override
        public void move(float worldX, float worldY) {
            if (dragging) {
                if (queryDrag) {
                    parameters.queryEnd = new b2Vec2(worldX, worldY);
                } else {
                    parameters.rayEnd = new b2Vec2(worldX, worldY);
                }
                dirty = true;
            }
        }
    }

    private static long stateHash(List<Proxy> proxies) {
        long hash = 1469598103934665603L;
        for (Proxy proxy : proxies) {
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.position.x));
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.position.y));
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.width.x));
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.width.y));
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.box.lowerBound.x));
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.box.lowerBound.y));
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.box.upperBound.x));
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.box.upperBound.y));
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.fatBox.lowerBound.x));
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.fatBox.lowerBound.y));
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.fatBox.upperBound.x));
            hash = hashWord(hash, Float.floatToRawIntBits(proxy.fatBox.upperBound.y));
            hash = hashWord(hash, proxy.proxyId);
            hash = hashWord(hash, proxy.moved ? 1 : 0);
        }
        return hash;
    }

    private static long hashWord(long hash, int value) {
        return (hash ^ Integer.toUnsignedLong(value)) * 1099511628211L;
    }

    private static int[] toIntArray(List<Long> values) {
        int[] result = new int[values.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = values.get(i).intValue();
        }
        return result;
    }

    private static ProxyState proxyState(Proxy proxy) {
        return new ProxyState(proxy.proxyId, proxy.position.x, proxy.position.y, proxy.width.x, proxy.width.y,
            proxy.box.lowerBound.x, proxy.box.lowerBound.y, proxy.box.upperBound.x, proxy.box.upperBound.y,
            proxy.fatBox.lowerBound.x, proxy.fatBox.lowerBound.y, proxy.fatBox.upperBound.x,
            proxy.fatBox.upperBound.y, proxy.moved);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    private static final class Proxy {
        b2AABB box;
        b2AABB fatBox;
        b2Vec2 position;
        b2Vec2 width;
        int proxyId;
        boolean moved;
    }

    private static final class RandomState {
        private int seed;

        RandomState(int seed) {
            this.seed = seed;
        }

        float range(float lo, float hi) {
            float r = randomInt() / 32767.0f;
            return (hi - lo) * r + lo;
        }

        float signed() {
            return 2.0f * randomInt() / 32767.0f - 1.0f;
        }

        private int randomInt() {
            int x = seed;
            x ^= x << 13;
            x ^= x >>> 17;
            x ^= x << 5;
            seed = x;
            return x & 32767;
        }
    }

    public static final class Result {
        public final VariantResult[] variants;

        Result(VariantResult[] variants) {
            this.variants = variants;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder("dynamicTree ").append(variants.length);
            for (VariantResult variant : variants) {
                builder.append(variant.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class VariantResult {
        public final int updateType;
        public final int proxyCount;
        public final int[] movedCounts;
        public final int[] rebuildCounts;
        public final int height;
        public final float areaRatio;
        public final int byteCount;
        public final float lowerX;
        public final float lowerY;
        public final float upperX;
        public final float upperY;
        public final long stateHash;
        public final int queryNodeVisits;
        public final int queryLeafVisits;
        public final int[] queryHits;
        public final int rayNodeVisits;
        public final int rayLeafVisits;
        public final int[] rayHits;
        public final ProxyState[] representatives;
        public final b2Vec2 queryLower;
        public final b2Vec2 queryUpper;
        public final b2Vec2 rayStart;
        public final b2Vec2 rayEnd;

        VariantResult(int updateType, int proxyCount, int[] movedCounts, int[] rebuildCounts, int height,
                      float areaRatio, int byteCount, float lowerX, float lowerY, float upperX, float upperY,
                      long stateHash, int queryNodeVisits, int queryLeafVisits, int[] queryHits,
                      int rayNodeVisits, int rayLeafVisits, int[] rayHits, ProxyState[] representatives,
                      b2Vec2 queryLower, b2Vec2 queryUpper, b2Vec2 rayStart, b2Vec2 rayEnd) {
            this.updateType = updateType;
            this.proxyCount = proxyCount;
            this.movedCounts = movedCounts;
            this.rebuildCounts = rebuildCounts;
            this.height = height;
            this.areaRatio = areaRatio;
            this.byteCount = byteCount;
            this.lowerX = lowerX;
            this.lowerY = lowerY;
            this.upperX = upperX;
            this.upperY = upperY;
            this.stateHash = stateHash;
            this.queryNodeVisits = queryNodeVisits;
            this.queryLeafVisits = queryLeafVisits;
            this.queryHits = queryHits;
            this.rayNodeVisits = rayNodeVisits;
            this.rayLeafVisits = rayLeafVisits;
            this.rayHits = rayHits;
            this.representatives = representatives;
            this.queryLower = queryLower.copy();
            this.queryUpper = queryUpper.copy();
            this.rayStart = rayStart.copy();
            this.rayEnd = rayEnd.copy();
        }

        String toLinePart() {
            StringBuilder builder = new StringBuilder().append(' ').append(updateType).append(' ')
                .append(proxyCount).append(' ').append(movedCounts.length);
            for (int i = 0; i < movedCounts.length; ++i) {
                builder.append(' ').append(movedCounts[i]).append(' ').append(rebuildCounts[i]);
            }
            builder.append(' ').append(height).append(formatFloat(areaRatio)).append(' ').append(byteCount)
                .append(formatFloat(lowerX)).append(formatFloat(lowerY)).append(formatFloat(upperX))
                .append(formatFloat(upperY)).append(' ').append(Long.toUnsignedString(stateHash))
                .append(' ').append(queryNodeVisits).append(' ').append(queryLeafVisits)
                .append(' ').append(queryHits.length);
            for (int hit : queryHits) {
                builder.append(' ').append(hit);
            }
            builder.append(' ').append(rayNodeVisits).append(' ').append(rayLeafVisits)
                .append(' ').append(rayHits.length);
            for (int hit : rayHits) {
                builder.append(' ').append(hit);
            }
            builder.append(' ').append(representatives.length);
            for (ProxyState state : representatives) {
                builder.append(state.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class ProxyState {
        public final int proxyId;
        public final float positionX;
        public final float positionY;
        public final float widthX;
        public final float widthY;
        public final float boxLowerX;
        public final float boxLowerY;
        public final float boxUpperX;
        public final float boxUpperY;
        public final float fatLowerX;
        public final float fatLowerY;
        public final float fatUpperX;
        public final float fatUpperY;
        public final boolean moved;

        ProxyState(int proxyId, float positionX, float positionY, float widthX, float widthY,
                   float boxLowerX, float boxLowerY, float boxUpperX, float boxUpperY,
                   float fatLowerX, float fatLowerY, float fatUpperX, float fatUpperY, boolean moved) {
            this.proxyId = proxyId;
            this.positionX = positionX;
            this.positionY = positionY;
            this.widthX = widthX;
            this.widthY = widthY;
            this.boxLowerX = boxLowerX;
            this.boxLowerY = boxLowerY;
            this.boxUpperX = boxUpperX;
            this.boxUpperY = boxUpperY;
            this.fatLowerX = fatLowerX;
            this.fatLowerY = fatLowerY;
            this.fatUpperX = fatUpperX;
            this.fatUpperY = fatUpperY;
            this.moved = moved;
        }

        String toLinePart() {
            return " " + proxyId + formatFloat(positionX) + formatFloat(positionY) + formatFloat(widthX)
                + formatFloat(widthY) + formatFloat(boxLowerX) + formatFloat(boxLowerY)
                + formatFloat(boxUpperX) + formatFloat(boxUpperY) + formatFloat(fatLowerX)
                + formatFloat(fatLowerY) + formatFloat(fatUpperX) + formatFloat(fatUpperY)
                + " " + (moved ? 1 : 0);
        }
    }

    private static String formatFloat(float value) {
        return " " + String.format(Locale.ROOT, "%.9g", value);
    }
}
