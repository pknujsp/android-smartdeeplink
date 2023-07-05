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

using namespace std;

static const long availableThreads = sysconf(_SC_NPROCESSORS_ONLN);

class ThreadPool {
public:
    explicit ThreadPool(size_t num_threads);

    ~ThreadPool();

    template<class F, class... Args>
    std::future<typename std::result_of<F(Args...)>::type> enqueueJob(F &&f, Args &&... args) {
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

private:
    size_t num_threads_;
    std::vector<std::thread> worker_threads_;
    std::queue<std::function<void()>> jobs_;
    std::condition_variable cv_job_q_;
    std::mutex m_job_q_;
    bool stop_all;

    void WorkerThread();
};


#endif //TESTBED_THREADPOOL_H
