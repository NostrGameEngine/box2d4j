package org.box2d4j.samples;

public final class BenchmarkLargePyramid {
    private BenchmarkLargePyramid() {
    }

    public static BenchmarkSampleResult run() {
        return BenchmarkSampleResult.simulate("benchmarkLargePyramid", BenchmarkScenes.createLargePyramid(), 2);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
