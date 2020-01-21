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

QT_VERSION=5.13.1
QT_VERSION_INSTALL=5131
if [ "${ABI}" = "arm64" ]; then
    QT_ANDROID=android_arm64_v8a
elif [ "${ABI}" = "x86" ]; then
    QT_ANDROID=android_x86
fi
if [ ! -d ${QT_HOME}/${QT_VERSION}/${QT_ANDROID}/include/QtAV ]; then
	export ANDROID_SDK_ROOT=${SDK}
	export ANDROID_NDK_ROOT=${NDK}
	export PATH=$PATH:${ANDROID_HOME}/tools:${JAVA_HOME}/bin
    echo "INCLUDEPATH = ${INSTALLATION_DIR}/include/" > ${BASE_DIR}/src/QtAV/.qmake.conf
	echo "LIBS = -L${INSTALLATION_DIR}/lib/" >> ${BASE_DIR}/src/QtAV/.qmake.conf
	mkdir -p ${DISTILLERY_BUILD_DIR}/qtav
	cd ${DISTILLERY_BUILD_DIR}/qtav
	${QT_HOME}/${QT_VERSION}/${QT_ANDROID}/bin/qmake ${BASE_DIR}/src/QtAV/QtAV.pro -spec android-clang 
	make
	make install INSTALL_ROOT=${QT_ANDROID}
	sh sdk_install.sh
	QTAV_VERSION=$(git --git-dir=${BASE_DIR}/src/QtAV/.git --work-tree=${BASE_DIR}/src/QtAV/ log -1 --format="%H")
	touch ${VERSIONS_FILE}
	${SED} -i "/${ABI}_QtAV/d" ${VERSIONS_FILE}
	echo ${ABI}_QtAV=${QTAV_VERSION} >> ${VERSIONS_FILE}
fi
