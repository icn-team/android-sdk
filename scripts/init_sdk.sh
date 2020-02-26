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
echo  ---> $ANDROID_SDK_TOOLS_REV
OS=`echo $OS | tr '[:upper:]' '[:lower:]'`

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