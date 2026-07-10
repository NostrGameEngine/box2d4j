package org.box2d4j.samples;

public final class BenchmarkJointGrid {
    private BenchmarkJointGrid() {
    }

    public static BenchmarkSampleResult run() {
        return BenchmarkSampleResult.simulate("benchmarkJointGrid", BenchmarkScenes.createJointGrid(), 2);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
