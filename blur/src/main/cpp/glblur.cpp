//
// Created by jesp on 2023-07-04.
//

#include "glblur.h"

void StackBlur::processingRow(const SharedValues *const sharedValues, short *imagePixels, const int startRow, const int endRow) {
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

    // RGB565 short color = (R & 0x1f) << 11 | (G & 0x3f) << 5 | (B & 0x1f);
    // ARGB8888  int color = (A & 0xff) << 24 | (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff);
    for (int row = startRow; row <= endRow; row++) {
        sumRed = sumGreen = sumBlue = sumInputRed = sumInputGreen = sumInputBlue = sumOutputRed = sumOutputGreen = sumOutputBlue = 0;
        startPixelIndex = row * targetWidth;
        inPixelIndex = startPixelIndex;
        stackIndex = blurRadius;

        for (int rad = 0; rad <= blurRadius; rad++) {
            stackIndex = rad;
            pixel = imagePixels[startPixelIndex];
            blurStack[stackIndex] = pixel;

            red = ((pixel >> RED_SHIFT) bitand RED_MASK);
            green = ((pixel >> GREEN_SHIFT) bitand GREEN_MASK);
            blue = (pixel bitand BLUE_MASK);

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

                red = ((pixel >> RED_SHIFT) bitand RED_MASK);
                green = ((pixel >> GREEN_SHIFT) bitand GREEN_MASK);
                blue = (pixel bitand BLUE_MASK);

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
                    (short) (((((sumRed * multiplySum) >> shiftSum) bitand RED_MASK) << RED_SHIFT) bitor
                             ((((sumGreen * multiplySum) >> shiftSum) bitand GREEN_MASK) << GREEN_SHIFT) bitor
                             (((sumBlue * multiplySum) >> shiftSum) bitand BLUE_MASK));
            outputPixelIndex++;
            sumRed -= sumOutputRed;
            sumGreen -= sumOutputGreen;
            sumBlue -= sumOutputBlue;

            stackStart = stackPointer + divisor - blurRadius;
            if (stackStart >= divisor) stackStart -= divisor;
            stackIndex = stackStart;

            sumOutputRed -= ((blurStack[stackIndex] >> RED_SHIFT) bitand RED_MASK);
            sumOutputGreen -= ((blurStack[stackIndex] >> GREEN_SHIFT) bitand GREEN_MASK);
            sumOutputBlue -= (blurStack[stackIndex] bitand BLUE_MASK);

            if (colOffset < widthMax) {
                inPixelIndex++;
                colOffset++;
            }

            pixel = imagePixels[inPixelIndex];

            blurStack[stackIndex] = pixel;

            red = ((pixel >> RED_SHIFT) bitand RED_MASK);
            green = ((pixel >> GREEN_SHIFT) bitand GREEN_MASK);
            blue = (pixel bitand BLUE_MASK);

            sumInputRed += red;
            sumInputGreen += green;
            sumInputBlue += blue;

            sumRed += sumInputRed;
            sumGreen += sumInputGreen;
            sumBlue += sumInputBlue;

            if (++stackPointer >= divisor) stackPointer = 0;
            stackIndex = stackPointer;

            pixel = blurStack[stackIndex];

            red = ((pixel >> RED_SHIFT) bitand RED_MASK);
            green = ((pixel >> GREEN_SHIFT) bitand GREEN_MASK);
            blue = (pixel bitand BLUE_MASK);

            sumOutputRed += red;
            sumOutputGreen += green;
            sumOutputBlue += blue;

            sumInputRed -= red;
            sumInputGreen -= green;
            sumInputBlue -= blue;
        }
    }
}

void StackBlur::processingColumn(const SharedValues *const sharedValues, short *imagePixels, const int startColumn, const int endColumn) {
    glGenFramebuffers(1, &frameBuffer);
}

void StackBlur::blur(short *imagePixels, const SharedValues *sharedValues) {

}

SharedValues *StackBlur::init(const int srcWidth, const int srcHeight, const int radius, const double resizeRatio) {
    return nullptr;
}
