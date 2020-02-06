#!/bin/bash

set -e
export ANDROID_ARCH="arm64"
make all
export ANDROID_ARCH="x86_64"
make all
