#!/bin/bash
set -e
set -x

SCRIPT_DIR=`realpath .`/scripts
OPENSSL_VERSION=1.1.1d

echo $SCRIPT_DIR/scripts
ARCH=$1
# Set directory
export ANDROID_NDK_HOME=$2
EXTERNAL=$3

OPENSSL_DIR=$EXTERNAL/openssl-$ARCH
cd $EXTERNAL

if [ ! -d $OPENSSL_DIR ]; then
	echo "openssl not found"
	if [ ! -f openssl-$OPENSSL_VERSION.tar.gz ]; then
		wget https://www.openssl.org/source/openssl-$OPENSSL_VERSION.tar.gz
    fi
    #mkdir $OPENSSL_DIR
    tar -zxvf openssl-$OPENSSL_VERSION.tar.gz
    mv openssl-$OPENSSL_VERSION $OPENSSL_DIR
fi

# Find the toolchain for your build machine
toolchains_path=$(python $SCRIPT_DIR/toolchains_path.py --ndk ${ANDROID_NDK_HOME})

# Configure the OpenSSL environment, refer to NOTES.ANDROID in OPENSSL_DIR
# Set compiler clang, instead of gcc by default
CC=clang

# Add toolchains bin directory to PATH
PATH=$toolchains_path/bin:$PATH

# Set the Android API levels
ANDROID_API=26

# Set the target architecture
# Can be android-arm, android-arm64, android-x86, android-x86 etc
architecture=$ARCH

# Create the make file
cd ${OPENSSL_DIR}
./Configure ${architecture} -D__ANDROID_API__=$ANDROID_API no-shared no-unit-test no-tests

# Build
make

