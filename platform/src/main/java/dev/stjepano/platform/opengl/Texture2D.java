package dev.stjepano.platform.opengl;

import java.lang.foreign.MemorySegment;

/// Represents a GPU texture 2D.
///
/// Assume that texture is stored in GPU only memory with optimized format for whichever purpose you
/// are using texture for (driver takes care of this).
///
/// ### Example
/// #### Upload and generate mipmaps
///
/// ```java
/// MemorySegment pixelData = ...;
/// Texture2D t = gl.createTexture2D(Texture2D.calculateMipLevels(1024, 1024), GLTexture2DFormat.RGBA8, 1024, 1024);
/// t.update(0, 0, 0, 1024, 1024, GLPixelDataFormat.RGBA, GLPixelDataType.UNSIGNED_BYTE, pixelData);
/// t.generateMipmaps();
/// ```
///
/// #### Upload from texture atlas
///
/// Let's say you have texture atlas 2048x2048 in memory and target texture
/// is 128x128. you want to copy from location 256x512 in texture atlas.
///
/// Note that texture atlas is RGB tightly packed so we must specify rowAlignment = 1 in update function.
///
/// ```java
/// Texture2D t = ...;
///
/// MemorySegment textureAtlas = ...;
/// long offsetPixels = 512*2048 + 256;
/// t.update(0, 0, 0, 128, 128, GLPixelDataFormat.RGB, GLPixelDataType.UNSIGNED_BYTE, 1, 2048, textureAtlas.asSlice(offsetPixels * 3));
/// ```
public interface Texture2D extends GpuResource {
    /// The width of the texture in pixels.
    int width();

    /// Get the mip level width.
    default int mipWidth(int mipLevel) {
        return Math.max(1, width() >> mipLevel);
    }

    /// The height of the texture in pixels.
    int height();

    /// Get the mip level height.
    default int mipHeight(int mipLevel) {
        return Math.max(1, height() >> mipLevel);
    }

    /// Number of mip levels (1 for texture without mips)
    int mipLevels();
    /// The internal format of the data on the GPU.
    GLTexture2DFormat internalFormat();
    /// Currently configured parameters.
    Texture2DParameters parameters();

    /// Update part (or entire) portion of texture mip level with new pixel data from pixels in memory.
    /// @param mipLevel target mip level
    /// @param x texel offset in the x direction
    /// @param y texel offset int the y direction
    /// @param width width of the texture portion
    /// @param height height of the texture portion
    /// @param format the format of pixel data (ex. RGBA)
    /// @param type the type of pixel data (ex. UNSIGNED_BYTE)
    /// @param rowAlignment the alignment of row byte data (in bytes)
    /// @param rowLength row length (in pixels), 0 means to use width
    /// @param pixels pointer to the pixels in memory
    void update(int mipLevel, int x, int y, int width, int height,
                GLPixelDataFormat format, GLPixelDataType type,
                int rowAlignment, int rowLength,
                MemorySegment pixels);

    /// Update part (or entire) portion of texture mip level with new pixel data.
    ///
    /// Same as calling update(mipLevel, x, y, width, height, format, type, 1, 0, pixels).
    /// **NOTE**: treats source image as tightly packed (rowAlignment = 1).
    ///
    /// @see Texture2D#update(int, int, int, int, int, GLPixelDataFormat, GLPixelDataType, int, int, MemorySegment)
    default void update(int mipLevel, int x, int y, int width, int height,
                        GLPixelDataFormat format, GLPixelDataType type,
                        MemorySegment pixels) {
        update(mipLevel, x, y, width, height, format, type, 1, 0, pixels);
    }

    /// Convenience method to upload whole base level (0, 0, width(), height()).
    default void update(GLPixelDataFormat format, GLPixelDataType type, MemorySegment pixels) {
        update(0, 0, 0, width(), height(), format, type, 1, 0, pixels);
    }

    /// Update part (or entire) portion of texture mip level with new pixel data from pixels in buffer.
    /// @param mipLevel target mip level
    /// @param x texel offset in the x direction
    /// @param y texel offset int the y direction
    /// @param width width of the texture portion
    /// @param height height of the texture portion
    /// @param format the format of pixel data (ex. RGBA)
    /// @param type the type of pixel data (ex. UNSIGNED_BYTE)
    /// @param rowAlignment the alignment of row byte data (in bytes)
    /// @param rowLength row length (in pixels), 0 means to use width
    /// @param pixelBuffer reference to buffer that contains pixels, buffer is interpreted and pixel unpack buffer.
    /// @param offset offset to the first pixel in the buffer
    void update(int mipLevel, int x, int y, int width, int height,
                GLPixelDataFormat format, GLPixelDataType type,
                int rowAlignment, int rowLength,
                Buffer pixelBuffer, long offset);


    /// Generate all texture mip levels automatically.
    void generateMipmaps();

    /// Configure the texture with new parameters.
    void configure(Texture2DParameters parameters);

    /// Calculate the mip levels required for image of specified width and height.
    static int calculateMipLevels(int width, int height) {
        return 1 + (int) Math.floor(Math.log(Math.max(width, height)) / Math.log(2));
    }
}
