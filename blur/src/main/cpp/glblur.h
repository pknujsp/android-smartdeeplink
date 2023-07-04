//
// Created by jesp on 2023-07-04.
//

#ifndef TESTBED_GLBLUR_H
#define TESTBED_GLBLUR_H

#include <GLES3/gl32.h>
#include <GLES3/gl3ext.h>
#include <GLES3/gl3platform.h>
#include <GLES/egl.h>
#include "shared-values.h"





class StackBlur {
public:
    static const int BLUR_RADIUS_MIN = 7;
    static const int BLUR_RADIUS_MAX = 60;

    GLuint frameBuffer;
    GLuint texture;

    void processingRow(const SharedValues *const sharedValues, short *imagePixels, const int startRow, const int endRow);

    void processingColumn(const SharedValues *const sharedValues, short *imagePixels, const int startColumn, const int endColumn);

    void blur(short *imagePixels, const SharedValues *sharedValues);

    SharedValues *init(const int srcWidth, const int srcHeight, const int radius, const double resizeRatio);
};

#endif //TESTBED_GLBLUR_H
