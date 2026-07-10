package org.box2d4j.samples;

public final class LargeWorld {
    private LargeWorld() {
    }

    public static BenchmarkSampleResult run() {
        BenchmarkScenes.LargeWorldScene sample = BenchmarkScenes.createLargeWorld();
        boolean interactive = SampleRuntime.isActive();
        sample.configureRuntime();
        return BenchmarkSampleResult.simulate("largeWorld", sample.scene, 4,
            interactive ? null : sample::beforeStep, null);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
