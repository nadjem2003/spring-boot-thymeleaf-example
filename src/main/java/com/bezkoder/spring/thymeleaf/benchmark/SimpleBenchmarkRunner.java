package com.bezkoder.spring.thymeleaf.benchmark;

/**
 * Simple benchmark runner that manually executes the benchmark method
 * to demonstrate JMH functionality without complex classpath issues.
 */
public class SimpleBenchmarkRunner {
    public static void main(String[] args) {
        System.out.println("=== JMH Benchmark Demo ===");
        System.out.println("Running SimpleBenchmark.testMethod manually\n");

        SimpleBenchmark benchmark = new SimpleBenchmark();

        // Warmup
        System.out.println("Warming up...");
        for (int i = 0; i < 1000; i++) {
            benchmark.testMethod();
        }

        // Measurement
        System.out.println("Measuring performance...");
        long startTime = System.nanoTime();

        int iterations = 100000; // 100K iterations
        for (int i = 0; i < iterations; i++) {
            benchmark.testMethod();
        }

        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;
        double durationMs = durationNs / 1_000_000.0;
        double opsPerSecond = iterations / (durationMs / 1000.0);

        System.out.println("\n=== Results ===");
        System.out.printf("Iterations: %,d\n", iterations);
        System.out.printf("Duration: %.2f ms\n", durationMs);
        System.out.printf("Throughput: %,.0f ops/s\n", opsPerSecond);
        System.out.printf("Average time per operation: %.2f ns\n", (double) durationNs / iterations);

        System.out.println("\n=== Benchmark Method Details ===");
        System.out.println("Method: SimpleBenchmark.testMethod()");
        System.out.println("Logic: Sum numbers 0 to 999 in a loop");
        System.out.println("Result: " + benchmark.testMethod() + " (expected: 499500)");
    }
}
