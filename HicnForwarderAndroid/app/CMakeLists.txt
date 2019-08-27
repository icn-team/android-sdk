# Copyright (c) 2019 Cisco and/or its affiliates.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
cmake_minimum_required(VERSION 3.4.1)


set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules/")
set_property(GLOBAL PROPERTY USE_FOLDERS ON)
set(CMAKE_FIND_ROOT_PATH "${CMAKE_CURRENT_SOURCE_DIR}/../../usr_${CMAKE_SYSTEM_PROCESSOR};${CMAKE_FIND_ROOT_PATH}")
find_package(OpenSSL REQUIRED)
find_package(LibEvent REQUIRED)
find_package(Libparc REQUIRED)
find_package(Libhicn REQUIRED)
find_package(Hicnlight REQUIRED)
find_package(Config REQUIRED)
find_package(Libhicnctrl REQUIRED)
find_package(Threads REQUIRED)

include_directories(${LIBHICNLIGHT_INCLUDE_DIRS})

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