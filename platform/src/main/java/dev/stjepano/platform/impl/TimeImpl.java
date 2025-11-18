package dev.stjepano.platform.impl;

import dev.stjepano.platform.Time;

public final class TimeImpl implements Time {
    private final long initTimeNanos;
    private long lastUpdateTime;
    private long frameTimeNanos;
    private long frameIndex;

    TimeImpl() {
        initTimeNanos = System.nanoTime();
        lastUpdateTime = 0;
        frameIndex = 0;
    }

    @Override
    public long nanos() {
        return System.nanoTime() - initTimeNanos;
    }

    @Override
    public double seconds() {
        return ((double)nanos()) / 1_000_000_000.0;
    }

    @Override
    public long frameDeltaNanos() {
        return frameTimeNanos;
    }

    @Override
    public double frameDeltaSeconds() {
        return ((double) frameDeltaNanos()) / 1_000_000_000.0;
    }

    @Override
    public long frameIndex() {
        return frameIndex;
    }

    void update() {
        long now = nanos();
        frameTimeNanos = now - lastUpdateTime;
        lastUpdateTime = now;
        frameIndex++;
    }
}
