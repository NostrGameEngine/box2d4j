package org.box2d4j.samples;

public final class BenchmarkManyPyramids {
    private BenchmarkManyPyramids() {
    }

    public static BenchmarkSampleResult run() {
        return BenchmarkSampleResult.simulate("benchmarkManyPyramids", BenchmarkScenes.createManyPyramids(), 1);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
