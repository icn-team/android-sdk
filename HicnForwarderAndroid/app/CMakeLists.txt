# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)


set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules/")
set_property(GLOBAL PROPERTY USE_FOLDERS ON)
set(CMAKE_FIND_ROOT_PATH "$ENV{DISTILLERY_ROOT_DIR}/usr_${CMAKE_SYSTEM_PROCESSOR};${CMAKE_FIND_ROOT_PATH}")
#set(CMAKE_FIND_ROOT_PATH "/Users/angelomantellini/android_wme/usr_aarch64/;${CMAKE_FIND_ROOT_PATH}")

find_package(OpenSSl REQUIRED)
find_package(LibEvent REQUIRED)
find_package(Libparc REQUIRED)
find_package(Libhicn REQUIRED)
find_package(Hicnlight REQUIRED)
include_directories(${LIBHICNLIGHT_INCLUDE_DIRS})

find_package ( Threads REQUIRED )

find_package ( OpenSSL REQUIRED )
#find_package( Doxygen )

#find_package( HicnFwd REQUIRED)
#include_directories(${LIBHICNFWD_INCLUDE_DIRS})

add_library( # Sets the name of the library.
             forwarderWrap

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/jni/forwarderWrap.c)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       forwarderWrap

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} ${LIBHICNLIGHT_LIBRARIES} ${HICN_LIBRARIES} ${LIBPARC_LIBRARIES} ${LIBEVENT_LIBRARIES} ${OPENSSL_LIBRARIES})