#!/bin/bash
set -e
cd HicnForwarderAndroid
if [ ! -f local.properties ]; then
	echo sdk.dir=${SDK} > local.properties
	#echo ndk.dir=${NDK} >> local.properties
fi

if [ "$1" = "DEBUG" ]; then
	./gradlew assembleDebug
else
	./gradlew assembleRelease
fi

echo "Apks are inside HicnForwarderAndroid/app/build/outputs/apk"
cd ..