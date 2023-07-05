//
// Created by jesp on 2023-07-05.
//

#ifndef TESTBED_THREADPOOL_H
#define TESTBED_THREADPOOL_H

#include <sys/sysinfo.h>
#include <vector>
#include <functional>
#include <queue>
#include <thread>
#include <future>
#include <bits/sysconf.h>

class ThreadPool {
public:
    ThreadPool(size_t threadsCount) {
        threads.reserve(threadsCount);
        for (size_t i = 0; i < threadsCount; ++i) {
            threads.emplace_back([this]() { this->WorkerThread(); });
        }
    }

    template<class F, class... Args>
    std::future<typename std::result_of<F(Args...)>::type> enqueueJob(F &&f, Args &&... args) {
        using return_type = typename std::result_of<F(Args...)>::type;
        auto job = std::make_shared<std::packaged_task<return_type()>>(
                [Func = std::forward<F>(f)] { return Func(); });
        std::future<return_type> job_result_future = job->get_future();
        {
            std::lock_guard<std::mutex> lock(mutex);
            jobs.push([job]() { (*job)(); });
        }
        cvJobQueue.notify_one();

        return job_result_future;
    }

    ~ThreadPool() {
        for (auto &t: threads) {
            t.join();
        }
        while (!jobs.empty()) {
            jobs.pop();
        }
    }

private:
    std::vector<std::thread> threads;
    std::queue<std::function<void()>> jobs;
    std::condition_variable cvJobQueue;
    std::mutex mutex;

    void WorkerThread() {
        while (true) {
            std::unique_lock<std::mutex> lock(mutex);
            cvJobQueue.wait(lock, [this]() { return !this->jobs.empty(); });
            if (this->jobs.empty())return;

            std::function<void()> job = std::move(jobs.front());
            jobs.pop();
            lock.unlock();

            job();
        }
    }
};

#endif //TESTBED_THREADPOOL_H
