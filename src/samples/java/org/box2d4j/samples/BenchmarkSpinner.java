package org.box2d4j.samples;

public final class BenchmarkSpinner {
    private BenchmarkSpinner() {
    }

    public static BenchmarkSampleResult run() {
        return BenchmarkSampleResult.simulate("benchmarkSpinner", BenchmarkScenes.createSpinner(), 2);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
