#!/bin/bash

set -e
export ANDROID_ARCH="arm64"
make init_qt
export ANDROID_ARCH="x86_64"
make init_qt
