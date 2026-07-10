package org.box2d4j.samples;

public final class BenchmarkCreateDestroy {
    private BenchmarkCreateDestroy() {
    }

    public static BenchmarkSampleResult run() {
        BenchmarkScenes.CreateDestroyScene sample = BenchmarkScenes.createCreateDestroy();
        return BenchmarkSampleResult.simulate("benchmarkCreateDestroy", sample.scene, 2, sample::beforeStep, null);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
