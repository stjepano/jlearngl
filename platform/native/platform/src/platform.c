#include "platform.h"

#include "glfw3_wrapper.h"
#define GLFW3_INCLUDE_NONE
#include <GLFW/glfw3.h>

#include <stdlib.h>
#include <stdio.h>

#define MAX_WINDOWS 8
static Window *s_window_list[MAX_WINDOWS] = {0};

#define ERROR_BUFFER_SZ 1024
static char s_error_buffer[ERROR_BUFFER_SZ] = {0};

static void error_callback(int error_code, const char *description) {
    if (error_code != GLFW_NO_ERROR) {
        platform_set_error(description);
    } else {
        platform_set_error(NULL);
    }
}

static void reset_input_transitions() {
    for (int i = 0; i < MAX_WINDOWS; i++) {
        Window *handle = s_window_list[i];
        if (handle) {
            jbyte *key_states = handle->keys;
            jbyte *button_states = handle->buttons;
            for (int j = 0; j < Key_LAST; j++) {
                key_states[j] &= (jbyte) ~(InputState_Transitioned);
            }
            for (int j = 0; j < MButton_LAST; j++) {
                button_states[j] &= (jbyte) ~(InputState_Transitioned);
            }

            // reset scroll offsets
            handle->vertical_scroll_offset = 0.0f;
        }
    }
}

jint_result platform_init() {
    if (!glfwInit()) {
        const char *error_desc = NULL;
        if (glfwGetError(&error_desc) == GLFW_NO_ERROR) {
            // if glfwInit does not set error description, make up our own
            snprintf(s_error_buffer, ERROR_BUFFER_SZ, "Unknown error while initializing GLFW.");
        } else {
            snprintf(s_error_buffer, ERROR_BUFFER_SZ, "%s", error_desc);
        }
        return RESULT_FAIL;
    }
    glfwSetErrorCallback(error_callback);

    return RESULT_OK;
}

void platform_terminate() {
    for (int i = 0; i < MAX_WINDOWS; i++) {
        if (s_window_list[i]) {
            glfwDestroyWindow(s_window_list[i]->window);
            free(s_window_list[i]);
            s_window_list[i] = NULL;
        }
    }
    glfwTerminate();
}

void platform_poll_events() {
    reset_input_transitions();
    glfwPollEvents();
}

const char *platform_get_error() {
    return s_error_buffer;
}

void platform_set_error(const char *error) {
    if (error) {
        snprintf(s_error_buffer, ERROR_BUFFER_SZ, "%s", error);
    } else {
        s_error_buffer[0] = 0;
    }
}

int platform_register_window(Window *window) {
    if (!window) {
        return 0;
    }
    int index = -1;
    for (int i = 0; i < MAX_WINDOWS; i++) {
        if (s_window_list[i] == NULL) {
            index = i;
            break;
        }
    }
    if (index == -1) {
        return 0;
    }

    s_window_list[index] = window;
    return 1;
}

void platform_deregister_window(Window *window) {
    if (window) {
        for (int i = 0; i < MAX_WINDOWS; i++) {
            if (s_window_list[i] == window) {
                s_window_list[i] = NULL;
            }
        }
    }
}
