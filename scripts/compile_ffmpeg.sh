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

if [ ! -d ${INSTALLATION_DIR}/include/libavcodec ] \
	|| [ ! -d ${INSTALLATION_DIR}/include/libavfilter ] \
	|| [ ! -d ${INSTALLATION_DIR}/include/libswresample ] \
	|| [ ! -d ${INSTALLATION_DIR}/include/libavformat ] \
	|| [ ! -d ${INSTALLATION_DIR}/include/libavutil ] \
	|| [ ! -d ${INSTALLATION_DIR}/include/libswscale ]; then
	if [ ! -d ffmpeg ]; then
		if [ ! -f ffmpeg-prebuilt.tar.xz ]; then
			wget -O ffmpeg.tar.xz https://sourceforge.net/projects/avbuild/files/android/ffmpeg-4.2-android-lite.tar.xz
		fi
		tar xf ffmpeg.tar.xz
		mv ffmpeg-4.2-android-lite ffmpeg
	fi
	cp -r ffmpeg/include/* ${INSTALLATION_DIR}/include/
    cp ffmpeg/lib/${ANDROID_ABI}/lib* ${INSTALLATION_DIR}/lib/
	touch ${VERSIONS_FILE}
	${SED} -i "/${ABI}_ffmpeg/d" ${VERSIONS_FILE}
	echo ${ABI}_ffmpeg=4.2 >> ${VERSIONS_FILE}
		
	cd ..
fi