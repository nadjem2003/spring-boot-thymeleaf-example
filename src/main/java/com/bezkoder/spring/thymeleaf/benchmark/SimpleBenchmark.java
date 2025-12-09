package com.bezkoder.spring.thymeleaf.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class SimpleBenchmark {

    @Benchmark
    public int testMethod() {
        int total = 0;
        for (int i = 0; i < 1000; i++) {
            total += i;
        }
        return total;
    }
}
