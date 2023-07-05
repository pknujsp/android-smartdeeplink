//
// Created by jesp on 2023-06-30.
//

#ifndef TESTBED_SHARED_VALUES_H
#define TESTBED_SHARED_VALUES_H

struct SharedValues {
    const int widthMax;
    const int heightMax;
    const int divisor;
    const unsigned short multiplySum;
    const unsigned char shiftSum;
    const int targetWidth;
    const int targetHeight;
    const int blurRadius;
    const long availableThreads;
    const bool isResized;

    SharedValues(int widthMax, int heightMax, int divisor, unsigned short multiplySum, unsigned char shiftSum, int targetWidth, int targetHeight, int
    blurRadius, long availableThreads, bool isResized) :
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

/*
// RGB565 short color = (R & 0x1f) << 11 | (G & 0x3f) << 5 | (B & 0x1f);
// ARGB8888  int color = (A & 0xff) << 24 | (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff);
 */

// RGB565
#define RGB_RED_MASK  0x1f
#define RGB_GREEN_MASK  0x3f
#define RGB_BLUE_MASK  0x1f

#define RGB_RED_SHIFT  11
#define RGB_GREEN_SHIFT  5

#define ARGB_PIXEL_MASK -0x1000000
#define ARGB_BLUE_MASK 0xff
#define ARGB_GREEN_MASK 0xff
#define ARGB_RED_MASK 0xff

#define ARGB_RED_SHIFT 16
#define ARGB_GREEN_SHIFT 8


static const unsigned short MUL_TABLE[] = {512, 512, 456, 512, 328, 456, 335, 512, 405, 328, 271, 456, 388, 335, 292, 512,
                                           454, 405, 364, 328, 298, 271, 496, 456, 420, 388, 360, 335, 312, 292, 273, 512,
                                           482, 454, 428, 405, 383, 364, 345, 328, 312, 298, 284, 271, 259, 496, 475, 456,
                                           437, 420, 404, 388, 374, 360, 347, 335, 323, 312, 302, 292, 282, 273, 265, 512,
                                           497, 482, 468, 454, 441, 428, 417, 405, 394, 383, 373, 364, 354, 345, 337, 328,
                                           320, 312, 305, 298, 291, 284, 278, 271, 265, 259, 507, 496, 485, 475, 465, 456,
                                           446, 437, 428, 420, 412, 404, 396, 388, 381, 374, 367, 360, 354, 347, 341, 335,
                                           329, 323, 318, 312, 307, 302, 297, 292, 287, 282, 278, 273, 269, 265, 261, 512,
                                           505, 497, 489, 482, 475, 468, 461, 454, 447, 441, 435, 428, 422, 417, 411, 405,
                                           399, 394, 389, 383, 378, 373, 368, 364, 359, 354, 350, 345, 341, 337, 332, 328,
                                           324, 320, 316, 312, 309, 305, 301, 298, 294, 291, 287, 284, 281, 278, 274, 271,
                                           268, 265, 262, 259, 257, 507, 501, 496, 491, 485, 480, 475, 470, 465, 460, 456,
                                           451, 446, 442, 437, 433, 428, 424, 420, 416, 412, 408, 404, 400, 396, 392, 388,
                                           385, 381, 377, 374, 370, 367, 363, 360, 357, 354, 350, 347, 344, 341, 338, 335,
                                           332, 329, 326, 323, 320, 318, 315, 312, 310, 307, 304, 302, 299, 297, 294, 292,
                                           289, 287, 285, 282, 280, 278, 275, 273, 271, 269, 267, 265, 263, 261, 259};

static const unsigned char SHR_TABLE[] = {
        9, 11, 12, 13, 13, 14, 14, 15, 15, 15, 15, 16, 16, 16, 16, 17,
        17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 19,
        19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 20, 20, 20,
        20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21,
        21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21,
        21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 22,
        22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
        22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24};


#endif //TESTBED_SHARED_VALUES_H
