#!/bin/bash
set -e
export BUILD_HPROXY=1
export HPROXY_URL=ssh://git@bitbucket-eng-gpk1.cisco.com:7999/ngl/hproxy.git
export HPROXY_AAR_URL=ssh://git@bitbucket-eng-gpk1.cisco.com:7999/ngl/hproxy-aar.git
export ANDROID_ARCH="arm64"
make all VERBOSE=1 
export ANDROID_ARCH="x86_64"
make all VERBOSE=1
