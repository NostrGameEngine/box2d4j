package org.box2d4j.samples;

public final class BenchmarkKinematic {
    private BenchmarkKinematic() {
    }

    public static BenchmarkSampleResult run() {
        return BenchmarkSampleResult.simulate("benchmarkKinematic", BenchmarkScenes.createKinematic(), 1);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }
}
