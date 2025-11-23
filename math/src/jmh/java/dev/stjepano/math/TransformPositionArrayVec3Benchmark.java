package dev.stjepano.math;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class TransformPositionArrayVec3Benchmark {

    private Transform transform;
    private final Vec3[] positions = new Vec3[10000]; // vertex data for 10000 positions
    private final Vec3[] positionsCopy = new Vec3[10000];

    @Setup
    public void setup() {
        transform = new Transform();
        transform.setPosition(-3, -4, 5);
        transform.setScale(2, 3 ,4);
        transform.setRotation(new Vec3(1, 1, 1).normalize(), (float) Math.toRadians(90.0f));

        for (int i = 0; i < positions.length; i++) {
            positions[i] = new Vec3();
            positions[i].set((float) Math.random(), (float) Math.random(), (float) Math.random());
            positionsCopy[i] = new Vec3();
        }
    }

    @Setup(Level.Invocation)
    public void resetPositions() {
        for (int i = 0; i < positions.length; i++) {
            positionsCopy[i].set(positions[i]);
        }
    }

    @Benchmark
    public void apply(Blackhole bh) {
        transform.transformPosition(positionsCopy);
        bh.consume(positionsCopy);
    }

}
