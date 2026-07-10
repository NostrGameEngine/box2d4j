package org.box2d4j.samples;

public final class BenchmarkSleep {
    private BenchmarkSleep() {
    }

    public static BenchmarkSampleResult run() {
        BenchmarkScenes.SleepScene sample = BenchmarkScenes.createSleep();
        return BenchmarkSampleResult.simulate("benchmarkSleep", sample.scene, 2, sample::beforeStep, null);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
