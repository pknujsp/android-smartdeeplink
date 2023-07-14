cmake_minimum_required(VERSION 3.22.1)

project("GLSurface")

add_definitions(-v -DANDROID -DOC_ARM_ASM)
set(CMAKE_CXX_FLAGS "-Wextra ${CMAKE_CXX_FLAGS} -std=c++17")

add_library(# Sets the name of the library.
        gl-surface
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        glblurringview.h
        glblurringview.cpp
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
        gl-surface

        vulkan
        ${log-lib}
        ${jnigraphics-lib}
        ${EGL-lib}
        ${GLESv3-lib}
        ${ANDROID_LIB}
        )

include(AndroidNdkModules)
android_ndk_import_module_cpufeatures()
