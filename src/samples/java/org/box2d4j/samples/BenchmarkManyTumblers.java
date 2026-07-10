package org.box2d4j.samples;

public final class BenchmarkManyTumblers {
    private BenchmarkManyTumblers() {
    }

    public static BenchmarkSampleResult run() {
        BenchmarkScenes.ManyTumblersScene sample = BenchmarkScenes.createManyTumblers();
        boolean interactive = SampleRuntime.isActive();
        int[] runtimeStep = {0};
        SampleRuntime.integer("benchmarkManyTumblers.rows", "Row Count", sample.rowCount, 1, 32, 1,
            sample::setRowCount);
        SampleRuntime.integer("benchmarkManyTumblers.columns", "Column Count", sample.columnCount, 1, 32, 1,
            sample::setColumnCount);
        SampleRuntime.slider("benchmarkManyTumblers.speed", "Speed", sample.angularSpeed, 0.0f, 100.0f, 1.0f,
            sample::setAngularSpeed);
        SampleRuntime.afterStep(() -> sample.afterStep(++runtimeStep[0]));
        return BenchmarkSampleResult.simulate("benchmarkManyTumblers", sample.scene, 8, null,
            interactive ? null : sample::afterStep);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
