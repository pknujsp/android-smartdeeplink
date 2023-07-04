//
// Created by jesp on 2023-06-26.
//

#ifndef TESTBED_BLUR_H
#define TESTBED_BLUR_H

#include <jni.h>
#include "shared-values.h"


void processingRow(const SharedValues *const sharedValues, short *imagePixels, const int startRow, const int endRow);

void processingColumn(const SharedValues *const sharedValues, short *imagePixels, const int startColumn, const int endColumn);

void blur(short *imagePixels, const SharedValues *sharedValues);

SharedValues *init(const int srcWidth, const int srcHeight, const int radius, const double resizeRatio);

#endif //TESTBED_BLUR_H
