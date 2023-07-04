
cmake_minimum_required(VERSION 3.22.1)


project("image-processor")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17 -Wall")

add_definitions(-DVK_USE_PLATFORM_ANDROID_KHR=1)


add_library(
        image-processor

        SHARED

        image-processor.cpp
        blur.h
        blur.cpp
        blur-manager.h
        blur-manager.cpp
        shared-values.h
        glblur.cpp
        glblur.h
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
        image-processor

        vulkan
        ${log-lib}
        ${jnigraphics-lib}
        ${EGL-lib}
        ${GLESv3-lib}
        ${ANDROID_LIB}
        )
