package org.box2d4j.samples;

public final class BenchmarkCompound {
    private BenchmarkCompound() {
    }

    public static BenchmarkSampleResult run() {
        return BenchmarkSampleResult.simulate("benchmarkCompound", BenchmarkScenes.createCompound(), 1);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
