package org.box2d4j.samples;

public final class BenchmarkSmash {
    private BenchmarkSmash() {
    }

    public static BenchmarkSampleResult run() {
        return BenchmarkSampleResult.simulate("benchmarkSmash", BenchmarkScenes.createSmash(), 2);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
