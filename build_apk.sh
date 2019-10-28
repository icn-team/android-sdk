#############################################################################
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
##############################################################################

#!/bin/bash

set -ex

mkdir -p /src
mkdir -p /apks

export ANDROID_NDK_HOME=/opt/android-sdk-linux/ndk-bundle/
if [ ! -d /src/android-sdk ]; then
    git clone https://github.com/icn-team/android-sdk.git /src/android-sdk
fi

ln -s /usr_aarch64 /src/android-sdk
ln -s /usr_i686 /src/android-sdk

cd /src/android-sdk/HicnForwarderAndroid
echo sdk.dir=${ANDROID_HOME} > local.properties
echo ndk.dir=${ANDROID_HOME}/ndk-bundle >> local.properties
./gradlew assembleRelease

cp app/build/outputs/apk/release/*.apk /apks


cd /src/android-sdk/hICNTools
echo sdk.dir=${ANDROID_HOME} > local.properties
echo ndk.dir=${ANDROID_HOME}/ndk-bundle >> local.properties
./gradlew assembleRelease
cp app/build/outputs/apk/release/*.apk /apks

ls /apks
