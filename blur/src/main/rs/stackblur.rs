#pragma version(1)
#pragma rs_fp_inprecise
#pragma rs java_package_name(io.github.pknujsp.blur)

rs_allocation gIn;
rs_allocation gOut;

float gRadius;

#define MAX_RADIUS 25
float gGaussianKernel[MAX_RADIUS];

void createGaussianKernel() {
    float sum = 0.0f;
    float sigma = gRadius / 2.0f;
    float twoSigmaSquare = 2.0f * sigma * sigma;
    float rootTwoPiSigma = sqrt(2.0f * M_PI) * sigma;

    for (int i = 0; i < gRadius; i++) {
        float x = i - gRadius / 2.0f;
        gGaussianKernel[i] = exp(-(x * x) / twoSigmaSquare) / rootTwoPiSigma;
        sum += gGaussianKernel[i];
    }

    for (int i = 0; i < gRadius; i++) {
        gGaussianKernel[i] /= sum;
    }
}

void __attribute__((kernel)) gaussianBlur(uchar4 in, uint32_t x, uint32_t y) {
    float4 sum = {0.0f, 0.0f, 0.0f, 0.0f};

    for (int i = 0; i < gRadius; i++) {
        int offset = i - gRadius / 2;
        uchar4 pixel = rsGetElementAt_uchar4(gIn, x + offset, y + offset);
        float4 floatPixel = convert_float4(pixel);
        sum += floatPixel * gGaussianKernel[i];
    }

    rsSetElementAt_uchar4(gOut, convert_uchar4(sum), x, y);
}
