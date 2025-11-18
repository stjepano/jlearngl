#ifndef PLATFORM_PLATFORM_H
#define PLATFORM_PLATFORM_H

#include "common.h"

PLATFORM_API jint_result platform_init();
PLATFORM_API void platform_terminate();
PLATFORM_API void platform_poll_events();
PLATFORM_API const char* platform_get_error();

void platform_set_error(const char* error);

typedef struct stWindow Window;
int platform_register_window(Window* window);
void platform_deregister_window(Window* window);

#endif //PLATFORM_PLATFORM_H