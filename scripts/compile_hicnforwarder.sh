#!/bin/bash
set -e
cd HicnForwarderAndroid
if [ ! -f local.properties ]; then
	echo sdk.dir=${SDK} > local.properties
fi
VERSION_CODE="${2:-1}"
if [ "$1" = "DEBUG" ]; then
	./gradlew assembleDebug -PVERSION_CODE=$VERSION_CODE
else
	./gradlew assembleRelease -PVERSION_CODE=$VERSION_CODE
fi

echo "Apks are inside HicnForwarderAndroid/app/build/outputs/apk"
cd ..