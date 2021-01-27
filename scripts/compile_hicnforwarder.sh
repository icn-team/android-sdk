#!/bin/bash
set -e
ENABLE_DEBUG="NODEBUG"
VERSION_CODE=1
GITHUB_USER=""
GITHUB_TOKEN=""
ENABLE_HPROXY="0"
MVN_REPO=""
while getopts ":d:v:p:u:t:r:" opt; do
  case $opt in
    d) ENABLE_DEBUG="$OPTARG"
    ;;
    v) VERSION_CODE="$OPTARG"
    ;;
    p) ENABLE_HPROXY="$OPTARG"
    ;;
    u) GITHUB_USER="$OPTARG"
    ;;
    t) GITHUB_TOKEN="$OPTARG"
    ;;
    r) MVN_REPO="$OPTARG"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    ;;
  esac
done

if [ "$ENABLE_HPROXY" = "1" ]; then
    if [ "$GITHUB_USER" = "" ] || [ "$GITUB_TOKEN" = "" ]; then
        echo "set github user and token"
        exit 1
    fi

fi

cd HicnForwarderAndroid
if [ ! -f local.properties ]; then
	echo sdk.dir=${SDK} > local.properties
fi

if [ "$ENABLE_DEBUG" = "DEBUG" ]; then
    ASSEMBLE="assembleDebug"
else
    ASSEMBLE="assembleRelease"
fi

if [ "$MVN_REPO" = "" ]; then
    ./gradlew $ASSEMBLE -PENABLE_HPROXY=$ENABLE_HPROXY -PVERSION_CODE=$VERSION_CODE -PGITHUB_USER=$GITHUB_USER -PGITHUB_TOKEN=$GITHUB_TOKEN
else
    ./gradlew $ASSEMBLE -PENABLE_HPROXY=$ENABLE_HPROXY -PVERSION_CODE=$VERSION_CODE -PGITHUB_USER=$GITHUB_USER -PGITHUB_TOKEN=$GITHUB_TOKEN -PMVN_REPO=$MVN_REPO
fi




echo "Apks are inside HicnForwarderAndroid/app/build/outputs/apk"
cd ..
