package dev.stjepano.platform.impl.opengl;

import dev.stjepano.platform.memory.StackAllocator;
import dev.stjepano.platform.opengl.*;

import java.lang.foreign.MemorySegment;
import java.util.List;

class Texture2DImpl implements Texture2D {
    /* package */ int glId;
    private final int width;
    private final int height;
    private final int mipLevels;
    private final GLTexture2DFormat internalFormat;
    private Texture2DParameters parameters;

    public Texture2DImpl(int glId, int width, int height, int mipLevels, GLTexture2DFormat internalFormat, Texture2DParameters parameters) {
        this.glId = glId;
        this.width = width;
        this.height = height;
        this.mipLevels = mipLevels;
        this.internalFormat = internalFormat;
        this.parameters = parameters;
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public int mipLevels() {
        return this.mipLevels;
    }

    @Override
    public GLTexture2DFormat internalFormat() {
        return this.internalFormat;
    }

    @Override
    public Texture2DParameters parameters() {
        return this.parameters;
    }

    @Override
    public void update(int mipLevel, int x, int y, int width, int height,
                GLPixelDataFormat format, GLPixelDataType type,
                int rowAlignment, int rowLength,
                MemorySegment pixels) {
        checkDeleted();
        validateUpdateParams(mipLevel, x, y, width, height, rowAlignment, rowLength);

        if (!JGL.jglTextureUpdateSubImage2D(glId, mipLevel, x, y, width, height, 
                format.glEnumValue(), type.glEnumValue(), rowAlignment, rowLength, pixels)) {
            throw new OpenGLException("Failed to update 2D texture!");
        }
    }


    @Override
    public void update(int mipLevel, int x, int y, int width, int height,
                GLPixelDataFormat format, GLPixelDataType type,
                int rowAlignment, int rowLength,
                Buffer pixelBuffer, long offset) {
        checkDeleted();
        validateUpdateParams(mipLevel, x, y, width, height, rowAlignment, rowLength);
        if (!(pixelBuffer instanceof BufferImpl pixelBufferImpl)) {
            throw new IllegalArgumentException("pixelBuffer not instance of BufferImpl!");
        }

        if (!JGL.jglTextureUpdateSubImage2DBuffer(glId, mipLevel, x, y, width, height,
                format.glEnumValue(), type.glEnumValue(), rowAlignment, rowLength, pixelBufferImpl.glId, offset)) {
            throw new OpenGLException("Failed to update 2D texture!");
        }

    }

    private void validateUpdateParams(int mipLevel, int x, int y, int width, int height, int rowAlignment, int rowLength) {
        if (mipLevel < 0 || mipLevel >= this.mipLevels) {
            throw new IllegalArgumentException("Invalid mip level. mipLevel = " + mipLevel);
        }
        if (rowLength < 0) {
            throw new IllegalArgumentException("Invalid row length. rowLength = " + rowLength);
        }
        if (!(rowAlignment == 1 || rowAlignment == 2 || rowAlignment == 4 || rowAlignment == 8)) {
            throw new IllegalArgumentException("Invalid row alignment " + rowAlignment + ". Valid values 1, 2, 4 or 8.");
        }
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Invalid coordinates. x = " + x + ", y = " + y);
        }
        int mipWidth = Math.max(1, this.width >> mipLevel);
        int mipHeight = Math.max(1, this.height >> mipLevel);
        if (x + width > mipWidth || y + height > mipHeight) {
            throw new IllegalArgumentException(String.format("Out of texture bounds at mip level %d (%dx%d). x + width = %d, y + height = %d",
                    mipLevel, mipWidth, mipHeight,
                    x + width, y + height
            ));
        }
    }

    @Override
    public void generateMipmaps() {
        checkDeleted();
        JGL.jglTextureGenerateMipmaps(glId);
    }

    @Override
    public void configure(Texture2DParameters parameters) {
        checkDeleted();
        List<OpenGLImpl.TextureParameter> delta = OpenGLImpl.textureParametersDelta(parameters, this.parameters);
        if (!delta.isEmpty()) {
            long deltaStreamSize = OpenGLImpl.calculateTextureParametersStreamSize(delta);
            try (StackAllocator stack = StackAllocator.push()) {
                MemorySegment stream = stack.allocate(deltaStreamSize, 8);
                OpenGLImpl.encodeTextureParametersStream(delta, stream);

                if (!JGL.jglTextureConfigure(glId, stream, stream.byteSize())) {
                    throw new OpenGLException("Failed to configure 2D texture.");
                }
            }
        }
        this.parameters = parameters;
    }

    @Override
    public void delete() {
        if (glId != 0) {
            JGL.jglDeleteTexture(glId);
            glId = 0;
        }
    }

    private void checkDeleted() {
        if (glId == 0) {
            throw new IllegalStateException("Texture is deleted.");
        }
    }
}
