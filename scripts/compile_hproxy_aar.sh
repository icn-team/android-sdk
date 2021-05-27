#!/bin/bash
set -ex
cd hproxy-aar
if [ ! -f local.properties ]; then
  echo sdk.dir=${SDK} > local.properties
  cat local.properties
fi
VERSION_CODE="${2:-1}"
if [ "$1" = "DEBUG" ]; then
  ./gradlew assembleDebug -PVERSION_CODE=$VERSION_CODE
else
  ./gradlew assembleRelease -PVERSION_CODE=$VERSION_CODE
fi
cd ..