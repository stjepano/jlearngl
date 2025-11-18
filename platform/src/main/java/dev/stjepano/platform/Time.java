package dev.stjepano.platform;

/// Platform managed time which can be used for animations and simple statistics.
@SuppressWarnings("unused")
public interface Time {
    /// Nanoseconds passed since platform initialization.
    long nanos();

    /// Seconds passed since platform initialization.
    double seconds();

    /// Current frame delta time in nanoseconds (this does not change until next frame).
    long frameDeltaNanos();

    /// Current frame delta time in seconds.
    double frameDeltaSeconds();

    /// The current frame index (first frame has index 0).
    long frameIndex();
}
