#ifndef PLATFORM_GLFW3_WRAPPER_H
#define PLATFORM_GLFW3_WRAPPER_H

#include "common.h"
#include "constants.h"

typedef struct GLFWwindow GLFWwindow;

typedef struct {
    const char* title;
    jint width;
    jint height;
    jboolean vsync;
} WindowSettings;

#define InputState_Pressed (1 << 0)
#define InputState_Transitioned (1 << 1)

typedef struct stWindow {
    GLFWwindow* window;
    jfloat mouse_x;
    jfloat mouse_y;
    jfloat vertical_scroll_offset;
    jint width;
    jint height;
    jint framebuffer_width;
    jint framebuffer_height;
    jboolean should_close;
    jbyte keys[Key_LAST];
    jbyte buttons[MButton_LAST];
} Window;


PLATFORM_API Window *glfw3_window_create(const WindowSettings* settings);
PLATFORM_API void glfw3_window_close(Window* handle);
PLATFORM_API void glfw3_window_set_should_close(Window* handle, jboolean val);

PLATFORM_API void glfw3_context_make_current(Window* handle);
PLATFORM_API Window *glfw3_context_get_current();
PLATFORM_API void glfw3_swap_buffers(Window* handle);

#endif //PLATFORM_GLFW3_WRAPPER_H