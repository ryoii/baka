package cn.ryoii.baka.bench;

import cn.ryoii.baka.Baka;
import cn.ryoii.util.FileUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Tag("bench")
public class BenchWriteAndRead {

    @Benchmark
    public void launchBenchmark(BenchmarkState state) {
        Baka db = state.db;
        int num = state.random.nextInt();
        String v = String.valueOf(num);
        db.put(v, v);
        Assertions.assertEquals(v, db.get(v));
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        Path path;
        Baka db;
        Random random;

        @Setup(Level.Trial)
        public void setup() throws IOException {
            path = Files.createTempDirectory("test");
            db = Baka.open(path.toFile());
            random = new Random();
        }

        @TearDown
        public void tearDown() {
            FileUtil.delete(path.toFile());
        }
    }

    @Test
    public void bench() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchWriteAndRead.class.getName() + ".*")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(2)
                .threads(2)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();
        new Runner(opt).run();
    }
}
