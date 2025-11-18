#include "glfw3_wrapper.h"

#include "platform.h"

#include <glad/glad.h>
#define GLFW3_INCLUDE_NONE
#include <GLFW/glfw3.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static enum Key translate_glfw_key(int key);
static enum MButton translate_glfw_button(int button);

static void key_callback(GLFWwindow *window, int key, int scancode, int action, int mods) {
    ((void) scancode);
    ((void) mods);
    Window *handle = glfwGetWindowUserPointer(window);
    if (action == GLFW_PRESS) {
        int jkey = translate_glfw_key(key);
        if (jkey < Key_LAST) {
            handle->keys[jkey] = (jbyte) (InputState_Pressed | InputState_Transitioned);
        }
    } else if (action == GLFW_RELEASE) {
        int jkey = translate_glfw_key(key);
        if (jkey < Key_LAST) {
            handle->keys[jkey] = (jbyte) (InputState_Transitioned);
        }
    }
}

static void button_callback(GLFWwindow *window, int button, int action, int mods) {
    Window *handle = glfwGetWindowUserPointer(window);
    if (action == GLFW_PRESS) {
        int jbutton = translate_glfw_button(button);
        if (jbutton < MButton_LAST) {
            handle->buttons[jbutton] = (jbyte) (InputState_Pressed | InputState_Transitioned);
        }
    } else if (action == GLFW_RELEASE) {
        int jbutton = translate_glfw_button(button);
        if (jbutton < MButton_LAST) {
            handle->buttons[jbutton] = (jbyte) (InputState_Transitioned);
        }
    }
}

static void cursor_pos_callback(GLFWwindow *window, double x, double y) {
    Window *handle = glfwGetWindowUserPointer(window);
    handle->mouse_x = (float) x;
    handle->mouse_y = (float) y;
}

static void scroll_callback(GLFWwindow *window, double x, double y) {
    (void) x;
    Window *handle = glfwGetWindowUserPointer(window);
    handle->vertical_scroll_offset = (float) y;
}

static void window_close_callback(GLFWwindow *window) {
    Window *handle = glfwGetWindowUserPointer(window);
    handle->should_close = JTRUE;
}

static void window_size_callback(GLFWwindow *window, int width, int height) {
    Window *handle = glfwGetWindowUserPointer(window);
    handle->width = width;
    handle->height = height;
}

static void framebuffer_size_callback(GLFWwindow *window, int width, int height) {
    Window *handle = glfwGetWindowUserPointer(window);
    handle->framebuffer_width = width;
    handle->framebuffer_height = height;
}

Window *glfw3_window_create(const WindowSettings *settings) {
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
    glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
    glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
    GLFWwindow *window = glfwCreateWindow(settings->width, settings->height, settings->title, NULL, NULL);
    if (window == NULL) {
        return NULL;
    }

    // Setup callbacks
    glfwSetKeyCallback(window, key_callback);
    glfwSetMouseButtonCallback(window, button_callback);
    glfwSetCursorPosCallback(window, cursor_pos_callback);
    glfwSetScrollCallback(window, scroll_callback);
    glfwSetWindowCloseCallback(window, window_close_callback);
    glfwSetWindowSizeCallback(window, window_size_callback);
    glfwSetFramebufferSizeCallback(window, framebuffer_size_callback);

    Window *result = malloc(sizeof(Window));
    if (result == NULL) {
        platform_set_error("Out of memory!");
        goto LError;
    }
    memset(result, 0, sizeof(Window));
    result->window = window;
    if (!platform_register_window(result)) {
        platform_set_error("Max window count reached!");
        goto LError;
    }

    // initialize with sizes
    glfwGetWindowSize(window, &result->width, &result->height);
    glfwGetFramebufferSize(window, &result->framebuffer_width, &result->framebuffer_height);

    // important! it allows the window to reference our structure
    glfwSetWindowUserPointer(window, result);

    // Make it current context
    glfwMakeContextCurrent(window);
    if (settings->vsync) {
        glfwSwapInterval(1);
    } else {
        glfwSwapInterval(0);
    }

    if (!gladLoadGLLoader((GLADloadproc) glfwGetProcAddress)) {
        platform_set_error("Failed to initialize GLAD!");
        goto LError;
    }

    goto LSuccess;
LError:
    free(result);
    glfwDestroyWindow(window);
    result = NULL;
LSuccess:
    return result;
}

void glfw3_window_close(Window *handle) {
    if (handle) {
        glfwDestroyWindow(handle->window);
        platform_deregister_window(handle);
        free(handle);
    }
}

void glfw3_window_set_should_close(Window *handle, jboolean val) {
    glfwSetWindowShouldClose(handle->window, val);
    handle->should_close = val;
}

void glfw3_context_make_current(Window *handle) {
    glfwMakeContextCurrent(handle->window);
}

Window *glfw3_context_get_current() {
    GLFWwindow *window = glfwGetCurrentContext();
    if (window) {
        Window *handle = glfwGetWindowUserPointer(window);
        return handle;
    }
    return NULL;
}

void glfw3_swap_buffers(Window *handle) {
    glfwSwapBuffers(handle->window);
}

#define KEY_CASE(nm) case GLFW_KEY_##nm: result = Key_##nm; break

enum Key translate_glfw_key(int key) {
    enum Key result;
    switch (key) {
        case GLFW_KEY_0: result = Key_DIGIT_0;
            break;
        case GLFW_KEY_1: result = Key_DIGIT_1;
            break;
        case GLFW_KEY_2: result = Key_DIGIT_2;
            break;
        case GLFW_KEY_3: result = Key_DIGIT_3;
            break;
        case GLFW_KEY_4: result = Key_DIGIT_4;
            break;
        case GLFW_KEY_5: result = Key_DIGIT_5;
            break;
        case GLFW_KEY_6: result = Key_DIGIT_6;
            break;
        case GLFW_KEY_7: result = Key_DIGIT_7;
            break;
        case GLFW_KEY_8: result = Key_DIGIT_8;
            break;
        case GLFW_KEY_9: result = Key_DIGIT_9;
            break;
        KEY_CASE(SPACE);
        KEY_CASE(APOSTROPHE);
        KEY_CASE(COMMA);
        KEY_CASE(MINUS);
        KEY_CASE(PERIOD);
        KEY_CASE(SLASH);
        KEY_CASE(SEMICOLON);
        KEY_CASE(EQUAL);
        KEY_CASE(A);
        KEY_CASE(B);
        KEY_CASE(C);
        KEY_CASE(D);
        KEY_CASE(E);
        KEY_CASE(F);
        KEY_CASE(G);
        KEY_CASE(H);
        KEY_CASE(I);
        KEY_CASE(J);
        KEY_CASE(K);
        KEY_CASE(L);
        KEY_CASE(M);
        KEY_CASE(N);
        KEY_CASE(O);
        KEY_CASE(P);
        KEY_CASE(Q);
        KEY_CASE(R);
        KEY_CASE(S);
        KEY_CASE(T);
        KEY_CASE(U);
        KEY_CASE(V);
        KEY_CASE(W);
        KEY_CASE(X);
        KEY_CASE(Y);
        KEY_CASE(Z);
        KEY_CASE(LEFT_BRACKET);
        KEY_CASE(BACKSLASH);
        KEY_CASE(RIGHT_BRACKET);
        KEY_CASE(GRAVE_ACCENT);
        KEY_CASE(WORLD_1);
        KEY_CASE(WORLD_2);
        KEY_CASE(ESCAPE);
        KEY_CASE(ENTER);
        KEY_CASE(TAB);
        KEY_CASE(BACKSPACE);
        KEY_CASE(INSERT);
        KEY_CASE(DELETE);
        KEY_CASE(RIGHT);
        KEY_CASE(LEFT);
        KEY_CASE(DOWN);
        KEY_CASE(UP);
        KEY_CASE(PAGE_UP);
        KEY_CASE(PAGE_DOWN);
        KEY_CASE(HOME);
        KEY_CASE(END);
        KEY_CASE(CAPS_LOCK);
        KEY_CASE(SCROLL_LOCK);
        KEY_CASE(NUM_LOCK);
        KEY_CASE(PRINT_SCREEN);
        KEY_CASE(PAUSE);
        KEY_CASE(F1);
        KEY_CASE(F2);
        KEY_CASE(F3);
        KEY_CASE(F4);
        KEY_CASE(F5);
        KEY_CASE(F6);
        KEY_CASE(F7);
        KEY_CASE(F8);
        KEY_CASE(F9);
        KEY_CASE(F10);
        KEY_CASE(F11);
        KEY_CASE(F12);
        KEY_CASE(F13);
        KEY_CASE(F14);
        KEY_CASE(F15);
        KEY_CASE(F16);
        KEY_CASE(F17);
        KEY_CASE(F18);
        KEY_CASE(F19);
        KEY_CASE(F20);
        KEY_CASE(F21);
        KEY_CASE(F22);
        KEY_CASE(F23);
        KEY_CASE(F24);
        KEY_CASE(F25);
        KEY_CASE(KP_0);
        KEY_CASE(KP_1);
        KEY_CASE(KP_2);
        KEY_CASE(KP_3);
        KEY_CASE(KP_4);
        KEY_CASE(KP_5);
        KEY_CASE(KP_6);
        KEY_CASE(KP_7);
        KEY_CASE(KP_8);
        KEY_CASE(KP_9);
        KEY_CASE(KP_DECIMAL);
        KEY_CASE(KP_DIVIDE);
        KEY_CASE(KP_MULTIPLY);
        KEY_CASE(KP_SUBTRACT);
        KEY_CASE(KP_ADD);
        KEY_CASE(KP_ENTER);
        KEY_CASE(KP_EQUAL);
        KEY_CASE(LEFT_SHIFT);
        KEY_CASE(LEFT_CONTROL);
        KEY_CASE(LEFT_ALT);
        KEY_CASE(LEFT_SUPER);
        KEY_CASE(RIGHT_SHIFT);
        KEY_CASE(RIGHT_CONTROL);
        KEY_CASE(RIGHT_ALT);
        KEY_CASE(RIGHT_SUPER);
        KEY_CASE(MENU);
        default: result = Key_LAST;
            break;
    }
    return result;
}

enum MButton translate_glfw_button(int button) {
    enum MButton result;
    switch (button) {
        case GLFW_MOUSE_BUTTON_LEFT: result = MButton_LEFT; break;
        case GLFW_MOUSE_BUTTON_RIGHT: result = MButton_RIGHT; break;
        case GLFW_MOUSE_BUTTON_MIDDLE: result = MButton_MIDDLE; break;
        default: result = MButton_LAST; break;
    }
    return result;
}
