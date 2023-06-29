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

#define LOG_TAG "Native Blur"
#define ANDROID_LOG_DEBUG 3
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

using namespace std;

namespace ThreadPool {
    class ThreadPool {
    public:
        explicit ThreadPool(size_t num_threads);

        ~ThreadPool();

        // job 을 추가한다.
        template<class F, class... Args>
        std::future<typename std::result_of<F(Args...)>::type> EnqueueJob(
                F &&f, Args &&... args);

    private:
        // 총 Worker 쓰레드의 개수.
        size_t num_threads_;
        // Worker 쓰레드를 보관하는 벡터.
        std::vector<std::thread> worker_threads_;
        // 할일들을 보관하는 job 큐.
        std::queue<std::function<void()>> jobs_;
        // 위의 job 큐를 위한 cv 와 m.
        std::condition_variable cv_job_q_;
        std::mutex m_job_q_;

        // 모든 쓰레드 종료
        bool stop_all;

        // Worker 쓰레드
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
            if (stop_all && this->jobs_.empty()) {
                return;
            }

            // 맨 앞의 job 을 뺀다.
            std::function<void()> job = std::move(jobs_.front());
            jobs_.pop();
            lock.unlock();

            // 해당 job 을 수행한다 :)
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

    template<class F, class... Args>
    std::future<typename std::result_of<F(Args...)>::type> ThreadPool::EnqueueJob(
            F &&f, Args &&... args) {
        if (stop_all) {
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

}

void dim(u_short *imagePixels, const int width, const int height, const int dimFactor) {
    const long availableThreads = sysconf(_SC_NPROCESSORS_ONLN);

    const int rowWorksCount = height / availableThreads;
    vector<function<void()>> rowWorks;

    const double factor = 1.0 - dimFactor / 100.0;

    for (int i = 0; i < availableThreads; i++) {
        const int startRow = i * rowWorksCount;
        int endRow = (i + 1) * rowWorksCount - 1;
        if (i == availableThreads - 1) endRow = height - 1;

        rowWorks.emplace_back([factor, imagePixels, width, startRow, endRow] {
            unsigned short pixel, red, green, blue;

            for (int i = width * startRow; i < width * endRow + width; i++) {
                pixel = imagePixels[i];

                red = (unsigned short) (((pixel >> RED_SHIFT) & RED_MASK) * factor);
                green = (unsigned short) (((pixel >> GREEN_SHIFT) & GREEN_MASK) * factor);
                blue = (unsigned short) ((pixel & BLUE_MASK) * factor);

                imagePixels[i] =
                        (unsigned short) (((red bitand RED_SHIFT) << RED_MASK)
                                          bitor ((green bitand GREEN_SHIFT) << GREEN_MASK)
                                          bitor (blue bitand BLUE_MASK));
            }
            return;
        });
    }

    ThreadPool::ThreadPool pool(availableThreads);
    std::vector<std::future<void>> futures;

    for (const auto &row: rowWorks) {
        futures.emplace_back(pool.EnqueueJob(row));
    }

    for (auto &f: futures) {
        f.wait();
    }
}

void processingRow(const SharedValues *const sharedValues, unsigned short *imagePixels, const int startRow, const int endRow) {
    long sumRed, sumGreen, sumBlue;
    long sumInputRed, sumInputGreen, sumInputBlue;
    long sumOutputRed, sumOutputGreen, sumOutputBlue;
    int startPixelIndex, inPixelIndex, outputPixelIndex;
    int stackStart, stackPointer, stackIndex;
    int colOffset;

    unsigned short red, green, blue;
    int multiplier;

    const int widthMax = sharedValues->widthMax;
    const int blurRadius = sharedValues->blurRadius;
    const int targetWidth = sharedValues->targetWidth;
    const int divisor = sharedValues->divisor;
    const int multiplySum = sharedValues->multiplySum;
    const int shiftSum = sharedValues->shiftSum;

    unsigned short blurStack[divisor];
    unsigned short pixel;

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

            red = ((pixel >> RED_SHIFT) & RED_MASK);
            green = ((pixel >> GREEN_SHIFT) & GREEN_MASK);
            blue = (pixel & BLUE_MASK);

            multiplier = rad - 1;
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

                red = ((pixel >> RED_SHIFT) & RED_MASK);
                green = ((pixel >> GREEN_SHIFT) & GREEN_MASK);
                blue = (pixel & BLUE_MASK);

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
                    (unsigned short) ((imagePixels[outputPixelIndex] bitand PIXEL_MASK) bitor
                                      ((((sumRed * multiplySum) >> shiftSum) bitand RED_MASK) << RED_SHIFT) bitor
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

void processingColumn(const SharedValues *const sharedValues, unsigned short *imagePixels, const int startColumn, const int endColumn) {
    const int heightMax = sharedValues->heightMax;
    const int blurRadius = sharedValues->blurRadius;
    const int targetWidth = sharedValues->targetWidth;
    const int targetHeight = sharedValues->targetHeight;
    const int divisor = sharedValues->divisor;
    const int multiplySum = sharedValues->multiplySum;
    const int shiftSum = sharedValues->shiftSum;

    int xOffset, yOffset, blurStackIndex, stackStart, stackIndex, stackPointer, sourceIndex, destinationIndex;

    long sumRed, sumGreen, sumBlue, sumInputRed, sumInputGreen, sumInputBlue, sumOutputRed, sumOutputGreen, sumOutputBlue;

    unsigned short red, green, blue;
    unsigned short blurStack[divisor];
    unsigned short pixel;

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
                    (unsigned short) ((imagePixels[destinationIndex] bitand PIXEL_MASK) bitor
                                      ((((sumRed * multiplySum) >> shiftSum) bitand RED_MASK) << RED_SHIFT) bitor (
                                              (((sumGreen * multiplySum) >> shiftSum) bitand GREEN_MASK) << GREEN_SHIFT) bitor
                                      (((sumBlue * multiplySum) >> shiftSum) bitand BLUE_MASK));

            destinationIndex += targetWidth;
            sumRed -= sumOutputRed;
            sumGreen -= sumOutputGreen;
            sumBlue -= sumOutputBlue;

            stackStart = stackPointer + divisor - blurRadius;
            if (stackStart >= divisor) stackStart -= divisor;
            stackIndex = stackStart;

            sumOutputRed -= ((blurStack[stackIndex] >> RED_SHIFT) bitand RED_MASK);
            sumOutputGreen -= ((blurStack[stackIndex] >> GREEN_SHIFT) bitand GREEN_MASK);
            sumOutputBlue -= (blurStack[stackIndex] bitand BLUE_MASK);

            if (yOffset < heightMax) {
                sourceIndex += targetWidth;
                yOffset++;
            }

            blurStack[stackIndex] = imagePixels[sourceIndex];

            sumInputRed += ((imagePixels[sourceIndex] >> RED_SHIFT) bitand RED_MASK);
            sumInputGreen += ((imagePixels[sourceIndex] >> GREEN_SHIFT) bitand GREEN_MASK);
            sumInputBlue += (imagePixels[sourceIndex] bitand BLUE_MASK);

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

void blur(unsigned short *imagePixels, const int radius, const int targetWidth, const int targetHeight) {
    const int widthMax = targetWidth - 1;
    const int heightMax = targetHeight - 1;
    const int newRadius = radius % 2 == 0 ? radius + 1 : radius;

    const SharedValues *sharedValues = new SharedValues{widthMax, heightMax, newRadius * 2 + 1, MUL_TABLE[newRadius], SHR_TABLE[newRadius],
                                                        targetWidth,
                                                        targetHeight, newRadius};

    const long availableThreads = sysconf(_SC_NPROCESSORS_ONLN);

    const int rowWorksCount = targetHeight / availableThreads;
    const int columnWorksCount = targetWidth / availableThreads;

    vector<function<void()>> rowWorks;
    vector<function<void()>> columnWorks;

    for (int i = 0; i < availableThreads; i++) {
        const int startRow = i * rowWorksCount;
        int endRow = (i + 1) * rowWorksCount - 1;
        if (i == availableThreads - 1) endRow = heightMax;
        rowWorks.emplace_back([sharedValues, imagePixels, startRow, endRow] { return processingRow(sharedValues, imagePixels, startRow, endRow); });
    }

    for (int i = 0; i < availableThreads; i++) {
        int startColumn = i * columnWorksCount;
        int endColumn = (i + 1) * columnWorksCount - 1;
        if (i == availableThreads - 1) endColumn = widthMax;
        columnWorks.emplace_back(
                [sharedValues, imagePixels, startColumn, endColumn] { return processingColumn(sharedValues, imagePixels, startColumn, endColumn); });
    }

    ThreadPool::ThreadPool pool(availableThreads);
    std::vector<std::future<void>> futures;

    for (const auto &row: rowWorks) {
        futures.emplace_back(pool.EnqueueJob(row));
    }

    for (auto &f: futures) {
        f.wait();
    }

    for (const auto &column: columnWorks) {
        futures.emplace_back(pool.EnqueueJob(column));
    }

    for (auto &f: futures) {
        f.wait();
    }

    delete sharedValues;
}
