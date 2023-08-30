//
// Created by jesp on 2023-07-05.
//

#ifndef TESTBED_ABGR_STACKBLUR_H
#define TESTBED_ABGR_STACKBLUR_H

#include "blur.h"

class ABGRStackBlur : public Blur<unsigned int> {
private:

public:

    void processingRow(unsigned int *imagePixels, const int startRow, const int endRow) override {
        long sumRed, sumGreen, sumBlue;
        long sumInputRed, sumInputGreen, sumInputBlue;
        long sumOutputRed, sumOutputGreen, sumOutputBlue;
        int startPixelIndex, inPixelIndex, outputPixelIndex;
        int stackStart, stackPointer, stackIndex;
        int colOffset;

        unsigned int red, green, blue;
        int multiplier;

        const int widthMax = sharedValues->widthMax;
        const int blurRadius = sharedValues->blurRadius;
        const int targetWidth = sharedValues->targetWidth;
        const int divisor = sharedValues->divisor;
        const int multiplySum = sharedValues->multiplySum;
        const int shiftSum = sharedValues->shiftSum;

        unsigned int blurStack[divisor];
        unsigned int pixel;

        // ABGR
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

                red = ((pixel >> ARGB_RED_SHIFT) bitand ARGB_RED_MASK);
                green = ((pixel >> ARGB_GREEN_SHIFT) bitand ARGB_GREEN_MASK);
                blue = (pixel bitand ARGB_BLUE_MASK);

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

                    red = ((pixel >> ARGB_RED_SHIFT) bitand ARGB_RED_MASK);
                    green = ((pixel >> ARGB_GREEN_SHIFT) bitand ARGB_GREEN_MASK);
                    blue = (pixel bitand ARGB_BLUE_MASK);

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
                        (unsigned int) ((imagePixels[outputPixelIndex] bitand ARGB_PIXEL_MASK) bitor
                                        ((((sumRed * multiplySum) >> shiftSum) bitand ARGB_RED_MASK) << ARGB_RED_SHIFT) bitor
                                        ((((sumGreen * multiplySum) >> shiftSum) bitand ARGB_GREEN_MASK) << ARGB_GREEN_SHIFT) bitor
                                        (((sumBlue * multiplySum) >> shiftSum) bitand ARGB_BLUE_MASK));

                outputPixelIndex++;
                sumRed -= sumOutputRed;
                sumGreen -= sumOutputGreen;
                sumBlue -= sumOutputBlue;

                stackStart = stackPointer + divisor - blurRadius;
                if (stackStart >= divisor) stackStart -= divisor;
                stackIndex = stackStart;

                sumOutputRed -= ((blurStack[stackIndex] >> ARGB_RED_SHIFT) bitand ARGB_RED_MASK);
                sumOutputGreen -= ((blurStack[stackIndex] >> ARGB_GREEN_SHIFT) bitand ARGB_GREEN_MASK);
                sumOutputBlue -= (blurStack[stackIndex] bitand ARGB_BLUE_MASK);

                if (colOffset < widthMax) {
                    inPixelIndex++;
                    colOffset++;
                }

                pixel = imagePixels[inPixelIndex];

                blurStack[stackIndex] = pixel;

                red = ((pixel >> ARGB_RED_SHIFT) bitand ARGB_RED_MASK);
                green = ((pixel >> ARGB_GREEN_SHIFT) bitand ARGB_GREEN_MASK);
                blue = (pixel bitand ARGB_BLUE_MASK);

                sumInputRed += red;
                sumInputGreen += green;
                sumInputBlue += blue;

                sumRed += sumInputRed;
                sumGreen += sumInputGreen;
                sumBlue += sumInputBlue;

                if (++stackPointer >= divisor) stackPointer = 0;
                stackIndex = stackPointer;

                pixel = blurStack[stackIndex];

                red = ((pixel >> ARGB_RED_SHIFT) bitand ARGB_RED_MASK);
                green = ((pixel >> ARGB_GREEN_SHIFT) bitand ARGB_GREEN_MASK);
                blue = (pixel bitand ARGB_BLUE_MASK);

                sumOutputRed += red;
                sumOutputGreen += green;
                sumOutputBlue += blue;

                sumInputRed -= red;
                sumInputGreen -= green;
                sumInputBlue -= blue;
            }
        }
    }

    void processingColumn(unsigned int *imagePixels, const int startColumn, const int endColumn) override {
        const int heightMax = sharedValues->heightMax;
        const int blurRadius = sharedValues->blurRadius;
        const int targetWidth = sharedValues->targetWidth;
        const int targetHeight = sharedValues->targetHeight;
        const int divisor = sharedValues->divisor;
        const int multiplySum = sharedValues->multiplySum;
        const int shiftSum = sharedValues->shiftSum;

        int yOffset, stackStart, stackIndex, stackPointer, sourceIndex, destinationIndex;

        long sumRed, sumGreen, sumBlue, sumInputRed, sumInputGreen, sumInputBlue, sumOutputRed, sumOutputGreen, sumOutputBlue;

        unsigned int red, green, blue;
        unsigned int blurStack[divisor];
        unsigned int pixel;

        for (int col = startColumn; col <= endColumn; col++) {
            sumOutputBlue = sumOutputGreen = sumOutputRed = sumInputBlue = sumInputGreen = sumInputRed = sumBlue = sumGreen = sumRed = 0;
            sourceIndex = col;

            for (int rad = 0; rad <= blurRadius; rad++) {
                stackIndex = rad;
                pixel = imagePixels[sourceIndex];
                blurStack[stackIndex] = pixel;

                red = ((pixel >> ARGB_RED_SHIFT) bitand ARGB_RED_MASK);
                green = ((pixel >> ARGB_GREEN_SHIFT) bitand ARGB_GREEN_MASK);
                blue = (pixel bitand ARGB_BLUE_MASK);

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

                    red = ((pixel >> ARGB_RED_SHIFT) bitand ARGB_RED_MASK);
                    green = ((pixel >> ARGB_GREEN_SHIFT) bitand ARGB_GREEN_MASK);
                    blue = (pixel bitand ARGB_BLUE_MASK);

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
                        (unsigned int) ((imagePixels[destinationIndex] bitand ARGB_PIXEL_MASK) bitor
                                        ((((sumRed * multiplySum) >> shiftSum) bitand ARGB_RED_MASK) << ARGB_RED_SHIFT) bitor
                                        ((((sumGreen * multiplySum) >> shiftSum) bitand ARGB_GREEN_MASK) << ARGB_GREEN_SHIFT) bitor
                                        ((((sumBlue * multiplySum) >> shiftSum) bitand ARGB_BLUE_MASK)));

                destinationIndex += targetWidth;
                sumRed -= sumOutputRed;
                sumGreen -= sumOutputGreen;
                sumBlue -= sumOutputBlue;

                stackStart = stackPointer + divisor - blurRadius;
                if (stackStart >= divisor) stackStart -= divisor;
                stackIndex = stackStart;

                pixel = blurStack[stackIndex];

                sumOutputRed -= ((pixel >> ARGB_RED_SHIFT) bitand ARGB_RED_MASK);
                sumOutputGreen -= ((pixel >> ARGB_GREEN_SHIFT) bitand ARGB_GREEN_MASK);
                sumOutputBlue -= (pixel bitand ARGB_BLUE_MASK);

                if (yOffset < heightMax) {
                    sourceIndex += targetWidth;
                    yOffset++;
                }

                blurStack[stackIndex] = imagePixels[sourceIndex];

                pixel = imagePixels[sourceIndex];

                sumInputRed += ((pixel >> ARGB_RED_SHIFT) bitand ARGB_RED_MASK);
                sumInputGreen += ((pixel >> ARGB_GREEN_SHIFT) bitand ARGB_GREEN_MASK);
                sumInputBlue += (pixel bitand ARGB_BLUE_MASK);

                sumRed += sumInputRed;
                sumGreen += sumInputGreen;
                sumBlue += sumInputBlue;

                if (++stackPointer >= divisor) stackPointer = 0;
                stackIndex = stackPointer;

                pixel = blurStack[stackIndex];

                red = ((pixel >> ARGB_RED_SHIFT) bitand ARGB_RED_MASK);
                green = ((pixel >> ARGB_GREEN_SHIFT) bitand ARGB_GREEN_MASK);
                blue = (pixel bitand ARGB_BLUE_MASK);

                sumOutputRed += red;
                sumOutputGreen += green;
                sumOutputBlue += blue;

                sumInputRed -= red;
                sumInputGreen -= green;
                sumInputBlue -= blue;
            }
        }
    }

};


#endif //TESTBED_ABGR_STACKBLUR_H
