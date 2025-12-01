package dev.stjepano.platform.opengl;

public record CullState(boolean enabled, GLFaceSide cullMode, GLFaceWinding frontFaceWinding) {

    public static final class Builder {
        private boolean enabled = false;
        private GLFaceSide faceSide = GLFaceSide.BACK;
        private GLFaceWinding faceWinding = GLFaceWinding.CCW;

        public Builder enable(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder cullMode(GLFaceSide faceSide) {
            this.faceSide = faceSide;
            return this;
        }

        public Builder frontFaceWinding(GLFaceWinding faceWinding) {
            this.faceWinding = faceWinding;
            return this;
        }

        public CullState build() {
            return new CullState(enabled, faceSide, faceWinding);
        }
    }

    /// Create a new builder for CullState
    public static Builder builder() {
        return new Builder();
    }

    /// Create a new builder initialized with the values from the given CullState
    public static Builder builder(CullState copy) {
        return new Builder()
                .enable(copy.enabled)
                .cullMode(copy.cullMode)
                .frontFaceWinding(copy.frontFaceWinding);
    }


}
