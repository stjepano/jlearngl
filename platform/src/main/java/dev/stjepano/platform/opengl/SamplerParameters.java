package dev.stjepano.platform.opengl;

public record SamplerParameters(
        GLTextureMinFilter minFilter,
        GLTextureMagFilter magFilter,
        GLTextureWrap wrapS,
        GLTextureWrap wrapT,
        Float4 borderColor,
        float minLod,
        float maxLod,
        float lodBias,
        float maxAnisotropy,
        GLTextureCompareMode textureCompareMode,
        GLCompareFunc textureCompareFunc
) {

    public static final class Builder {

        // following values are based on GL4 specs (default values)

        private GLTextureMinFilter minFilter = GLTextureMinFilter.NEAREST_MIPMAP_LINEAR;
        private GLTextureMagFilter magFilter = GLTextureMagFilter.LINEAR;
        private GLTextureWrap wrapS = GLTextureWrap.REPEAT;
        private GLTextureWrap wrapT = GLTextureWrap.REPEAT;
        private Float4 borderColor = new Float4(0, 0, 0, 0);
        private float minLod = -1000;
        private float maxLod = 1000;
        private float lodBias = 0.0f;
        private float maxAnisotropy = 1.0f;
        private GLTextureCompareMode textureCompareMode = GLTextureCompareMode.NONE;
        private GLCompareFunc textureCompareFunc = GLCompareFunc.LEQUAL;

        /// Specify texture minification filter (when texture on screen is smaller than image).
        /// Default is NEAREST_MIPMAP_LINEAR.
        public Builder minFilter(GLTextureMinFilter filter) {
            this.minFilter = filter;
            return this;
        }

        /// Specify texture magnification filter (when texture on screen is larger than image).
        /// Default is LINEAR.
        public Builder magFilter(GLTextureMagFilter filter) {
            this.magFilter = filter;
            return this;
        }

        /// Specify texture behaviour when S coordinate wraps
        /// Default is REPEAT.
        public Builder wrapS(GLTextureWrap wrap) {
            this.wrapS = wrap;
            return this;
        }

        /// Specify texture behaviour when T coordinate wraps
        /// Default is REPEAT.
        public Builder wrapT(GLTextureWrap wrap) {
            this.wrapT = wrap;
            return this;
        }

        /// Specify border color used when wrap is configured to CLAMP_TO_BORDER.
        /// Default is (0, 0, 0, 0).
        public Builder borderColor(float r, float g, float b, float a) {
            this.borderColor = new Float4(r, g, b, a);
            return this;
        }

        /// Control lod levels and bias used during sampling.
        /// Default minLod = -1000, maxLod = 1000, bias = 0.0.
        public Builder lod(float minLod, float maxLod, float bias) {
            this.minLod = minLod;
            this.maxLod = maxLod;
            this.lodBias = bias;
            return this;
        }

        /// Control maximum anisotropy.
        /// Default is 1.0.
        public Builder maxAnisotropy(float anisotropy) {
            this.maxAnisotropy = anisotropy;
            return this;
        }

        /// Control texture compare mode and function.
        /// Default is mode = NONE, function = LEQUAL.
        public Builder textureCompare(GLTextureCompareMode mode, GLCompareFunc function) {
            this.textureCompareMode = mode;
            this.textureCompareFunc = function;
            return this;
        }

        /// Build the texture parameters.
        public SamplerParameters build() {
            return new SamplerParameters(
                    minFilter,
                    magFilter,
                    wrapS,
                    wrapT,
                    borderColor,
                    minLod,
                    maxLod,
                    lodBias,
                    maxAnisotropy,
                    textureCompareMode,
                    textureCompareFunc
            );
        }

    }

    /// Return a builder with default values (from OpenGL spec).
    public static Builder builder() {
        return new Builder();
    }

    /// Return a builder with values set to values from specified texture parameters.
    public static Builder builder(SamplerParameters parameters) {
        Builder builder = new Builder();
        builder.minFilter = parameters.minFilter();
        builder.magFilter = parameters.magFilter();
        builder.wrapS = parameters.wrapS();
        builder.wrapT = parameters.wrapT();
        builder.borderColor = parameters.borderColor();
        builder.minLod = parameters.minLod();
        builder.maxLod = parameters.maxLod();
        builder.lodBias = parameters.lodBias();
        builder.textureCompareMode = parameters.textureCompareMode();
        builder.textureCompareFunc = parameters.textureCompareFunc();
        return builder;
    }
}
