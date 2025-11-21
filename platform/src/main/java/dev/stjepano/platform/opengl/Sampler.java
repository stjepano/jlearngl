package dev.stjepano.platform.opengl;

public interface Sampler extends GpuResource {

    /// Get current sampler parameters.
    SamplerParameters parameters();

    /// Configure sampler with new parameters.
    void configure(SamplerParameters parameters);

}
