 #############################################################################
 # Copyright (c) 2017 Cisco and/or its affiliates.
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

DISTILLERY_VERSION=2.0

default.target: help

all: init_depend install-all
#init_qt

##############################################################
# Variables
#
# Set some variables
DISTILLERY_STAMP=.distillery.stamp
REBUILD_DEPENDS=

##############################################################
# Load the configuration
#
# For more information please see config.default.mk
#
DISTILLERY_CONFIG_DIR ?= config

##DISTILLERY_DEFAULT_CONFIG ?= ${DISTILLERY_CONFIG_DIR}/config.mk
##DISTILLERY_LOCAL_CONFIG   ?= ${DISTILLERY_CONFIG_DIR}/local/config.mk
DISTILLERY_USER_CONFIG    ?= ${DISTILLERY_CONFIG_DIR}/config.mk

ifneq (,$(wildcard ${DISTILLERY_USER_CONFIG}))
    include ${DISTILLERY_USER_CONFIG}
    REBUILD_DEPENDS+=${DISTILLERY_USER_CONFIG}
else
    DISTILLERY_USER_CONFIG+="[Not Found]"
endif

ifneq (,$(wildcard ${DISTILLERY_LOCAL_CONFIG}))
    include ${DISTILLERY_LOCAL_CONFIG}
    REBUILD_DEPENDS+=${DISTILLERY_LOCAL_CONFIG}
endif

include ${DISTILLERY_DEFAULT_CONFIG}


##############################################################
# Set the paths
#
# PATH: add our install dir, build dependencies and system dependencies
# LD_RUN_PATH: add our install dir

export PATH := $(DISTILLERY_INSTALL_DIR)/bin:$(DISTILLERY_TOOLS_DIR)/bin:$(PATH)
#export LD_RUN_PATH := $(DISTILLERY_INSTALL_DIR)/lib
#export LD_LIBRARY_PATH := $(DISTILLERY_INSTALL_DIR)/lib
export CCNX_HOME
export FOUNDATION_HOME


##############################################################
# Modules
#
# Load the modules config. Please refer to that file for more information
DISTILLERY_MODULES_DIR=${DISTILLERY_CONFIG_DIR}/modules

# The modules variable is a list of modules. It will be populated by the
# modules config files.
modules=
modules_dir=
    include config/modules/000-distillery-update.mk
	include config/modules/000-gitModule.mk
	include config/modules/001-modules.mk
	include config/modules/002-cmake-modules.mk
	include config/modules/002-make-modules.mk
	include config/modules/100-distillery.mk
	include config/modules/105-libconfig.mk
	include config/modules/107-libevent.mk
	include config/modules/108-libxml2.mk
	include config/modules/109-curl.mk
	include config/modules/120-libparc.mk
	include config/modules/130-hicn.mk
	include config/modules/610-libdash.mk

# Load user defined modules
DISTILLERY_USER_MODULES_DIR=${DISTILLERY_USER_CONFIG_DIR}/modules
ifneq (,$(wildcard ${DISTILLERY_USER_MODULES_DIR}))
    include ${DISTILLERY_USER_MODULES_DIR}/*.mk
else
    DISTILLERY_USER_MODULES_DIR+="[Not Found]"
endif

ifdef ${DISTILLERY_LOCAL_MODULES_DIR}
    include ${DISTILLERY_LOCAL_MODULES_DIR}/*.mk
else
    DISTILLERY_LOCAL_MODULES_DIR="[Undefined]"
endif

install-all: install-directories ${modules}

init_depend:
	./scripts/init.sh ${ABI} ${DISTILLERY_INSTALL_DIR};
init_qt:
	./scripts/init_qt.sh
android_hicnforwarder:
	./scripts/compile_hicnforwarder.sh
android_hicnforwarder_debug:
	./scripts/compile_hicnforwarder.sh DEBUG
android_hicntools:
	./scripts/compile_hicntools.sh
android_hicntools_debug:
	./scripts/compile_hicntools.sh DEBUG
android_viper:
	./scripts/compile_androidviper.sh
android_viper_debug:
	./scripts/compile_androidviper.sh DEBUG

curl-clean:
	@rm -rf ${DISTILLERY_INSTALL_DIR}/lib/libcurl.*
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/curl
	@rm -rf ${DISTILLERY_BUILD_DIR}/curl
	
openssl-clean:
	@rm -rf ${DISTILLERY_INSTALL_DIR}/lib/libssl.*
	@rm -rf ${DISTILLERY_INSTALL_DIR}/lib/libcrypto.*
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/openssl
	@rm -rf external/openssl*

event-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR}/libevent
	@rm -rf ${DISTILLERY_INSTALL_DIR}/lib/libevent*
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/event2
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/ev*.h
		
xml2-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR}/libxml2
	@rm -rf ${DISTILLERY_INSTALL_DIR}/lib/libxml2*
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/libxml

libconfig-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR}/libconfig
	@rm -rf ${DISTILLERY_INSTALL_DIR}/lib/libconfig*
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/libconfig*
	
asio-clean:
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/asio*

libparc-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR}/cframework
	@rm -rf ${DISTILLERY_INSTALL_DIR}/lib/libparc.*
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/parc

ffmpeg-clean:
	@rm -rf ${DISTILLERY_INSTALL_DIR}/lib/libav.*
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/libav*
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/libsw*

libdash-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR}/viper/libdash
	@rm -rf ${DISTILLERY_INSTALL_DIR}/lib/libdash.*
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/libdash

dependencies-clean: event-clean openssl-clean curl-clean xml2-clean libconfig-clean libparc-clean asio-clean ffmpeg-clean libdash-clean
	
sdk-clean:
	@rm -rf sdk/android-sdk_*
	@rm -rf sdk/sdk
	
ndk-clean:
	@rm -rf sdk/android-ndk-*
	@rm -rf sdk/ndk-bundle

	
androidsdk-clean: sdk-clean ndk-clean

hicn-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR}/hicn
	@rm -rf ${DISTILLERY_INSTALL_DIR}/lib/libhicn.*
	@rm -rf ${DISTILLERY_INSTALL_DIR}/include/hicn

viper-clean:
	@rm -rf ${DISTILLERY_BUILD_DIR}/viper
	
	
qt-clean:
	@rm -rf qt/Qt

qtav-clean:
	@bash ${DISTILLERY_BUILD_DIR}/qtav/sdk_uninstall.sh
	@rm -rf ${DISTILLERY_BUILD_DIR}/qtav

	
all-clean: dependencies-clean hicn-clean qt-clean qtav-clean viper-clean

update:
	./scripts/update.sh

help:
	@echo "---- Basic build targets ----"
	@echo "make help			- This help message"
	@echo "make update			- git pull the different modules to the head of master"
	@echo "make all				- Download sdk, ndk and dependencies, configure, compile and install all software in DISTILLERY_INSTALL_DIR"
	@echo "make init_depend 	- Download sdk, ndk and dependencies, compile and install all dependencies in DISTILLERY_INSTALL"
	@echo "make install-all 	- Configure, compile and install all software in DISTILLERY_INSTALL_DIR"
	@echo "curl-clean			- Clean curl files and libs"
	@echo "openssl-clean		- Clean opennssl files and libs"
	@echo "asio-clean			- Clean asio files"
	@echo "event-clean			- Clean libevent files and libs"
	@echo "ffmpeg-clean			- Clean ffmpeg files and libs"
	@echo "libconfig-clean		- Clean libconfig files and libs"
	@echo "xml2-clean			- Clean libxml2 files and libs"
	@echo "libdash-clean		- Clean libdash files and libs"
	@echo "viper-clean			- Clean viper files"
	@echo "dependencies-clean 	- Clean all dependencies files and libs"
	@echo "sdk-clean			- Clean sdk files"
	@echo "ndk-clean			- Clean ndk files"
	@echo "androidsdk-clean		- Clean sdk, ndk and cmake files"
	@echo "libparc-clean		- Clean libparc files and libs"
	@echo "hicn-clean			- Clean hicn files and libs"
	@echo "all-clean			- Clean	all files and libs"
	@echo "android_hicnforwarder	- Build HicnForwader apk for android"
	@echo "android_hicnforwarder_debug	- Build HicnForwader apk for android in debug mode"
	@echo "android_hicntools	- Build HicnTools apk for android"
	@echo "android_hicntools_debug	- Build HicnTools apk for android in debug mode"
	@echo "android_viper		- Build Viper apk for android apk in debug mode (only arm64)" 
	@echo "android_viper_debug	- Build Viper apk for android apk (only arm64)"
	
${DISTILLERY_STAMP}: ${REBUILD_DEPENDS}
	touch $@

install-directories:
	@mkdir -p ${DISTILLERY_INSTALL_DIR}/include
	@mkdir -p ${DISTILLERY_INSTALL_DIR}/lib
	@mkdir -p ${DISTILLERY_INSTALL_DIR}/bin


.PHONY: dependencies
