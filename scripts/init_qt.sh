 #############################################################################
 # Copyright (c) 2017 Cisco and/or its affiliates.
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
 ##############################################################################

#!/bin/bash

set -ex

if [ "$ABI" = "arm64" ]; then
	TOOLCHAIN=`pwd`/sdk/toolchain
	BASE_PATH=`pwd`
	mkdir -p qt
	cd qt
	export QT_HOME=`pwd`/Qt
	if [ ! -d ${QT_HOME} ]; then
	    if [ ! -d qtci ]; then
			git clone https://github.com/benlau/qtci.git
		fi
		export PATH=$PATH:`pwd`/qtci/bin:`pwd`/qtci/recipes
		export QT_CI_PACKAGES=qt.qt5.5123.android_arm64_v8a,qt.qt5.5123.qtcharts.android_arm64_v8a,qt.qt5.5123.qtcharts,qt.qt5.5123.qtcharts.clang_64
		install-qt 5.12.3
	fi
	if [ ! -d ffmpeg ]; then
		if [ ! -f ffmpeg-master-android-clang.tar.xz ]; then
			wget https://iweb.dl.sourceforge.net/project/avbuild/android/ffmpeg-master-android-clang.tar.xz
		fi
	
		tar xf ffmpeg-master-android-clang.tar.xz
		mv ffmpeg-master-android-clang ffmpeg
		cp -r ffmpeg/include/ ${BASE_PATH}/usr_aarch64/include/
		cp ffmpeg/lib/arm64-v8a/lib* ${BASE_PATH}/usr_aarch64/lib/
	fi 

	export ANDROID_SDK_ROOT=${SDK}
	export ANDROID_NDK_ROOT=${NDK}
	export PATH=$PATH:${ANDROID_HOME}/tools:${JAVA_HOME}/bin

	if [ ! -d ${QT_HOME}/5.12.3/android_arm64_v8a/include/QtAV ]; then
    	if [ ! -d QtAV ]; then
       		git clone https://github.com/wang-bin/QtAV.git
    	fi
		cd QtAV
		git submodule update --init

		echo "INCLUDEPATH = ${BASE_PATH}/usr_aarch64/include/" > .qmake.conf
		echo "LIBS = -L${BASE_PATH}/usr_aarch64/lib/" >> .qmake.conf
		mkdir -p ${DISTILLERY_BUILD_DIR}/qtav
		cd ${DISTILLERY_BUILD_DIR}/qtav
		${QT_HOME}/5.12.3/android_arm64_v8a/bin/qmake $BASE_PATH/qt/QtAV/QtAV.pro -spec android-clang 
		make
		make install INSTALL_ROOT=android_arm64_v8a
		sh sdk_install.sh
	fi
fi
