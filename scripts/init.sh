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
mkdir -p ${INSTALLATION_DIR}/lib

if [ -z ${SDK_PATH} ]  ; then
	mkdir -p sdk
	cd sdk
	if [ ! -d tools ]; then
		if [ ! -f sdk-tools-${OS}-${ANDROID_SDK_TOOLS_REV}.zip ]; then
		    wget --quiet https://dl.google.com/android/repository/sdk-tools-${OS}-${ANDROID_SDK_TOOLS_REV}.zip
		fi
		unzip -qq sdk-tools-${OS}-${ANDROID_SDK_TOOLS_REV}.zip
	fi
	if [ ! -d build-tools ] || [ ! -d extras ] || [ ! -d licenses ] || [ ! -d patcher ] || [ ! -d platform-tools ] || [ ! -d platforms ]; then 
		echo yes | tools/bin/sdkmanager --licenses > /dev/null
		echo yes | tools/bin/sdkmanager --update
		echo yes | tools/bin/sdkmanager 'tools'
		echo yes | tools/bin/sdkmanager 'platform-tools'
		echo yes | tools/bin/sdkmanager 'build-tools;'$ANDROID_BUILD_TOOLS
		echo yes | tools/bin/sdkmanager 'platforms;android-'$ANDROID_COMPILE_SDK
		echo yes | tools/bin/sdkmanager 'platforms;android-28'
		echo yes | tools/bin/sdkmanager 'extras;android;m2repository'
		echo yes | tools/bin/sdkmanager 'extras;google;google_play_services'
		echo yes | tools/bin/sdkmanager 'extras;google;m2repository'
    fi
	cd ..
fi

if [ -z ${NDK_PATH} ]; then
	mkdir -p sdk
	cd sdk
	if [ ! -d tools ]; then
		if [ ! -f sdk-tools-${OS}-${ANDROID_SDK_TOOLS_REV}.zip ]; then
		    wget --quiet https://dl.google.com/android/repository/sdk-tools-${OS}-${ANDROID_SDK_TOOLS_REV}.zip
		fi
		unzip -qq sdk-tools-${OS}-${ANDROID_SDK_TOOLS_REV}.zip
	fi
	if [ ! -d build-tools ] || [ ! -d cmake ] || [ ! -d ndk-bundle ] || [ ! -d platform-tools ] || [ ! -d platforms ]; then
		echo yes | tools/bin/sdkmanager 'tools'
		echo yes | tools/bin/sdkmanager 'platform-tools'
		echo yes | tools/bin/sdkmanager 'build-tools;'$ANDROID_BUILD_TOOLS
		echo yes | tools/bin/sdkmanager 'cmake;'$ANDROID_CMAKE_REV
		echo yes | tools/bin/sdkmanager --channel=3 --channel=1 'cmake;'$ANDROID_CMAKE_REV_3_10
		echo yes | tools/bin/sdkmanager 'ndk-bundle'
	fi
	cd ..
fi

mkdir -p src
cd src

if [ ! -d cframework ]; then
	echo "cframework not found"
	git clone -b cframework/master https://gerrit.fd.io/r/cicn cframework
fi

if [ ! -d viper ]; then
	echo "cframework not found"
	git clone -b viper/master https://gerrit.fd.io/r/cicn viper
fi

if [ ! -d hicn ]; then
	echo "libhicn not found"
	git clone https://github.com/FDio/hicn.git
	cd hicn
	git checkout $HICN_COMMIT
	for hash in $(git log -100 --format="%H")
	do
		if ! grep -q $hash "${BLACKLIST_FILE}"; then
  			actual_hash=$(git log -1 --format="%H")
  			if [ "${hash}" != "${actual_hash}" ]; then
  				git checkout $hash
  				if [ -f ${VERSIONS_FILE} ]; then
  					installed_version_arm64=$(cat ${VERSIONS_FILE} | grep "arm64_hicn" | awk -F "=" '{print $2;}')
  					if [ "$installed_version_arm64" != "$hash" ]; then
  						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_arm64/lib/libhicn*
						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_arm64/lib/libfacemgr.*
						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_arm64/include/hicn
  					fi
  					installed_version_x86=$(cat ${VERSIONS_FILE} | grep "x86_hicn" | awk -F "=" '{print $2;}')
  					if [ "$installed_version_armx86" != "$hash" ]; then
  						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_x86/lib/libhicn*
						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_x86/lib/libfacemgr.*
						rm -rf ${DISTILLERY_INSTALL_DIR_PREFIX}_x86/include/hicn
  					fi
  				fi
  			fi
  			break
		fi
	done
	cd ..
fi
if [ ! -d curl ]; then
	echo "curl  not found"
	git clone https://github.com/curl/curl.git
	cd curl
	git checkout tags/curl-7_66_0
	cd ..
fi



if [ ! -d libxml2 ]; then
	echo "libxml2 not found"
	git clone https://github.com/GNOME/libxml2.git
	cd libxml2
	git checkout tags/v2.9.9
	cd ..
	cp $BASE_DIR/external/libxml2/CMakeLists.txt libxml2
	cp $BASE_DIR/external/libxml2/xmlversion.h libxml2/include/libxml
	cp $BASE_DIR/external/libxml2/config.h libxml2
	${SED} -i '1s/^/#include <errno.h>/' libxml2/triodef.h
fi

if [ ! -d libevent ]; then
    echo "libevent not found"
	git clone https://github.com/libevent/libevent.git
	cd libevent
	git checkout tags/release-2.1.11-stable
	cd ..
fi

if [ ! -d ${INSTALLATION_DIR}/include/asio ]; then
	echo "Asio not found"
	if [ ! -d asio ]; then
		echo "Asio directory not found"
		git clone https://github.com/chriskohlhoff/asio.git
		cd asio
		git checkout tags/asio-1-12-2
		cd ..
	fi
	cp -r asio/asio/include/asio.hpp ${INSTALLATION_DIR}/include/
	cp -r asio/asio/include/asio ${INSTALLATION_DIR}/include/
fi

if [ ! -d libconfig ]; then
	echo "libconfig not found"
	git clone https://github.com/hyperrealm/libconfig.git
	cd libconfig
	git checkout a6b370e78578f5bf594f8efe0802cdc9b9d18f1a
	cd ..
	${SED} -i -- '2s/$/include(CheckSymbolExists)/' libconfig/CMakeLists.txt 
fi

cd ../

if [ ! -d ${INSTALLATION_DIR}/include/openssl ]; then
	echo "OpenSSL Libs not found!"
	echo "Compile OpenSSL"
	export ANDROID_NDK_ROOT=${BASE_DIR}/sdk/ndk-bundle
    bash ${BASE_DIR}/scripts/build-openssl.sh android-$ABI  $ANDROID_NDK_ROOT $BASE_DIR/external
	cp $BASE_DIR/external/openssl-android-$ABI/*.a ${INSTALLATION_DIR}/lib/
	cp -r $BASE_DIR/external/openssl-android-$ABI/include/openssl ${INSTALLATION_DIR}/include/
	rm -rf $BASE_DIR/external/openssl-android-$ABI
	${SED} -i "/${ABI}_openssl/d" ${VERSIONS_FILE}
	echo ${ABI}_openssl=1.1.1d >> ${VERSIONS_FILE}
fi