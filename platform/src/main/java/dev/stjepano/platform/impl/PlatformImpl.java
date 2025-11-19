package dev.stjepano.platform.impl;

import dev.stjepano.platform.*;
import dev.stjepano.platform.impl.opengl.OpenGLImpl;
import dev.stjepano.platform.opengl.OpenGL;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.WeakHashMap;

public final class PlatformImpl implements Platform {

    private static PlatformImpl sInstance = null;
    private static final WeakHashMap<WindowImpl, Integer> reachableWindows = new WeakHashMap<>();

    private final TimeImpl time;
    private final OpenGLImpl openGL;

    public static PlatformImpl getInstance() {
        // NOTE: not multithreading safe
        if (sInstance == null) {
            sInstance = new PlatformImpl();
        }
        return sInstance;
    }

    private PlatformImpl() {
        NativePlatform.loadLibrary();
        if (!NativePlatform.platformInit()) {
            String errorMessage = NativePlatform.platformGetError();
            throw new PlatformException("Failed to initialize platform: " + errorMessage);
        }
        time = new TimeImpl();
        openGL = new OpenGLImpl();
    }

    @Override
    public Window createWindow(WindowSettings windowSettings) {
        MemorySegment windowPtr = NativePlatform.glfw3WindowCreate(windowSettings);
        if (windowPtr.address() == 0L) {
            String errorMessage = NativePlatform.platformGetError();
            throw new PlatformException("Failed to create window: " + errorMessage);
        }
        var window = new WindowImpl(windowPtr);
        reachableWindows.put(window, 55);
        return window;
    }

    @Override
    public void pollEvents() {
        this.time.update();
        NativePlatform.platformPollEvents();
    }

    @Override
    public void setGLContextForCurrentThread(Window window) {
        if (window instanceof WindowImpl windowImpl) {
            NativePlatform.glfw3ContextMakeCurrent(windowImpl.windowPtr);
        } else {
            throw new PlatformException("Window not instance of WindowImpl!");
        }
    }

    @Override
    public Time time() {
        return this.time;
    }

    @Override
    public OpenGL openGl() {
        return openGL;
    }

    @Override
    public void close() {
        // NOTE: this make sure that you can not use Window or Mouse/Keyboard after you close the platform
        var windows = new ArrayList<>(reachableWindows.keySet());
        for (var window : windows) {
            window.close();
        }
        NativePlatform.platformTerminate();
    }
}
