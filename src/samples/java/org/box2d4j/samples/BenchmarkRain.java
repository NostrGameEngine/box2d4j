package org.box2d4j.samples;

public final class BenchmarkRain {
    private BenchmarkRain() {
    }

    public static BenchmarkSampleResult run() {
        BenchmarkScenes.RainScene sample = BenchmarkScenes.createRain();
        return BenchmarkSampleResult.simulate("benchmarkRain", sample.scene, 33, sample::beforeStep, null);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
