package dev.stjepano.platform.impl;

import dev.stjepano.platform.Key;
import dev.stjepano.platform.MButton;
import dev.stjepano.platform.WindowSettings;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.NoSuchElementException;

public final class NativePlatform {

    private static final String LIBRARY_NAME = "platform";

    public static final int INPUT_STATE_PRESSED = 1;
    public static final int INPUT_STATE_TRANSITIONED = 2;

    private static Linker linker;
    private static SymbolLookup symbolLookup;

    private static MethodHandle hPlatformGetError;
    private static MethodHandle hPlatformInit;
    private static MethodHandle hPlatformPollEvents;
    private static MethodHandle hPlatformTerminate;

    private static MethodHandle hGlfw3ContextGetCurrent;
    private static MethodHandle hGlfw3ContextMakeCurrent;
    private static MethodHandle hGlfw3SwapBuffers;

    private static MethodHandle hGlfw3WindowClose;
    private static MethodHandle hGlfw3WindowCreate;
    private static MethodHandle hGlfw3WindowSetShouldClose;

    private static GroupLayout windowSettingsLayout;


    // OpenGL handles
    private static MethodHandle hGlViewport;
    private static MethodHandle hGlClearNamedFramebufferiv;
    private static MethodHandle hGlClearNamedFramebufferuiv;
    private static MethodHandle hGlClearNamedFramebufferfv;
    private static MethodHandle hGlClearNamedFramebufferfi;
    private static MethodHandle hGlNamedBufferSubData;
    private static MethodHandle hGlMapNamedBufferRange;
    private static MethodHandle hGlUnmapNamedBuffer;
    private static MethodHandle hGlFlushMappedNamedBufferRange;
    private static MethodHandle hGlDeleteBuffers;
    private static MethodHandle hGlCreateBufferWithStorage;

    private static final int ERROR_BUFFER_SZ = 1024;

    private static MethodHandle findFunction(String functionName, FunctionDescriptor functionDescriptor) {
        return linker.downcallHandle(
                symbolLookup.find(functionName).orElseThrow(() -> new NoSuchElementException("Function " + functionName + " not found in library")),
                functionDescriptor);
    }

    private static String detectOS() {
        String osProp = System.getProperty("os.name").toLowerCase();
        if (osProp.contains("linux")) {
            return "linux";
        }
        if (osProp.contains("windows")) {
            return "windows";
        }
        if (osProp.contains("mac")) {
            return "macos";
        }
        throw new RuntimeException("Can not determine OS (os.name = " + osProp + ")");
    }

    private static String detectArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.contains("amd64") || arch.contains("x86_64")) {
            return "x64";
        }
        if (arch.contains("aarch64") || arch.contains("arm64")) {
            return "arm64";
        }
        throw new RuntimeException("Can not determine OS architecture (os.arch = " + arch + ")");
    }

    private static Path extractLibrary() {
        String os = detectOS();
        String arch = detectArch();

        String libFileName = System.mapLibraryName(LIBRARY_NAME);
        String resourcePath = "/dev/stjepano/platform/natives/" + os + "/" + arch + "/" + libFileName;
        try (InputStream inputStream = NativePlatform.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Can not find native library in " + resourcePath);
            }

            Path tempDir = Files.createTempDirectory("platform-natives");
            Path libPath = tempDir.resolve(tempDir);

            Files.copy(inputStream, libPath, StandardCopyOption.REPLACE_EXISTING);

            //noinspection ResultOfMethodCallIgnored
            libPath.toFile().setExecutable(true);

            return libPath;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadLibrary() {
        linker = Linker.nativeLinker();
        Path libPath = extractLibrary();
        symbolLookup = SymbolLookup.libraryLookup(libPath, Arena.global());

        windowSettingsLayout = MemoryLayout.structLayout(
                ValueLayout.ADDRESS.withName("title"),
                ValueLayout.JAVA_INT.withName("width"),
                ValueLayout.JAVA_INT.withName("height"),
                ValueLayout.JAVA_BOOLEAN.withName("vsync")
        );

        hPlatformGetError = findFunction("platform_get_error", FunctionDescriptor.of(ValueLayout.ADDRESS));
        hPlatformInit = findFunction("platform_init", FunctionDescriptor.of(ValueLayout.JAVA_INT));
        hPlatformPollEvents = findFunction("platform_poll_events", FunctionDescriptor.ofVoid());
        hPlatformTerminate = findFunction("platform_terminate", FunctionDescriptor.ofVoid());

        hGlfw3ContextGetCurrent = findFunction("glfw3_context_get_current", FunctionDescriptor.of(ValueLayout.ADDRESS));
        hGlfw3ContextMakeCurrent = findFunction("glfw3_context_make_current", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        hGlfw3SwapBuffers = findFunction("glfw3_swap_buffers", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

        hGlfw3WindowClose = findFunction("glfw3_window_close", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        hGlfw3WindowCreate = findFunction("glfw3_window_create", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        hGlfw3WindowSetShouldClose = findFunction("glfw3_window_set_should_close", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN));

        // OpenGL functions
        hGlViewport = findFunction("jglViewport", FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
        hGlClearNamedFramebufferiv = findFunction("jglClearNamedFramebufferiv", FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        hGlClearNamedFramebufferuiv = findFunction("jglClearNamedFramebufferuiv", FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        hGlClearNamedFramebufferfv = findFunction("jglClearNamedFramebufferfv", FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        hGlClearNamedFramebufferfi = findFunction("jglClearNamedFramebufferfi", FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_INT));
        hGlNamedBufferSubData = findFunction("jglNamedBufferSubData", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        hGlMapNamedBufferRange = findFunction("jglMapNamedBufferRange", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
        hGlUnmapNamedBuffer = findFunction("jglUnmapNamedBuffer", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_INT));
        hGlFlushMappedNamedBufferRange = findFunction("jglFlushMappedNamedBufferRange", FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
        hGlDeleteBuffers = findFunction("jglDeleteBuffers", FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT));
        hGlCreateBufferWithStorage = findFunction("jglCreateBufferWithStorage", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    }

    public static MemorySegment glfw3WindowCreate(WindowSettings settings) {
        long titleOffset = windowSettingsLayout.byteOffset(MemoryLayout.PathElement.groupElement("title"));
        long widthOffset = windowSettingsLayout.byteOffset(MemoryLayout.PathElement.groupElement("width"));
        long heightOffset = windowSettingsLayout.byteOffset(MemoryLayout.PathElement.groupElement("height"));
        long vsyncOffset = windowSettingsLayout.byteOffset(MemoryLayout.PathElement.groupElement("vsync"));

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment settingsPtr = arena.allocate(windowSettingsLayout);
            settingsPtr.set(ValueLayout.ADDRESS, titleOffset, arena.allocateFrom(settings.title()));
            settingsPtr.set(ValueLayout.JAVA_INT, widthOffset, settings.width());
            settingsPtr.set(ValueLayout.JAVA_INT, heightOffset, settings.height());
            settingsPtr.set(ValueLayout.JAVA_BOOLEAN, vsyncOffset, settings.vsync());

            MemorySegment windowPtr = (MemorySegment) hGlfw3WindowCreate.invokeExact(settingsPtr);
            if (windowPtr.address() == 0L) {
                return MemorySegment.NULL;
            }
            return windowPtr.reinterpret(WindowStructPtr.LAYOUT.byteSize());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void glfw3WindowClose(MemorySegment windowPtr) {
        try {
            hGlfw3WindowClose.invokeExact(windowPtr);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void glfw3WindowSetShouldClose(MemorySegment windowPtr, boolean value) {
        try {
            hGlfw3WindowSetShouldClose.invokeExact(windowPtr, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void glfw3ContextMakeCurrent(MemorySegment windowPtr) {
        try {
            hGlfw3ContextMakeCurrent.invokeExact(windowPtr);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public static MemorySegment glfw3ContextGetCurrent() {
        try {
            MemorySegment segment = (MemorySegment) hGlfw3ContextGetCurrent.invokeExact();
            if (segment.address() != 0L) {
                segment = segment.reinterpret(WindowStructPtr.LAYOUT.byteSize());
            }
            return segment;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void glfw3SwapBuffers(MemorySegment windowPtr) {
        try {
            hGlfw3SwapBuffers.invokeExact(windowPtr);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean platformInit() {
        try {
            int result = (int) hPlatformInit.invokeExact();
            return result == 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void platformTerminate() {
        try {
            hPlatformTerminate.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void platformPollEvents() {
        try {
            hPlatformPollEvents.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String platformGetError() {
        try {
            MemorySegment messagePtr = (MemorySegment) hPlatformGetError.invokeExact();
            if (messagePtr.address() == 0L) {
                return null;
            }
            return messagePtr.reinterpret(ERROR_BUFFER_SZ).getString(0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void jglViewport(int x, int y, int width, int height) {
        try {
            hGlViewport.invokeExact(x, y, width, height);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public static void jglClearNamedFramebufferiv(int framebuffer, int buffer, int drawbuffer, MemorySegment valuePtr) {
        try {
            hGlClearNamedFramebufferiv.invokeExact(framebuffer, buffer, drawbuffer, valuePtr);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public static void jglClearNamedFramebufferuiv(int framebuffer, int buffer, int drawbuffer, MemorySegment valuePtr) {
        try {
            hGlClearNamedFramebufferuiv.invokeExact(framebuffer, buffer, drawbuffer, valuePtr);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void jglClearNamedFramebufferfv(int framebuffer, int buffer, int drawbuffer, MemorySegment valuePtr) {
        try {
            hGlClearNamedFramebufferfv.invokeExact(framebuffer, buffer, drawbuffer, valuePtr);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void jglClearNamedFramebufferfi(int framebuffer, int buffer, int drawbuffer, float depthValue, int stencilValue) {
        try {
            hGlClearNamedFramebufferfi.invokeExact(framebuffer, buffer, drawbuffer, depthValue, stencilValue);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean jglNamedBufferSubData(int buffer, long offset, long size, MemorySegment data) {
        try {
            return (boolean) hGlNamedBufferSubData.invokeExact(buffer, offset, size, data);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static MemorySegment jglMapNamedBufferRange(int buffer, long offset, long length, int accessflags) {
        try {
            return (MemorySegment) hGlMapNamedBufferRange.invokeExact(buffer, offset, length, accessflags);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean jglUnmapNamedBuffer(int buffer) {
        try {
            return (boolean) hGlUnmapNamedBuffer.invokeExact(buffer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean jglFlushMappedNamedBufferRange(int buffer, long offset, long length) {
        try {
            return (boolean) hGlFlushMappedNamedBufferRange.invokeExact(buffer, offset, length);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void jglDeleteBuffers(int buffer) {
        try {
            hGlDeleteBuffers.invokeExact(buffer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int jglCreateBufferWithStorage(long byteSize, int storageflags, MemorySegment data) {
        try {
            return (int) hGlCreateBufferWithStorage.invokeExact(byteSize, storageflags, data);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static class WindowStructPtr {

        public static final GroupLayout LAYOUT;

        @SuppressWarnings("unused")
        private static final long WINDOW_OFFSET;
        private static final long MOUSE_X_OFFSET;
        private static final long MOUSE_Y_OFFSET;
        private static final long VERTICAL_SCROLL_OFFSET;
        private static final long WIDTH_OFFSET;
        private static final long HEIGHT_OFFSET;
        private static final long FRAMEBUFFER_WIDTH_OFFSET;
        private static final long FRAMEBUFFER_HEIGHT_OFFSET;
        private static final long SHOULD_CLOSE_OFFSET;
        private static final long KEYS_OFFSET;
        private static final long BUTTONS_OFFSET;

        static {
            LAYOUT = MemoryLayout.structLayout(
                    ValueLayout.ADDRESS.withName("window"),
                    ValueLayout.JAVA_FLOAT.withName("mouse_x"),
                    ValueLayout.JAVA_FLOAT.withName("mouse_y"),
                    ValueLayout.JAVA_FLOAT.withName("vertical_scroll_offset"),
                    ValueLayout.JAVA_INT.withName("width"),
                    ValueLayout.JAVA_INT.withName("height"),
                    ValueLayout.JAVA_INT.withName("framebuffer_width"),
                    ValueLayout.JAVA_INT.withName("framebuffer_height"),
                    ValueLayout.JAVA_BOOLEAN.withName("should_close"),
                    MemoryLayout.sequenceLayout(Key.LAST.ordinal(), ValueLayout.JAVA_BYTE).withName("keys"),
                    MemoryLayout.sequenceLayout(MButton.LAST.ordinal(), ValueLayout.JAVA_BYTE).withName("buttons")
            );

            WINDOW_OFFSET = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("window"));
            MOUSE_X_OFFSET = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("mouse_x"));
            MOUSE_Y_OFFSET = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("mouse_y"));
            VERTICAL_SCROLL_OFFSET = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("vertical_scroll_offset"));
            WIDTH_OFFSET = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("width"));
            HEIGHT_OFFSET = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("height"));
            FRAMEBUFFER_WIDTH_OFFSET = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("framebuffer_width"));
            FRAMEBUFFER_HEIGHT_OFFSET = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("framebuffer_height"));
            SHOULD_CLOSE_OFFSET = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("should_close"));
            KEYS_OFFSET = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("keys"));
            BUTTONS_OFFSET = LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("buttons"));
        }

        public static float mouseX(MemorySegment windowPtr) {
            return windowPtr.get(ValueLayout.JAVA_FLOAT, MOUSE_X_OFFSET);
        }

        public static float mouseY(MemorySegment windowPtr) {
            return windowPtr.get(ValueLayout.JAVA_FLOAT, MOUSE_Y_OFFSET);
        }

        public static float verticalScrollOffset(MemorySegment windowPtr) {
            return windowPtr.get(ValueLayout.JAVA_FLOAT, VERTICAL_SCROLL_OFFSET);
        }

        public static int width(MemorySegment windowPtr) {
            return windowPtr.get(ValueLayout.JAVA_INT, WIDTH_OFFSET);
        }

        public static int height(MemorySegment windowPtr) {
            return windowPtr.get(ValueLayout.JAVA_INT, HEIGHT_OFFSET);
        }

        public static int framebufferWidth(MemorySegment windowPtr) {
            return windowPtr.get(ValueLayout.JAVA_INT, FRAMEBUFFER_WIDTH_OFFSET);
        }

        public static int framebufferHeight(MemorySegment windowPtr) {
            return windowPtr.get(ValueLayout.JAVA_INT, FRAMEBUFFER_HEIGHT_OFFSET);
        }

        public static boolean shouldClose(MemorySegment windowPtr) {
            return windowPtr.get(ValueLayout.JAVA_BOOLEAN, SHOULD_CLOSE_OFFSET);
        }

        public static byte key(MemorySegment windowPtr, Key key) {
            if (key == Key.LAST) {
                return 0;
            }
            long offset = KEYS_OFFSET + (key.ordinal() * ValueLayout.JAVA_BYTE.byteSize());
            return windowPtr.get(ValueLayout.JAVA_BYTE, offset);
        }

        @SuppressWarnings("unused")
        public static MemorySegment keys(MemorySegment windowPtr) {
            return windowPtr.asSlice(KEYS_OFFSET, Key.LAST.ordinal() * ValueLayout.JAVA_BYTE.byteSize());
        }

        public static byte button(MemorySegment windowPtr, MButton button) {
            if (button == MButton.LAST) {
                return 0;
            }
            long offset = BUTTONS_OFFSET + (button.ordinal() * ValueLayout.JAVA_BYTE.byteSize());
            return windowPtr.get(ValueLayout.JAVA_BYTE, offset);
        }

        @SuppressWarnings("unused")
        public static MemorySegment buttons(MemorySegment windowPtr) {
            return windowPtr.asSlice(BUTTONS_OFFSET, MButton.LAST.ordinal() * ValueLayout.JAVA_BYTE.byteSize());
        }

    }

}
