package dev.stjepano.math;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class TransformPositionArrayBenchmark {

    private Transform transform;
    private final float[] positions = new float[10000*3]; // vertex data for 10000 positions
    private final float[] positionsCopy = new float[10000*3];

    @Setup
    public void setup() {
        transform = new Transform();
        transform.setPosition(-3, -4, 5);
        transform.setScale(2, 3 ,4);
        transform.setRotation(new Vec3(1, 1, 1).normalize(), (float) Math.toRadians(90.0f));

        for (int i = 0; i < positions.length; i++) {
            positions[i] = (float) Math.random();
        }
    }

    @Setup(Level.Invocation)
    public void resetPositions() {
        System.arraycopy(positions, 0, positionsCopy, 0, positions.length);
    }

    @Benchmark
    public void apply(Blackhole bh) {
        transform.transformPosition(positionsCopy, 10000, 0, 3);
        bh.consume(positionsCopy);
    }

}
