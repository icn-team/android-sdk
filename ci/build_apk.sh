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

#mkdir -p /src
export ANDROID_NDK_HOME=/hicn/sdk/ndk-bundle
if [ ! -d /src/viper ]; then
	git clone -b viper/master https://gerrit.fd.io/r/cicn /src/viper
fi
export OS=$(uname | tr '[:upper:]' '[:lower:]')
export ARCH=$(uname -m)
export ANDROID_HOME=${ANDROID_HOME} 
export ANDROID_NDK_HOST=${OS}-${ARCH}
export ANDROID_NDK_PLATFORM=android-28
export ANDROID_NDK_ROOT=${ANDROID_HOME}/ndk-bundle
export ANDROID_SDK_ROOT=${ANDROID_HOME}
export ANDROID_API_VERSION=android-28
export PATH=$PATH:${ANDROID_HOME}/tools:${JAVA_HOME}/bin

export ANDROID_ARCH=arm64_v8a
export DISTILLARY_INSTALLATION_PATH=/usr_aarch64/
mkdir -p /build_aarch64/viper
cd /build_aarch64/viper
/qt/Qt/$QT_VERSION/android_${ANDROID_ARCH}/bin/qmake -r -spec android-clang /src/viper/viper.pro  "TRANSPORT_LIBRARY = HICNET"
make
make install INSTALL_ROOT=hicn-viper-${ANDROID_ARCH}
/qt/Qt/android_${ANDROID_ARCH}/bin/androiddeployqt --output hicn-viper-${ANDROID_ARCH} --verbose --input android-libviper.so-deployment-settings.json --gradle --android-platform ${ANDROID_NDK_PLATFORM} --stacktrace --release --target ${ANDROID_NDK_PLATFORM} --release \
--sign /src/viper/android/viper.keystore viper --storepass icn_viper

cp /build_aarch64/viper/hicn-viper-arm64_v8a//build/outputs/apk/hicn-viper-arm64_v8a-release-signed.apk /hicn
mv /hicn/hicn-viper-arm64_v8a-release-signed.apk /hicn/viper-arm64.apk

export DISTILLARY_INSTALLATION_PATH=/usr_i686/
export ANDROID_ARCH=x86
mkdir -p /build_i686/viper
cd /build_i686/viper
/qt/Qt/$QT_VERSION/android_${ANDROID_ARCH}/bin/qmake -r -spec android-clang /src/viper/viper.pro  "TRANSPORT_LIBRARY = HICNET"
make
make install INSTALL_ROOT=hicn-viper-${ANDROID_ARCH}
/qt/Qt/$QT_VERSION/android_${ANDROID_ARCH}/bin/androiddeployqt --output hicn-viper-${ANDROID_ARCH} --verbose --input android-libviper.so-deployment-settings.json --gradle --android-platform ${ANDROID_NDK_PLATFORM} --stacktrace --release --target ${ANDROID_NDK_PLATFORM} --release \
--sign /src/viper/android/viper.keystore viper --storepass icn_viper

cp /build_i686/viper/hicn-viper-x86//build/outputs/apk/hicn-viper-x86-release-signed.apk /hicn
mv /hicn/hicn-viper-x86-release-signed.apk /hicn/viper-x86.apk


ln -sf /usr_aarch64 /hicn
ln -sf /usr_i686 /hicn

cd /hicn/HicnForwarderAndroid
echo sdk.dir=/sdk > local.properties
echo ndk.dir=/sdk/ndk-bundle >> local.properties
./gradlew assembleRelease

cp app/build/outputs/apk/release/*.apk /hicn

cd /hicn/hICNTools
echo sdk.dir=/sdk > local.properties
echo ndk.dir=/sdk/ndk-bundle >> local.properties
./gradlew assembleRelease
cp app/build/outputs/apk/release/*.apk /hicn

rm /hicn/usr_*
