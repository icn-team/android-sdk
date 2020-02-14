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
export QT_HOME=`pwd`/qt/Qt

QT_ABI=`echo "${ANDROID_ABI}" | tr '[:upper:]' '[:lower:]' | tr '-' '_'`
QT_ANDROID=android_${QT_ABI}

mkdir -p qt
cd qt
if [ ! -d ${DISTILLERY_INSTALL_DIR}/include/libavcodec ] \
	|| [ ! -d ${DISTILLERY_INSTALL_DIR}/include/libavfilter ] \
	|| [ ! -d ${DISTILLERY_INSTALL_DIR}/include/libswresample ] \
	|| [ ! -d ${DISTILLERY_INSTALL_DIR}/include/libavformat ] \
	|| [ ! -d ${DISTILLERY_INSTALL_DIR}/include/libavutil ] \
	|| [ ! -d ${DISTILLERY_INSTALL_DIR}/include/libswscale ]; then
	if [ ! -d ffmpeg ]; then
		if [ ! -f ffmpeg.tar.xz ]; then
			wget -O ffmpeg.tar.xz https://sourceforge.net/projects/avbuild/files/android/ffmpeg-4.2-android-lite.tar.xz
		fi
		tar xf ffmpeg.tar.xz
		mv ffmpeg-4.2-android-lite ffmpeg
	fi
	cp -r ffmpeg/include/* ${DISTILLERY_INSTALL_DIR}/include/
	cp ffmpeg/lib/${ANDROID_ABI}/lib* ${DISTILLERY_INSTALL_DIR}/lib/
	touch ${VERSIONS_FILE}
	echo ${ABI}_ffmpeg
	echo ${VERSIONS_FILE}
	${SED} -i "/${ABI}_ffmpeg/d" ${VERSIONS_FILE}
	echo ${ABI}_ffmpeg=4.2 >> ${VERSIONS_FILE}
	cd $BASE_DIR/qt
fi

export ANDROID_SDK_ROOT=${SDK}
export ANDROID_NDK_ROOT=${NDK}
export PATH=$PATH:${ANDROID_HOME}/tools:${JAVA_HOME}/bin

if [ ! -d ${QT_HOME}/${QT_VERSION}/${QT_ANDROID}/include/QtAV ]; then
	if [ ! -d QtAV ]; then
		git clone https://github.com/wang-bin/QtAV.git
		cd QtAV
		git checkout 0307c174a4197fd33b1c1e7d37406d1ee5df6c82
		cd ..
	fi
	cd QtAV
	git submodule update --init

	echo "INCLUDEPATH = ${DISTILLERY_INSTALL_DIR}/include/" > .qmake.conf
	echo "LIBS = -L${DISTILLERY_INSTALL_DIR}/lib/" >> .qmake.conf
	mkdir -p ${DISTILLERY_BUILD_DIR}/qtav
	cd ${DISTILLERY_BUILD_DIR}/qtav
	${QT_HOME}/${QT_VERSION}/${QT_ANDROID}/bin/qmake $BASE_DIR/qt/QtAV/QtAV.pro -spec android-clang
	make
	make install INSTALL_ROOT=${QT_ANDROID}
	sh sdk_install.sh
	QTAV_VERSION=$(git --git-dir=$BASE_DIR/qt/QtAV/.git --work-tree=$BASE_DIR/qt/QtAV/ log -1 --format="%H")
	touch ${VERSIONS_FILE}
	${SED} -i "/${ABI}_QtAV/d" ${VERSIONS_FILE}
	echo ${ABI}_QtAV=${QTAV_VERSION} >> ${VERSIONS_FILE}
fi