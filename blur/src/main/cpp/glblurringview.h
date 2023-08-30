//
// Created by jesp on 2023-07-04.
//

#ifndef TESTBED_GLBLURRINGVIEW_H
#define TESTBED_GLBLURRINGVIEW_H

#include <GLES3/gl32.h>
#include <GLES3/gl3ext.h>
#include <GLES3/gl3platform.h>
#include <android/bitmap.h>
#include <android/surface_control.h>
#include <android/hardware_buffer.h>
#include <android/native_window.h>
#include "stackblur/abgr-stackblur.h"

#define BUFFER_OFFSET(offset)   ((GLvoid*) (offset))

static ABGRStackBlur *stackBlur = new ABGRStackBlur();

#endif //TESTBED_GLBLURRINGVIEW_H
