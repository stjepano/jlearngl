package dev.stjepano.learngl.window;

import dev.stjepano.platform.*;

public class Main {
    static void main() {
        WindowSettings windowSettings = WindowSettings.builder()
                .size(1920, 1080)
                .title("LearnOpenGL - Window")
                .vsync(true)
                .build();
        try (Platform platform = Platform.defaultInstance();
             Window window = platform.createWindow(windowSettings)) {

            Keyboard keyboard = window.keyboard();
            Mouse mouse = window.mouse();
            double totalFrameTime = 0;
            Time time = platform.time();
            OpenGL gl = platform.openGl();

            gl.viewport(0, 0, window.framebufferWidth(), window.framebufferHeight());

            while (!window.shouldClose()) {
                platform.pollEvents();
                gl.clearColorBuffer(0.11f, 0.12f, 0.13f, 1.0f);
                gl.clearDepth(1.0f);
                totalFrameTime += time.frameDeltaSeconds();
                if (mouse.wasJustPressed(MButton.LEFT)) {
                    IO.println("Left button pressed at " + mouse.x() + ", " + mouse.y());
                }
                if (keyboard.wasJustPressed(Key.ESCAPE) || mouse.wasJustPressed(MButton.RIGHT)) {
                    window.setShouldClose(true);
                    continue;
                }

                if (keyboard.wasJustPressed(Key.W)) {
                    IO.println("Window size: " + window.width() + "x" + window.height());
                    IO.println("Framebuffer size: " + window.framebufferWidth() + "x" + window.framebufferHeight());
                    IO.println("W was pressed");
                }
                if (keyboard.isPressed(Key.A)) {
                    IO.println("A is pressed");
                }
                if (keyboard.wasJustReleased(Key.D)) {
                    IO.print("D was released");
                }

                window.swapBuffers();
            }
            long frameCount = time.frameIndex();
            double fps = (double) frameCount / totalFrameTime;
            IO.println("FPS: " + fps);
        }
    }
}
