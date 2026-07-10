package org.box2d4j.samples;

public final class BenchmarkSensor {
    private BenchmarkSensor() {
    }

    public static BenchmarkSampleResult run() {
        BenchmarkScenes.SensorScene sample = BenchmarkScenes.createSensor();
        return BenchmarkSampleResult.simulate("benchmarkSensor", sample.scene, 120, null, sample::afterStep,
            sample.hash::value);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
