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

mkdir -p /apks

export ANDROID_NDK_HOME=/opt/android-sdk-linux/ndk-bundle/

ln -sf /usr_aarch64 /hicn
ln -sf /usr_i686 /hicn

cd /hicn/HicnForwarderAndroid
echo sdk.dir=${ANDROID_HOME} > local.properties
echo ndk.dir=${ANDROID_HOME}/ndk-bundle >> local.properties
./gradlew assembleRelease

cp app/build/outputs/apk/release/*.apk /apks


#cd /src/android-sdk/hICNTools
cd /hicn/hICNTools
echo sdk.dir=${ANDROID_HOME} > local.properties
echo ndk.dir=${ANDROID_HOME}/ndk-bundle >> local.properties
./gradlew assembleRelease
cp app/build/outputs/apk/release/*.apk /apks

rm /hicn/usr_*
ls /apks
