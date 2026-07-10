package org.box2d4j.samples;

public final class BenchmarkCast {
    private BenchmarkCast() {
    }

    public static BenchmarkSampleResult run() {
        BenchmarkScenes.CastScene sample = BenchmarkScenes.createCast();
        boolean interactive = SampleRuntime.isActive();
        int[] runtimeStep = {0};
        SampleRuntime.choice("benchmarkCast.query", "Query", sample.queryType,
            new String[] {"Ray", "Circle", "Overlap"}, value -> {
                sample.queryType = value;
                sample.radius = value == 2 ? 5.0f : 0.1f;
            });
        SampleRuntime.integer("benchmarkCast.rows", "Rows", sample.rowCount, 0, 250, 1, value -> {
            sample.rowCount = value;
            sample.buildScene();
        });
        SampleRuntime.integer("benchmarkCast.columns", "Columns", sample.columnCount, 0, 250, 1, value -> {
            sample.columnCount = value;
            sample.buildScene();
        });
        SampleRuntime.slider("benchmarkCast.fill", "Fill", sample.fill, 0.0f, 1.0f, 0.01f, value -> {
            sample.fill = value;
            sample.buildScene();
        });
        SampleRuntime.slider("benchmarkCast.grid", "Grid", sample.grid, 0.5f, 2.0f, 0.01f, value -> {
            sample.grid = value;
            sample.buildScene();
        });
        SampleRuntime.slider("benchmarkCast.ratio", "Ratio", sample.ratio, 1.0f, 10.0f, 0.01f, value -> {
            sample.ratio = value;
            sample.buildScene();
        });
        SampleRuntime.toggle("benchmarkCast.topDown", "Top Down", sample.topDown, value -> {
            sample.topDown = value;
            sample.buildScene();
        });
        SampleRuntime.action("benchmarkCast.drawNext", "Draw Next",
            () -> sample.drawIndex = (sample.drawIndex + 1) % sample.origins.length);
        SampleRuntime.afterStep(() -> sample.afterStep(++runtimeStep[0]));
        return BenchmarkSampleResult.simulate("benchmarkCast", sample.scene, 1, null,
            interactive ? null : sample::afterStep,
            sample.hash::value);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
