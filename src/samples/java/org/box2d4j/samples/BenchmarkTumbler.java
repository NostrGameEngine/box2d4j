package org.box2d4j.samples;

public final class BenchmarkTumbler {
    private BenchmarkTumbler() {
    }

    public static BenchmarkSampleResult run() {
        return BenchmarkSampleResult.simulate("benchmarkTumbler", BenchmarkScenes.createTumbler(), 2);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
