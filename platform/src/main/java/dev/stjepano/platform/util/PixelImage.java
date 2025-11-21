package dev.stjepano.platform.util;

import dev.stjepano.platform.memory.StackAllocator;
import dev.stjepano.platform.opengl.GLPixelDataFormat;
import dev.stjepano.platform.opengl.GLPixelDataType;

import java.lang.foreign.*;
import java.nio.file.Path;
import java.util.function.Consumer;

public class PixelImage implements AutoCloseable {

    private static final StructLayout Struct_ImageInfo;
    static {
        Struct_ImageInfo = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("width"),
                ValueLayout.JAVA_INT.withName("height"),
                ValueLayout.JAVA_INT.withName("channels")
        );
    }

    private final int width;
    private final int height;
    private final GLPixelDataFormat dataFormat;
    private final GLPixelDataType dataType;
    private MemorySegment data;
    private Consumer<MemorySegment> freeFn;

    private PixelImage(int width, int height, GLPixelDataFormat dataFormat, GLPixelDataType dataType, MemorySegment data, Consumer<MemorySegment> freeFn) {
        this.width = width;
        this.height = height;
        this.dataFormat = dataFormat;
        this.dataType = dataType;
        this.data = data;
        this.freeFn = freeFn;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public GLPixelDataFormat dataFormat() {
        return dataFormat;
    }

    public GLPixelDataType dataType() {
        return dataType;
    }

    public MemorySegment data() {
        checkClosed();
        return data;
    }

    private void checkClosed() {
        if (this.data.address() == 0L) {
            throw new IllegalStateException("PixelImage is closed!");
        }
    }

    @Override
    public void close() {
        if (freeFn != null && this.data.address() != 0L) {
            freeFn.accept(this.data);
        }
        this.data = MemorySegment.NULL;
        this.freeFn = null;
    }

    /// Load image from file system.
    ///
    /// @param filePath the path to image file (file must exist)
    /// @param flipVertically should the image be flipped vertically
    /// @throws LoadException if for some reason we fail to load the image
    public static PixelImage load(Path filePath, boolean flipVertically) {
        String absoluteFilePath = filePath.toAbsolutePath().normalize().toString();
        try (StackAllocator stack = StackAllocator.push()) {
            MemorySegment filePathPtr = stack.allocateFrom(absoluteFilePath);
            MemorySegment imageInfoPtr = stack.allocate(Struct_ImageInfo);
            MemorySegment errorBuf = stack.allocate(512, 8);

            MemorySegment pixels = JSTBI.jstbiLoadFromFile(filePathPtr, flipVertically, imageInfoPtr, errorBuf, (int) errorBuf.byteSize());
            if (pixels.address() == 0L) {
                String errorMessage = errorBuf.getString(0);
                throw new LoadException("Failed to load image at " + filePath + ": " + errorMessage);
            }

            long widthOffset = Struct_ImageInfo.byteOffset(MemoryLayout.PathElement.groupElement("width"));
            long heightOffset = Struct_ImageInfo.byteOffset(MemoryLayout.PathElement.groupElement("height"));
            long channelsOffset = Struct_ImageInfo.byteOffset(MemoryLayout.PathElement.groupElement("channels"));
            int width = imageInfoPtr.get(ValueLayout.JAVA_INT, widthOffset);
            int height = imageInfoPtr.get(ValueLayout.JAVA_INT, heightOffset);
            int channels = imageInfoPtr.get(ValueLayout.JAVA_INT, channelsOffset);

            if (width <= 0 || height <= 0) {
                JSTBI.jstbiFree(pixels);
                throw new LoadException("Image " + filePath + " loaded with illegal size. Width = " + width + ", height = " + height);
            }

            GLPixelDataType pixelDataType = GLPixelDataType.UNSIGNED_BYTE;
            GLPixelDataFormat pixelDataFormat;
            if (channels == 1) {
                pixelDataFormat = GLPixelDataFormat.RED;
            } else if (channels == 2) {
                pixelDataFormat = GLPixelDataFormat.RG;
            } else if (channels == 3) {
                pixelDataFormat = GLPixelDataFormat.RGB;
            } else if (channels == 4) {
                pixelDataFormat = GLPixelDataFormat.RGBA;
            } else {
                JSTBI.jstbiFree(pixels);
                throw new LoadException("Image " + filePath + " loaded with illegal channel count. Channels = " + channels);
            }

            return new PixelImage(width,
                    height,
                    pixelDataFormat,
                    pixelDataType,
                    pixels,
                    JSTBI::jstbiFree);
        }
    }

    /// Load image from memory.
    ///
    /// This function expects entire image file in the memory.
    ///
    /// @param memory some memory buffer where the image data is, must be native memory
    /// @param flipVertically should the image be flipped vertically
    /// @throws LoadException if for some reason we fail to load the image
    public static PixelImage load(MemorySegment memory, boolean flipVertically) {
        try (StackAllocator stack = StackAllocator.push()) {
            MemorySegment imageInfoPtr = stack.allocate(Struct_ImageInfo);
            MemorySegment errorBuf = stack.allocate(512, 8);

            MemorySegment pixels = JSTBI.jstbiLoadFromMemory(memory, memory.byteSize(), flipVertically, imageInfoPtr, errorBuf, (int) errorBuf.byteSize());
            if (pixels.address() == 0L) {
                String errorMessage = errorBuf.getString(0);
                throw new LoadException("Failed to load image from memory: " + errorMessage);
            }

            long widthOffset = Struct_ImageInfo.byteOffset(MemoryLayout.PathElement.groupElement("width"));
            long heightOffset = Struct_ImageInfo.byteOffset(MemoryLayout.PathElement.groupElement("height"));
            long channelsOffset = Struct_ImageInfo.byteOffset(MemoryLayout.PathElement.groupElement("channels"));
            int width = imageInfoPtr.get(ValueLayout.JAVA_INT, widthOffset);
            int height = imageInfoPtr.get(ValueLayout.JAVA_INT, heightOffset);
            int channels = imageInfoPtr.get(ValueLayout.JAVA_INT, channelsOffset);

            if (width <= 0 || height <= 0) {
                JSTBI.jstbiFree(pixels);
                throw new LoadException("Image loaded with illegal size. Width = " + width + ", height = " + height);
            }

            GLPixelDataType pixelDataType = GLPixelDataType.UNSIGNED_BYTE;
            GLPixelDataFormat pixelDataFormat;
            if (channels == 1) {
                pixelDataFormat = GLPixelDataFormat.RED;
            } else if (channels == 2) {
                pixelDataFormat = GLPixelDataFormat.RG;
            } else if (channels == 3) {
                pixelDataFormat = GLPixelDataFormat.RGB;
            } else if (channels == 4) {
                pixelDataFormat = GLPixelDataFormat.RGBA;
            } else {
                JSTBI.jstbiFree(pixels);
                throw new LoadException("Image loaded with illegal channel count. Channels = " + channels);
            }

            return new PixelImage(width,
                    height,
                    pixelDataFormat,
                    pixelDataType,
                    pixels,
                    JSTBI::jstbiFree);
        }
    }

    /// Utility method to load image from resource.
    ///
    /// This function will first read the resource into native memory segment,
    /// and then call `load(segment, fileType, flipVertically)`. As you can imagine this
    /// function is slow and not efficient so use sparingly.
    /// @param resourcePath the path to resource file, extension is significant
    /// @param flipVertically should the image be flipped vertically
    /// @throws ResourceNotFoundException if resource does not exist at resourcePath
    /// @throws LoadException if for some reason we fail to load the image
    public static PixelImage loadFromResource(String resourcePath, boolean flipVertically) {
        try (Arena arena = Arena.ofConfined()) {
            byte[] imageFileData = JavaResources.loadIntoByteArray(resourcePath);

            // Allocate memory for file data
            MemorySegment imageFileDataMemory = arena.allocateFrom(ValueLayout.JAVA_BYTE, imageFileData);
            return PixelImage.load(imageFileDataMemory, flipVertically);

            // Memory for file data is now cleaned when Arena closes
        }
    }

}
