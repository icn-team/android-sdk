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
if [ -z ${SDK_PATH} ]; then
	mkdir -p sdk
	cd sdk
	if [ ! -d sdk ]; then
		if [ $OS = darwin ]; then
			if [ ! -f android-sdk_r24.4.1-macosx.zip ]; then
				wget http://dl.google.com/android/android-sdk_r24.4.1-macosx.zip
			fi
			
			echo "unzip android-sdk"
			unzip -q android-sdk_r24.4.1-macosx.zip
			mv android-sdk-macosx sdk
		else
			if [ ! -f android-sdk_r24.4.1-linux.tgz ]; then
				wget http://dl.google.com/android/android-sdk_r24.4.1-linux.tgz
			fi
			echo "untar android-sdk"
			tar zxf android-sdk_r24.4.1-linux.tgz
			mv android-sdk-linux sdk
		fi
		mkdir -p sdk/licenses
		echo "\n24333f8a63b6825ea9c5514f83c2829b004d1fee" > "sdk/licenses/android-sdk-license"
		echo "\n8933bad161af4178b1185d1a37fbf41ea5269c55" >> "sdk/licenses/android-sdk-license"
		echo "\n84831b9409646a918e30573bab4c9c91346d8abd" > "sdk/licenses/android-sdk-preview-license"
		echo "y" | ./sdk/tools/android update sdk --filter platform-tools,build-tools-28.0.3,android-28,extra-android-m2repository,extra-google-m2repository --no-ui --all --force
		echo "y" | ./sdk/tools/android update sdk --filter "android-28" --no-ui --all --force 
		echo "y" | ./sdk/tools/android update sdk --no-ui --all --filter build-tools-28.0.3
	fi
	cd ..
fi

if [ -z ${NDK_PATH} ]; then
    mkdir -p sdk
	cd sdk
	if [ ! -d ndk-bundle ]; then
		if [ ! -f android-ndk-r19c-${OS}-${ARCH}.zip ]; then
		    wget https://dl.google.com/android/repository/android-ndk-r19c-${OS}-${ARCH}.zip
		fi
		
		echo "unzip android-ndk"
		unzip -q android-ndk-r19c-${OS}-${ARCH}.zip
		mv android-ndk-r19c ndk-bundle
	fi
	cd ..
fi

export TOOLCHAIN=$BASE_DIR/sdk/toolchain_$ABI

if [ ! -d $TOOLCHAIN ];then
	echo "creating toolchain"
	python $NDK/build/tools/make_standalone_toolchain.py \
        --arch $ABI --api 26 --install-dir $TOOLCHAIN
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
	git clone https://gerrit.fd.io/r/hicn
fi

if [ ! -d curl ]; then
	echo "curl  not found"
	git clone https://github.com/curl/curl.git
	cd curl
	git checkout tags/curl-7_66_0
fi



if [ ! -d libxml2 ]; then
	echo "libxml2 not found"
	git clone https://github.com/GNOME/libxml2.git
	cp $BASE_DIR/external/libxml2/CMakeLists.txt libxml2
	cp $BASE_DIR/external/libxml2/xmlversion.h libxml2/include/libxml
	cp $BASE_DIR/external/libxml2/config.h libxml2
	if [ $OS = darwin ]; then
		sed -i '' '1s/^/#include <errno.h>/' libxml2/triodef.h
	else
		sed -i '1s/^/#include <errno.h>/' libxml2/triodef.h
	fi
fi

if [ ! -d libevent ]; then
    echo "libevent not found"
	git clone https://github.com/libevent/libevent.git
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
	sed -i -- '2s/$/include(CheckSymbolExists)/' libconfig/CMakeLists.txt 
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
fi

