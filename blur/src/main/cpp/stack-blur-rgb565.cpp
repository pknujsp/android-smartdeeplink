//
// Created by jesp on 2023-06-26.
//

#include "blur.h"
#include <GLES3/gl31.h>
#include <GLES3/gl3ext.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <cmath>
#include <unistd.h>
#include <sys/sysinfo.h>
#include <vector>
#include <functional>
#include <queue>
#include <thread>
#include <future>
#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <vulkan/vulkan_core.h>
#include <mutex>
#include "threadpool.h"

#define LOG_TAG "Native Blur"
#define ANDROID_LOG_DEBUG 3
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

static ThreadPool threadPool(availableThreads);


SharedValues *init(const int srcWidth, const int srcHeight, const int radius, const double resizeRatio) {
    const bool resize = resizeRatio > 1.0;
    int targetWidth = resize ? (int) (srcWidth / resizeRatio) : srcWidth;
    int targetHeight = resize ? (int) (srcHeight / resizeRatio) : srcHeight;

    if (targetWidth % 2 != 0) targetWidth--;
    if (targetHeight % 2 != 0) targetHeight--;

    const int widthMax = targetWidth - 1;
    const int heightMax = targetHeight - 1;
    const int newRadius = radius % 2 == 0 ? radius + 1 : radius;

    return new SharedValues{widthMax, heightMax, newRadius * 2 + 1, MUL_TABLE[newRadius], SHR_TABLE[newRadius],
                            targetWidth, targetHeight, newRadius, availableThreads, resize};
}


void processingRow(const SharedValues *const sharedValues, short *imagePixels, const int startRow, const int endRow) {
    long sumRed, sumGreen, sumBlue;
    long sumInputRed, sumInputGreen, sumInputBlue;
    long sumOutputRed, sumOutputGreen, sumOutputBlue;
    int startPixelIndex, inPixelIndex, outputPixelIndex;
    int stackStart, stackPointer, stackIndex;
    int colOffset;

    short red, green, blue;
    int multiplier;

    const int widthMax = sharedValues->widthMax;
    const int blurRadius = sharedValues->blurRadius;
    const int targetWidth = sharedValues->targetWidth;
    const int divisor = sharedValues->divisor;
    const int multiplySum = sharedValues->multiplySum;
    const int shiftSum = sharedValues->shiftSum;

    short blurStack[divisor];
    short pixel;


    for (int row = startRow; row <= endRow; row++) {
        sumRed = sumGreen = sumBlue = sumInputRed = sumInputGreen = sumInputBlue = sumOutputRed = sumOutputGreen = sumOutputBlue = 0;
        startPixelIndex = row * targetWidth;
        inPixelIndex = startPixelIndex;
        stackIndex = blurRadius;

        for (int rad = 0; rad <= blurRadius; rad++) {
            stackIndex = rad;
            pixel = imagePixels[startPixelIndex];
            blurStack[stackIndex] = pixel;

            red = ((pixel >> RGB_RED_SHIFT) bitand RGB_RED_MASK);
            green = ((pixel >> RGB_GREEN_SHIFT) bitand RGB_GREEN_MASK);
            blue = (pixel bitand RGB_BLUE_MASK);

            multiplier = rad + 1;
            sumRed += red * multiplier;
            sumGreen += green * multiplier;
            sumBlue += blue * multiplier;

            sumOutputRed += red;
            sumOutputGreen += green;
            sumOutputBlue += blue;

            if (rad >= 1) {
                if (rad <= widthMax) inPixelIndex++;
                stackIndex = rad + blurRadius;

                pixel = imagePixels[inPixelIndex];
                blurStack[stackIndex] = pixel;

                multiplier = blurRadius + 1 - rad;

                red = ((pixel >> RGB_RED_SHIFT) bitand RGB_RED_MASK);
                green = ((pixel >> RGB_GREEN_SHIFT) bitand RGB_GREEN_MASK);
                blue = (pixel bitand RGB_BLUE_MASK);

                sumRed += red * multiplier;
                sumGreen += green * multiplier;
                sumBlue += blue * multiplier;

                sumInputRed += red;
                sumInputGreen += green;
                sumInputBlue += blue;
            }
        }

        stackStart = blurRadius;
        stackPointer = blurRadius;
        colOffset = blurRadius;
        if (colOffset > widthMax) colOffset = widthMax;
        inPixelIndex = colOffset + row * targetWidth;
        outputPixelIndex = startPixelIndex;

        for (int col = 0; col < targetWidth; col++) {
            imagePixels[outputPixelIndex] =
                    (short) (((((sumRed * multiplySum) >> shiftSum) bitand RGB_RED_MASK) << RGB_RED_SHIFT) bitor
                             ((((sumGreen * multiplySum) >> shiftSum) bitand RGB_GREEN_MASK) << RGB_GREEN_SHIFT) bitor
                             (((sumBlue * multiplySum) >> shiftSum) bitand RGB_BLUE_MASK));
            outputPixelIndex++;
            sumRed -= sumOutputRed;
            sumGreen -= sumOutputGreen;
            sumBlue -= sumOutputBlue;

            stackStart = stackPointer + divisor - blurRadius;
            if (stackStart >= divisor) stackStart -= divisor;
            stackIndex = stackStart;

            sumOutputRed -= ((blurStack[stackIndex] >> RGB_RED_SHIFT) bitand RGB_RED_MASK);
            sumOutputGreen -= ((blurStack[stackIndex] >> RGB_GREEN_SHIFT) bitand RGB_GREEN_MASK);
            sumOutputBlue -= (blurStack[stackIndex] bitand RGB_BLUE_MASK);

            if (colOffset < widthMax) {
                inPixelIndex++;
                colOffset++;
            }

            pixel = imagePixels[inPixelIndex];

            blurStack[stackIndex] = pixel;

            red = ((pixel >> RGB_RED_SHIFT) bitand RGB_RED_MASK);
            green = ((pixel >> RGB_GREEN_SHIFT) bitand RGB_GREEN_MASK);
            blue = (pixel bitand RGB_BLUE_MASK);

            sumInputRed += red;
            sumInputGreen += green;
            sumInputBlue += blue;

            sumRed += sumInputRed;
            sumGreen += sumInputGreen;
            sumBlue += sumInputBlue;

            if (++stackPointer >= divisor) stackPointer = 0;
            stackIndex = stackPointer;

            pixel = blurStack[stackIndex];

            red = ((pixel >> RGB_RED_SHIFT) bitand RGB_RED_MASK);
            green = ((pixel >> RGB_GREEN_SHIFT) bitand RGB_GREEN_MASK);
            blue = (pixel bitand RGB_BLUE_MASK);

            sumOutputRed += red;
            sumOutputGreen += green;
            sumOutputBlue += blue;

            sumInputRed -= red;
            sumInputGreen -= green;
            sumInputBlue -= blue;
        }
    }
}

void processingColumn(const SharedValues *const sharedValues, short *imagePixels, const int startColumn, const int endColumn) {
    const int heightMax = sharedValues->heightMax;
    const int blurRadius = sharedValues->blurRadius;
    const int targetWidth = sharedValues->targetWidth;
    const int targetHeight = sharedValues->targetHeight;
    const int divisor = sharedValues->divisor;
    const int multiplySum = sharedValues->multiplySum;
    const int shiftSum = sharedValues->shiftSum;

    int yOffset, stackStart, stackIndex, stackPointer, sourceIndex, destinationIndex;

    long sumRed, sumGreen, sumBlue, sumInputRed, sumInputGreen, sumInputBlue, sumOutputRed, sumOutputGreen, sumOutputBlue;

    short red, green, blue;
    short blurStack[divisor];
    short pixel;

    for (int col = startColumn; col <= endColumn; col++) {
        sumOutputBlue = sumOutputGreen = sumOutputRed = sumInputBlue = sumInputGreen = sumInputRed = sumBlue = sumGreen = sumRed = 0;
        sourceIndex = col;

        for (int rad = 0; rad <= blurRadius; rad++) {
            stackIndex = rad;
            pixel = imagePixels[sourceIndex];
            blurStack[stackIndex] = pixel;

            red = ((pixel >> RGB_RED_SHIFT) bitand RGB_RED_MASK);
            green = ((pixel >> RGB_GREEN_SHIFT) bitand RGB_GREEN_MASK);
            blue = (pixel bitand RGB_BLUE_MASK);

            int multiplier = rad + 1;

            sumRed += red * multiplier;
            sumGreen += green * multiplier;
            sumBlue += blue * multiplier;

            sumOutputRed += red;
            sumOutputGreen += green;
            sumOutputBlue += blue;

            if (rad >= 1) {
                if (rad <= heightMax) sourceIndex += targetWidth;

                stackIndex = rad + blurRadius;
                pixel = imagePixels[sourceIndex];
                blurStack[stackIndex] = pixel;

                multiplier = blurRadius + 1 - rad;

                red = ((pixel >> RGB_RED_SHIFT) bitand RGB_RED_MASK);
                green = ((pixel >> RGB_GREEN_SHIFT) bitand RGB_GREEN_MASK);
                blue = (pixel bitand RGB_BLUE_MASK);

                sumRed += red * multiplier;
                sumGreen += green * multiplier;
                sumBlue += blue * multiplier;

                sumInputRed += red;
                sumInputGreen += green;
                sumInputBlue += blue;
            }
        }

        stackPointer = blurRadius;
        yOffset = min(blurRadius, heightMax);
        sourceIndex = col + yOffset * targetWidth;
        destinationIndex = col;

        for (int y = 0; y < targetHeight; y++) {
            imagePixels[destinationIndex] =
                    (short) (((((sumRed * multiplySum) >> shiftSum) bitand RGB_RED_MASK) << RGB_RED_SHIFT) bitor (
                            (((sumGreen * multiplySum) >> shiftSum) bitand RGB_GREEN_MASK) << RGB_GREEN_SHIFT) bitor
                             (((sumBlue * multiplySum) >> shiftSum) bitand RGB_BLUE_MASK));

            destinationIndex += targetWidth;
            sumRed -= sumOutputRed;
            sumGreen -= sumOutputGreen;
            sumBlue -= sumOutputBlue;

            stackStart = stackPointer + divisor - blurRadius;
            if (stackStart >= divisor) stackStart -= divisor;
            stackIndex = stackStart;

            pixel = blurStack[stackIndex];

            sumOutputRed -= ((pixel >> RGB_RED_SHIFT) bitand RGB_RED_MASK);
            sumOutputGreen -= ((pixel >> RGB_GREEN_SHIFT) bitand RGB_GREEN_MASK);
            sumOutputBlue -= (pixel bitand RGB_BLUE_MASK);

            if (yOffset < heightMax) {
                sourceIndex += targetWidth;
                yOffset++;
            }

            blurStack[stackIndex] = imagePixels[sourceIndex];

            pixel = imagePixels[sourceIndex];

            sumInputRed += ((pixel >> RGB_RED_SHIFT) bitand RGB_RED_MASK);
            sumInputGreen += ((pixel >> RGB_GREEN_SHIFT) bitand RGB_GREEN_MASK);
            sumInputBlue += (pixel bitand RGB_BLUE_MASK);

            sumRed += sumInputRed;
            sumGreen += sumInputGreen;
            sumBlue += sumInputBlue;

            if (++stackPointer >= divisor) stackPointer = 0;
            stackIndex = stackPointer;

            pixel = blurStack[stackIndex];

            red = ((pixel >> RGB_RED_SHIFT) bitand RGB_RED_MASK);
            green = ((pixel >> RGB_GREEN_SHIFT) bitand RGB_GREEN_MASK);
            blue = (pixel bitand RGB_BLUE_MASK);

            sumOutputRed += red;
            sumOutputGreen += green;
            sumOutputBlue += blue;

            sumInputRed -= red;
            sumInputGreen -= green;
            sumInputBlue -= blue;
        }
    }
}

void blur(short *imagePixels, const SharedValues *sharedValues) {
    const int widthMax = sharedValues->widthMax;
    const int heightMax = sharedValues->heightMax;

    const int rowWorksCount = sharedValues->targetHeight / availableThreads;
    const int columnWorksCount = sharedValues->targetWidth / availableThreads;

    vector<function<void()>> rowWorks;
    vector<function<void()>> columnWorks;

    for (int i = 0; i < availableThreads; i++) {
        int startRow = i * rowWorksCount;
        int endRow = (i + 1) * rowWorksCount - 1;
        if (i == availableThreads - 1) endRow = heightMax;

        rowWorks.emplace_back([sharedValues, imagePixels, startRow, endRow] { return processingRow(sharedValues, imagePixels, startRow, endRow); });

        int startColumn = i * columnWorksCount;
        int endColumn = (i + 1) * columnWorksCount - 1;
        if (i == availableThreads - 1) endColumn = widthMax;

        columnWorks.emplace_back(
                [sharedValues, imagePixels, startColumn, endColumn] { return processingColumn(sharedValues, imagePixels, startColumn, endColumn); });
    }

    std::vector<std::future<void>> futures;

    for (function<void()> &row: rowWorks) {
        futures.emplace_back(threadPool.enqueueJob(row));
    }

    for (future<void> &func: futures) {
        func.wait();
    }

    for (function<void()> &column: columnWorks) {
        futures.emplace_back(threadPool.enqueueJob(column));
    }

    for (future<void> &func: futures) {
        func.wait();
    }
}
