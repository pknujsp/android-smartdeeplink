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

#define LOG_TAG "Native Blur"
#define ANDROID_LOG_DEBUG 3
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

using namespace std;

static const long availableThreads = sysconf(_SC_NPROCESSORS_ONLN);

class ThreadPool {
public:
    explicit ThreadPool(size_t num_threads);

    ~ThreadPool();

    template<class F, class... Args>
    std::future<typename std::result_of<F(Args...)>::type> enqueueJob(F &&f, Args &&... args) {
        if (stop_all) {
            LOGD("ThreadPool 사용 중지됨");
            throw std::runtime_error("ThreadPool 사용 중지됨");
        }

        using return_type = typename std::result_of<F(Args...)>::type;
        auto job = std::make_shared<std::packaged_task<return_type()>>(
                [Func = std::forward<F>(f)] { return Func(); });
        std::future<return_type> job_result_future = job->get_future();
        {
            std::lock_guard<std::mutex> lock(m_job_q_);
            jobs_.push([job]() { (*job)(); });
        }
        cv_job_q_.notify_one();

        return job_result_future;
    }

private:
    size_t num_threads_;
    std::vector<std::thread> worker_threads_;
    std::queue<std::function<void()>> jobs_;
    std::condition_variable cv_job_q_;
    std::mutex m_job_q_;
    bool stop_all;

    void WorkerThread();
};

ThreadPool::ThreadPool(size_t num_threads)
        : num_threads_(num_threads), stop_all(false) {
    worker_threads_.reserve(num_threads_);
    for (size_t i = 0; i < num_threads_; ++i) {
        worker_threads_.emplace_back([this]() { this->WorkerThread(); });
    }
}

void ThreadPool::WorkerThread() {
    while (true) {
        std::unique_lock<std::mutex> lock(m_job_q_);
        cv_job_q_.wait(lock, [this]() { return !this->jobs_.empty() || stop_all; });
        if (stop_all && this->jobs_.empty())return;

        std::function<void()> job = std::move(jobs_.front());
        jobs_.pop();
        lock.unlock();

        job();
    }
}

ThreadPool::~ThreadPool() {
    stop_all = true;
    cv_job_q_.notify_all();

    for (auto &t: worker_threads_) {
        t.join();
    }
}


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

            red = ((pixel >> RED_SHIFT) bitand RED_MASK);
            green = ((pixel >> GREEN_SHIFT) bitand GREEN_MASK);
            blue = (pixel bitand BLUE_MASK);

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

        stackPointer = blurRadius;
        yOffset = min(blurRadius, heightMax);
        sourceIndex = col + yOffset * targetWidth;
        destinationIndex = col;

        for (int y = 0; y < targetHeight; y++) {
            imagePixels[destinationIndex] =
                    (short) (((((sumRed * multiplySum) >> shiftSum) bitand RED_MASK) << RED_SHIFT) bitor (
                            (((sumGreen * multiplySum) >> shiftSum) bitand GREEN_MASK) << GREEN_SHIFT) bitor
                             (((sumBlue * multiplySum) >> shiftSum) bitand BLUE_MASK));

            destinationIndex += targetWidth;
            sumRed -= sumOutputRed;
            sumGreen -= sumOutputGreen;
            sumBlue -= sumOutputBlue;

            stackStart = stackPointer + divisor - blurRadius;
            if (stackStart >= divisor) stackStart -= divisor;
            stackIndex = stackStart;

            pixel = blurStack[stackIndex];

            sumOutputRed -= ((pixel >> RED_SHIFT) bitand RED_MASK);
            sumOutputGreen -= ((pixel >> GREEN_SHIFT) bitand GREEN_MASK);
            sumOutputBlue -= (pixel bitand BLUE_MASK);

            if (yOffset < heightMax) {
                sourceIndex += targetWidth;
                yOffset++;
            }

            blurStack[stackIndex] = imagePixels[sourceIndex];

            pixel = imagePixels[sourceIndex];

            sumInputRed += ((pixel >> RED_SHIFT) bitand RED_MASK);
            sumInputGreen += ((pixel >> GREEN_SHIFT) bitand GREEN_MASK);
            sumInputBlue += (pixel bitand BLUE_MASK);

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
