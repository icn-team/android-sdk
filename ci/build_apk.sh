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

wget https://github.com/icn-team/android-sdk/releases/download/release/HicnForwarderAndroid.apk
AAPT=$(find /sdk -name "aapt" | sort -r | head -1)
VERSION_CODE=$($AAPT dump badging HicnForwarderAndroid.apk | grep versionCode | awk '{print $3}' | sed s/versionCode=//g | sed s/\'//g) 
echo $VERSION_CODE
if [ "$VERSION_CODE" -lt "29" ]; then
  VERSION_CODE=29
fi
VERSION_CODE=$((VERSION_CODE+1))


ln -sf /usr_aarch64 /hicn
ln -sf /usr_x86_64 /hicn
ln -s /.versions /hicn
cd /hicn
make version

cd /hicn/HicnForwarderAndroid
echo sdk.dir=/sdk > local.properties
echo ndk.dir=/sdk/ndk-bundle >> local.properties
./gradlew assembleRelease -PVERSION_CODE=$VERSION_CODE
ANDROID_HOME=/sdk

cp app/build/outputs/apk/release/*.apk /hicn

cd /hicn/hICNTools
echo sdk.dir=/sdk > local.properties
echo ndk.dir=/sdk/ndk-bundle >> local.properties
./gradlew assembleRelease -PVERSION_CODE=$VERSION_CODE
cp app/build/outputs/apk/release/*.apk /hicn




export QT_VERSION=5.13.2
export QT_HOME=/qt/Qt
export ANDROID_NDK_HOME=/sdk/ndk-bundle
if [ ! -d /src/viper ]; then
	git clone -b viper/master https://gerrit.fd.io/r/cicn /src/viper
fi
export OS=$(uname | tr '[:upper:]' '[:lower:]')
export ARCH=$(uname -m)
export ANDROID_HOME=${ANDROID_HOME} 
export ANDROID_NDK_HOST=${OS}-${ARCH}
export ANDROID_NDK_PLATFORM=android-28
export ANDROID_NDK_ROOT=/sdk/ndk-bundle
export ANDROID_SDK_ROOT=/sdk
export ANDROID_API_VERSION=android-28
export PATH=$PATH:${ANDROID_HOME}/tools:${JAVA_HOME}/bin

sed -i -e "s/android:versionCode=\"9\"/android:versionCode=\"$VERSION_CODE\"/g" /src/viper/android/AndroidManifest.xml
sed -i -e "s/android:targetSdkVersion=\"26\"/android:targetSdkVersion=\"28\"/g" /src/viper/android/AndroidManifest.xml
export ANDROID_ARCH=arm64_v8a
export DISTILLARY_INSTALLATION_PATH=/usr_aarch64/
export QT_HOST_PREFIX=/qt/Qt/$QT_VERSION/android_${ANDROID_ARCH}
mkdir -p /build_aarch64/viper
cd /build_aarch64/viper
/qt/Qt/$QT_VERSION/android_${ANDROID_ARCH}/bin/qmake -r -spec android-clang /src/viper/viper.pro  "TRANSPORT_LIBRARY = HICNET"
make
make install INSTALL_ROOT=hicn-viper-${ANDROID_ARCH}
/qt/Qt/$QT_VERSION/android_${ANDROID_ARCH}/bin/androiddeployqt --output hicn-viper-${ANDROID_ARCH} --verbose --input android-libviper.so-deployment-settings.json --gradle --android-platform ${ANDROID_NDK_PLATFORM} --stacktrace --release --target ${ANDROID_NDK_PLATFORM} --release \
--sign /src/viper/android/viper.keystore viper --storepass icn_viper

cp /build_aarch64/viper/hicn-viper-arm64_v8a//build/outputs/apk/hicn-viper-arm64_v8a-release-signed.apk /hicn

mv /hicn/hicn-viper-arm64_v8a-release-signed.apk /hicn/viper-arm64.apk

export DISTILLARY_INSTALLATION_PATH=/usr_x86_64/
export QT_HOST_PREFIX=/qt/Qt/$QT_VERSION/android_${ANDROID_ARCH}
export ANDROID_ARCH=x86_64
mkdir -p /build_x86_64/viper
cd /build_x86_64/viper
/qt/Qt/$QT_VERSION/android_${ANDROID_ARCH}/bin/qmake -r -spec android-clang /src/viper/viper.pro  "TRANSPORT_LIBRARY = HICNET"
make
make install INSTALL_ROOT=hicn-viper-${ANDROID_ARCH}
/qt/Qt/$QT_VERSION/android_${ANDROID_ARCH}/bin/androiddeployqt --output hicn-viper-${ANDROID_ARCH} --verbose --input android-libviper.so-deployment-settings.json --gradle --android-platform ${ANDROID_NDK_PLATFORM} --stacktrace --release --target ${ANDROID_NDK_PLATFORM} --release \
--sign /src/viper/android/viper.keystore viper --storepass icn_viper

cp /build_x86_64/viper/hicn-viper-x86_64//build/outputs/apk/hicn-viper-x86_64-release-signed.apk /hicn

mv /hicn/hicn-viper-x86_64-release-signed.apk /hicn/viper-x86_64.apk

if [ "$1" = "1" ]; then
  APK_PATH=/hicn/HicnForwarderAndroid.apk
  bash /hicn/ci/push_playstore.sh /hicn/playstore_key.json $APK_PATH $VERSION_CODE /sdk
  APK_PATH=/hicn/hICN_Tools.apk
  bash /hicn/ci/push_playstore.sh /hicn/playstore_key.json $APK_PATH $VERSION_CODE /sdk
  APK_PATH=/hicn/viper-arm64.apk
  bash /hicn/ci/push_playstore.sh /hicn/playstore_key.json $APK_PATH $VERSION_CODE /sdk
fi

rm /hicn/usr_*
