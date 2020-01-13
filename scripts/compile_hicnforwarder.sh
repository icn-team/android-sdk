#!/bin/bash
set -e
cd HicnForwarderAndroid
if [ ! -f local.properties ]; then
	echo sdk.dir=${SDK} > local.properties
	#echo ndk.dir=${NDK} >> local.properties
fi

if [ "$2" = "DEBUG" ]; then
	if [ -z "$1" ]; then
	    ./gradlew assembleDebug
	else
	    ./gradlew assembleDebug -PVERSION_CODE=$1
	fi
else
    if [ -z "$1" ]; then
	    ./gradlew assembleRelease
	else
	    ./gradlew assembleRelease -PVERSION_CODE=$1 
	fi
fi

echo "Apks are inside HicnForwarderAndroid/app/build/outputs/apk"
cd ..
