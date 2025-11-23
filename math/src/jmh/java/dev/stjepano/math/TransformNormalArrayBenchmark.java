package dev.stjepano.math;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class TransformNormalArrayBenchmark {

    private Transform transform;
    private final float[] normals = new float[10000*3]; // vertex data for 10000 positions
    private final float[] normalsCopy = new float[10000*3];

    @Setup
    public void setup() {
        transform = new Transform();
        transform.setPosition(-3, -4, 5);
        transform.setScale(2, 3 ,4);
        transform.setRotation(new Vec3(1, 1, 1).normalize(), (float) Math.toRadians(90.0f));

        for (int i = 0; i < normals.length/3; i++) {
            float x = (float) Math.random() + 0.2f;
            float y = (float) Math.random() + 0.2f;
            float z = (float) Math.random() + 0.2f;
            float oneOverLen = 1.0f/(float)Math.sqrt(x*x + y*y + z*z);
            normals[i*3] = x * oneOverLen;
            normals[i*3 + 1] = y * oneOverLen;
            normals[i*3 + 2] = z * oneOverLen;
        }
    }

    @Setup(Level.Invocation)
    public void resetNormals() {
        System.arraycopy(normals, 0, normalsCopy, 0, normals.length);
    }

    @Benchmark
    public void apply(Blackhole bh) {
        transform.transformPosition(normalsCopy, 10000, 0, 3);
        bh.consume(normalsCopy);
    }

}
