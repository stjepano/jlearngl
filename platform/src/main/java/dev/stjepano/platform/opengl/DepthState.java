package dev.stjepano.platform.opengl;

/// Represents the depth testing state in OpenGL.
/// @param enabled whether depth testing is enabled
/// @param function the depth comparison function
/// @param enableWrite whether depth writes are enabled
/// @param enableClamp whether depth clamping is enabled
/// @param nearPlane the near plane for depth range
/// @param farPlane the far plane for depth range
public record DepthState(boolean enabled, GLDepthFunc function, boolean enableWrite, boolean enableClamp, float nearPlane, float farPlane) {

    public static final class Builder {
        private boolean enabled = false;
        private GLDepthFunc function = GLDepthFunc.LESS;
        private boolean enableWrite = true;
        private boolean enableClamp = false;
        private float nearPlane = 0.0f;
        private float farPlane = 1.0f;

        public Builder enable(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder function(GLDepthFunc function) {
            this.function = function;
            return this;
        }

        public Builder enableWrite(boolean enableWrite) {
            this.enableWrite = enableWrite;
            return this;
        }

        public Builder enableClamp(boolean enableClamp) {
            this.enableClamp = enableClamp;
            return this;
        }

        public Builder nearPlane(float nearClamp) {
            this.nearPlane = nearClamp;
            return this;
        }

        public Builder farPlane(float farClamp) {
            this.farPlane = farClamp;
            return this;
        }

        public DepthState build() {
            return new DepthState(enabled, function, enableWrite, enableClamp, nearPlane, farPlane);
        }
    }

    /// Create a new DepthState builder.
    public static Builder builder() {
        return new Builder();
    }

    /// Create a new DepthState builder initialized with values from the given DepthState.
    public static Builder builder(DepthState depthState) {
        Builder builder = new Builder();
        builder.enabled = depthState.enabled;
        builder.function = depthState.function;
        builder.enableWrite = depthState.enableWrite;
        builder.enableClamp = depthState.enableClamp;
        builder.nearPlane = depthState.nearPlane;
        builder.farPlane = depthState.farPlane;
        return builder;
    }


}
