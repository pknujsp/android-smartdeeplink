cmake_minimum_required(VERSION 3.22.1)

project("image-processor")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17 -Wall")

add_definitions(-DVK_USE_PLATFORM_ANDROID_KHR=1)

add_library(
        stack-blur

        SHARED

        stackblur/blur.h
        stackblur/ABGR-StackBlur.cpp
        stackblur/abgr-stackblur.h
        stackblur/shared-values.h
        stackblur/threadpool.cpp
        stackblur/threadpool.h
        stackblur/RGB-StackBlur.cpp
        BlurManager.cpp
        BlurManager.h
)


add_library(
        gl-blurring

        SHARED

        glblurringview.cpp
        glblurringview.h
        stackblur/shared-values.h
)

find_library(
        log-lib
        log
)

find_library(
        jnigraphics-lib
        jnigraphics
)

find_library(EGL-lib EGL)
find_library(GLESv3-lib GLESv3)
find_library(ANDROID_LIB android)

target_link_libraries( # Specifies the target library.
        stack-blur

        ${log-lib}
        ${jnigraphics-lib}
        ${EGL-lib}
        ${GLESv3-lib}
        ${ANDROID_LIB}
        )


target_link_libraries( # Specifies the target library.
        gl-blurring

        vulkan
        stack-blur
        ${log-lib}
        ${jnigraphics-lib}
        ${EGL-lib}
        ${GLESv3-lib}
        ${ANDROID_LIB}
        )
