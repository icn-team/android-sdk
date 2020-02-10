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
ABI=$1
INSTALLATION_DIR=$2
OS=`echo $OS | tr '[:upper:]' '[:lower:]'`
export BASE_DIR=`pwd`
mkdir -p ${INSTALLATION_DIR}
mkdir -p ${INSTALLATION_DIR}/include

if [ "$ABI" = "arm64" ]; then
	if [ ! -d ${BASE_PATH}/usr_aarch64/include/libavcodec ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libavfilter ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libavresample ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libswresample ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libavdevice ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libavformat ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libavutil ] \
	 	|| [ ! -d ${BASE_PATH}/usr_aarch64/include/libswscale ]; then
		cd qt
	 	if [ ! -d ffmpeg-prebuilt ]; then
			if [ ! -f ffmpeg-prebuilt.tar.xz ]; then
				wget -O ffmpeg-prebuilt.tar.xz https://iweb.dl.sourceforge.net/project/avbuild/android/ffmpeg-4.2-android-clang.tar.xz
			fi
			tar xf ffmpeg-prebuilt.tar.xz
			mv ffmpeg-4.2-android-clang ffmpeg-prebuilt
		fi
		cp -r ffmpeg-prebuilt/include/* ${INSTALLATION_DIR}/include/
		cp ffmpeg-prebuilt/lib/arm64-v8a/lib* ${INSTALLATION_DIR}/lib/
		${SED} -i "/${ABI}_ffmpeg/d" ${VERSIONS_FILE}
		echo ${ABI}_ffmpeg=4.2 >> ${VERSIONS_FILE}
		cd ..
	fi
else
	if [ ! -d ${INSTALLATION_DIR}/include/libavcodec ] \
			|| [ ! -d ${INSTALLATION_DIR}/include/libavfilter ] \
			|| [ ! -d ${INSTALLATION_DIR}/include/libavresample ] \
			|| [ ! -d ${INSTALLATION_DIR}/include/libswresample ] \
			|| [ ! -d ${INSTALLATION_DIR}/include/libavdevice ] \
			|| [ ! -d ${INSTALLATION_DIR}/include/libavformat ] \
			|| [ ! -d ${INSTALLATION_DIR}/include/libavutil ] \
			|| [ ! -d ${INSTALLATION_DIR}/include/libswscale ]; then

		export FFSRC=$BASE_DIR/src/ffmpeg
		export NDK_ROOT=$BASE_DIR/sdk/ndk-bundle
		export ANDROID_NDK=$BASE_DIR/sdk/ndk-bundle
		cd $BASE_DIR/qt/avbuild/
		bash avbuild.sh android24 "${ABI}-clang"
		cp -rf sdk-android-${ABI}-clang/include/* ${INSTALLATION_DIR}/include/
		cp -f sdk-android-${ABI}-clang/lib/lib* ${INSTALLATION_DIR}/lib/
		touch ${VERSIONS_FILE}
		${SED} -i "/${ABI}_ffmpeg/d" ${VERSIONS_FILE}
		echo ${ABI}_ffmpeg=4.2 >> ${VERSIONS_FILE}
		
		cd ..
	fi
fi