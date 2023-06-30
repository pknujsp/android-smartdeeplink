//
// Created by jesp on 2023-06-30.
//

#ifndef TESTBED_SHARED_VALUES_H
#define TESTBED_SHARED_VALUES_H

struct SharedValues {
    const int widthMax;
    const int heightMax;
    const int divisor;
    const short multiplySum;
    const short shiftSum;
    const int targetWidth;
    const int targetHeight;
    const int blurRadius;
    const long availableThreads;
    const bool isResized;

    SharedValues(int widthMax, int heightMax, int divisor, short multiplySum, short shiftSum, int targetWidth, int targetHeight, int blurRadius,
                 long availableThreads, bool isResized) :
            widthMax(widthMax),
            heightMax(heightMax),
            divisor(divisor),
            multiplySum(multiplySum),
            shiftSum(shiftSum),
            targetWidth(targetWidth),
            targetHeight(targetHeight),
            blurRadius(blurRadius),
            availableThreads(availableThreads),
            isResized(isResized) {
    }
};

#endif //TESTBED_SHARED_VALUES_H
