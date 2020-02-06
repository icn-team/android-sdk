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
QT_VERSION=5.13.2
QT_VERSION_INSTALL=5132
export BASE_DIR=`pwd`
if [ "$ABI" = "arm64" ]; then
	BASE_PATH=`pwd`
	mkdir -p qt
	cd qt
	rm -rf Qt
	export QT_HOME=`pwd`/Qt_${ABI}
	if [ ! -d qtci ]; then
		git clone https://github.com/benlau/qtci.git
	fi
	if [ ! -d ${QT_HOME}/${QT_VERSION}/android_arm64_v8a ]; then
		export PATH=$PATH:`pwd`/qtci/bin:`pwd`/qtci/recipes
		export QT_CI_PACKAGES=qt.qt5.${QT_VERSION_INSTALL}.android_arm64_v8a,qt.qt5.${QT_VERSION_INSTALL}.qtcharts.android_arm64_v8a,qt.qt5.${QT_VERSION_INSTALL}.qtcharts
		install-qt ${QT_VERSION}
		mv `pwd`/Qt ${QT_HOME}
		rm -rf ${QT_HOME}/Tools
		rm -rf ${QT_HOME}/MaintenanceTool*
		rm -rf ${QT_HOME}/Qt\ Creator*
	fi
	if [ ! -d avbuild ]; then
		git clone https://github.com/wang-bin/avbuild.git
	fi
	if [ ! -d ${BASE_PATH}/usr_aarch64/include/libavcodec ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libavfilter ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libavresample ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libswresample ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libavdevice ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libavformat ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libavutil ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libswscale ]; then
	 	if [ ! -d ffmpeg ]; then
			if [ ! -f ffmpeg.tar.xz ]; then
				wget -O ffmpeg.tar.xz https://www.ffmpeg.org/releases/ffmpeg-4.2.2.tar.xz
			fi
			tar xf ffmpeg.tar.xz
			mv ffmpeg-4.2.2 ffmpeg
		fi
		export FFSRC=$BASE_DIR/qt/ffmpeg
		export NDK_ROOT=$BASE_DIR/sdk/ndk-bundle
		export ANDROID_NDK=$BASE_DIR/sdk/ndk-bundle
		cd $BASE_DIR/qt/avbuild/
		bash avbuild.sh android24 "arm64-clang"
		cp -rf sdk-android-${ABI}-clang/include/* ${BASE_PATH}/usr_aarch64/include/
		cp -f sdk-android-${ABI}-clang/lib/lib* ${BASE_PATH}/usr_aarch64/lib/
	 	touch ${VERSIONS_FILE}
	 	echo ${ABI}_ffmpeg
	 	echo ${VERSIONS_FILE}
	 	${SED} -i "/arm64_ffmpeg/d" ${VERSIONS_FILE}
	 	echo ${ABI}_ffmpeg=4.2 >> ${VERSIONS_FILE}
		cd $BASE_DIR/qt
	fi

	export ANDROID_SDK_ROOT=${SDK}
	export ANDROID_NDK_ROOT=${NDK}
	export PATH=$PATH:${ANDROID_HOME}/tools:${JAVA_HOME}/bin

	if [ ! -d ${QT_HOME}/${QT_VERSION}/android_arm64_v8a/include/QtAV ]; then
    	if [ ! -d QtAV ]; then
       		git clone https://github.com/wang-bin/QtAV.git
       		cd QtAV
			git checkout tags/v1.13.0
			cd ..
    	fi
		cd QtAV
		git submodule update --init

		echo "INCLUDEPATH = ${BASE_PATH}/usr_aarch64/include/" > .qmake.conf
		echo "LIBS = -L${BASE_PATH}/usr_aarch64/lib/" >> .qmake.conf
		mkdir -p ${DISTILLERY_BUILD_DIR}/qtav
		cd ${DISTILLERY_BUILD_DIR}/qtav
		${QT_HOME}/${QT_VERSION}/android_arm64_v8a/bin/qmake $BASE_PATH/qt/QtAV/QtAV.pro -spec android-clang
		make
		make install INSTALL_ROOT=android_arm64_v8a
		sh sdk_install.sh
		QTAV_VERSION=$(git --git-dir=$BASE_PATH/qt/QtAV/.git --work-tree=$BASE_PATH/qt/QtAV/ log -1 --format="%H")
		touch ${VERSIONS_FILE}
		${SED} -i "/${ABI}_QtAV/d" ${VERSIONS_FILE}
		echo ${ABI}_QtAV=${QTAV_VERSION} >> ${VERSIONS_FILE}
	fi
elif [ "$ABI" = "x86_64" ]; then
	BASE_PATH=`pwd`
	mkdir -p qt
	cd qt
	export QT_HOME=`pwd`/Qt_${ABI}
	if [ ! -d qtci ]; then
		git clone https://github.com/benlau/qtci.git
	fi
	echo "${QT_HOME}/${QT_VERSION}/android_x86_64"
	if [ ! -d ${QT_HOME}/${QT_VERSION}/android_x86_64 ]; then
		export PATH=$PATH:`pwd`/qtci/bin:`pwd`/qtci/recipes
		export QT_CI_PACKAGES=qt.qt5.${QT_VERSION_INSTALL}.android_x86_64,qt.qt5.${QT_VERSION_INSTALL}.qtcharts.android_x86_64,qt.qt5.${QT_VERSION_INSTALL}.qtcharts
		install-qt ${QT_VERSION}
		mv `pwd`/Qt ${QT_HOME}
		rm -rf ${QT_HOME}/MaintenanceTool*
		rm -rf ${QT_HOME}/Tools
		rm -rf ${QT_HOME}/Qt\ Creator*
	fi
	if [ ! -d avbuild ]; then
		git clone https://github.com/wang-bin/avbuild.git
	fi
	if [ ! -d ${BASE_PATH}/usr_x86_64/include/libavcodec ] \
	 	|| [ ! -d ${BASE_PATH}/usr_x86_64/include/libavfilter ] \
	 	|| [ ! -d ${BASE_PATH}/usr_x86_64/include/libavresample ] \
	 	|| [ ! -d ${BASE_PATH}/usr_x86_64/include/libswresample ] \
	 	|| [ ! -d ${BASE_PATH}/usr_x86_64/include/libavdevice ] \
	 	|| [ ! -d ${BASE_PATH}/usr_x86_64/include/libavformat ] \
	 	|| [ ! -d ${BASE_PATH}/usr_x86_64/include/libavutil ] \
	 	|| [ ! -d ${BASE_PATH}/usr_x86_64/include/libswscale ]; then
	 	if [ ! -d ffmpeg ]; then
			if [ ! -f ffmpeg.tar.xz ]; then
				wget -O ffmpeg.tar.xz https://www.ffmpeg.org/releases/ffmpeg-4.2.2.tar.xz
			fi
			tar xf ffmpeg.tar.xz
			mv ffmpeg-4.2.2 ffmpeg
		fi
		export FFSRC=$BASE_DIR/qt/ffmpeg
		export NDK_ROOT=$BASE_DIR/sdk/ndk-bundle
		export ANDROID_NDK=$BASE_DIR/sdk/ndk-bundle
		cd $BASE_DIR/qt/avbuild/
		bash avbuild.sh android24 "x86_64-clang"
		cp -rf sdk-android-${ABI}-clang/include/* ${BASE_PATH}/usr_x86_64/include/
		cp -f sdk-android-${ABI}-clang/lib/lib* ${BASE_PATH}/usr_x86_64/lib/
	 	touch ${VERSIONS_FILE}
	 	echo ${ABI}_ffmpeg
	 	echo ${VERSIONS_FILE}
	 	${SED} -i "/x86_64_ffmpeg/d" ${VERSIONS_FILE}
	 	echo ${ABI}_ffmpeg=4.2 >> ${VERSIONS_FILE}
		cd $BASE_DIR/qt
	fi
	export ANDROID_SDK_ROOT=${SDK}
	export ANDROID_NDK_ROOT=${NDK}
	export PATH=$PATH:${ANDROID_HOME}/tools:${JAVA_HOME}/bin
	if [ ! -d ${QT_HOME}/${QT_VERSION}/android_x86/include/QtAV ]; then
    	if [ ! -d QtAV ]; then
       		git clone https://github.com/wang-bin/QtAV.git
       		cd QtAV
			git checkout tags/v1.13.0
			cd ..
    	fi
		cd QtAV
		git submodule update --init
		echo "INCLUDEPATH = ${BASE_PATH}/usr_x86_64/include/" > .qmake.conf
		echo "LIBS = -L${BASE_PATH}/usr_x86_64/lib/" >> .qmake.conf
		mkdir -p ${DISTILLERY_BUILD_DIR}/qtav
		cd ${DISTILLERY_BUILD_DIR}/qtav
		${QT_HOME}/${QT_VERSION}/android_x86_64/bin/qmake $BASE_PATH/qt/QtAV/QtAV.pro -spec android-clang
		make
		make install INSTALL_ROOT=android_x86_64
		sh sdk_install.sh
		QTAV_VERSION=$(git --git-dir=$BASE_PATH/qt/QtAV/.git --work-tree=$BASE_PATH/qt/QtAV/ log -1 --format="%H")
		touch ${VERSIONS_FILE}
		${SED} -i "/${ABI}_QtAV/d" ${VERSIONS_FILE}
		echo ${ABI}_QtAV=${QTAV_VERSION} >> ${VERSIONS_FILE}
	fi
fi
