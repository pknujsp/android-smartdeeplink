//
// Created by jesp on 2023-06-26.
//

#ifndef TESTBED_BLUR_H
#define TESTBED_BLUR_H

#include "shared-values.h"
#include <vector>
#include <functional>
#include <queue>
#include <thread>
#include <future>
#include <android/log.h>
#include <cmath>
#include <unistd.h>
#include <sys/sysinfo.h>
#include <mutex>
#include "threadpool.h"

using namespace std;

#define TAG "Blur"
#define ANDROID_LOG_DEBUG 3
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

// RGB565 or ARGB8888
template<typename T>
class Blur {
private:
    ThreadPool *threadPool;

protected:
    SharedValues *sharedValues;
public:

    virtual void processingRow(T *imagePixels, const int startRow, const int endRow) = 0;

    virtual void processingColumn(T *imagePixels, const int startColumn, const int endColumn) = 0;

    ~Blur() {
        delete threadPool;
        delete sharedValues;
    }

    void onDestroy() {
        delete sharedValues;
    }

    void blur(T *imagePixels) {
        const int widthMax = sharedValues->widthMax;
        const int heightMax = sharedValues->heightMax;
        const long threads = sharedValues->availableThreads;

        const int rowWorksCount = sharedValues->targetHeight / threads;
        const int columnWorksCount = sharedValues->targetWidth / threads;

        vector<function<void()>> rowWorks;
        vector<function<void()>> columnWorks;

        for (int i = 0; i < threads; i++) {
            int startRow = i * rowWorksCount;
            int endRow = (i + 1) * rowWorksCount - 1;
            if (i == threads - 1) endRow = heightMax;

            rowWorks.emplace_back(
                    [imagePixels, startRow, endRow, this] { return processingRow(imagePixels, startRow, endRow); });

            int startColumn = i * columnWorksCount;
            int endColumn = (i + 1) * columnWorksCount - 1;
            if (i == threads - 1) endColumn = widthMax;

            columnWorks.emplace_back(
                    [imagePixels, startColumn, endColumn, this] { return processingColumn(imagePixels, startColumn, endColumn); });
        }

        std::vector<std::future<void>> futures;

        for (function<void()> &row: rowWorks) {
            futures.emplace_back(threadPool->enqueueJob(row));
        }

        for (future<void> &func: futures) {
            func.wait();
        }

        for (function<void()> &column: columnWorks) {
            futures.emplace_back(threadPool->enqueueJob(column));
        }

        for (future<void> &func: futures) {
            func.wait();
        }
    }

    SharedValues *prepare(const int srcWidth, const int srcHeight, const int radius, const double resizeRatio) {
        const bool resize = resizeRatio > 1.0;
        int targetWidth = resize ? (int) (srcWidth / resizeRatio) : srcWidth;
        int targetHeight = resize ? (int) (srcHeight / resizeRatio) : srcHeight;

        if (targetWidth % 2 != 0) targetWidth--;
        if (targetHeight % 2 != 0) targetHeight--;

        const int widthMax = targetWidth - 1;
        const int heightMax = targetHeight - 1;
        const int newRadius = radius % 2 == 0 ? radius + 1 : radius;


        long threads = sysconf(_SC_NPROCESSORS_ONLN);
        threadPool = new ThreadPool(threads);
        LOGD("threads : %ld", threads);

        sharedValues = new SharedValues{widthMax, heightMax, newRadius * 2 + 1, MUL_TABLE[newRadius], SHR_TABLE[newRadius],
                                        targetWidth, targetHeight, newRadius, threads, resize};
        return sharedValues;
    }
};

#endif //TESTBED_BLUR_H
