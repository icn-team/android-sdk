## Android SDK ##

This is the hICN Distillery software distribution for Android. It is in charge of pulling
together all the necessary modules to build a full hICN software suite for arm64 and x86 Android arch.

## Dependencies ##

Install dependencies

If Ubuntu:

```
sudo apt-get git wget python curl automake libconf libtool openjdk-8-jdk
```

If Mac Os X

```
brew install git wget automake libconf libtool
```


## Quick Start ##

Clone this distro

```
git clone https://github.com/icn-team/android-sdk.git
cd android-sdk
```

Compile everything (dependencies and hICN modules)

```
make update
export ANDROID_ARCH="arm64"
make all
export ANDROID_ARCH="x86"
make all
```

The hICN Distillery software will be installed in android-sdk/usr_aarch64 and android-sdk/usr_i686


To compile Hybrid ICN Network Service for android app

```
make android_hicnforwarder
```

To install the application run

```
# Optionally, uninstall previous version (to avoid signature mismatch issues)
adb uninstall com.cisco.hicn.forwarder

adb install -r ./HicnForwarderAndroid/app/build/outputs/apk/release/HicnForwarderAndroid.apk
```

To compile Hybrid ICN SpeedTest & Test android app

```
make android_hicntools
```

To install the application run

```
adb install -r ./app/build/outputs/apk/release/hICN_Tools.apk
```

To compile Viper ABR video player for android app 

```
make android_viper
```

To install the application run

```
adb install -r build_aarch64/viper/hicn-viper-arm64_v8a/build/outputs/apk/hicn-viper-arm64_v8a-release-signed.apk
```


## Platforms ##

- Android



## Getting Started ##

To get simple help run `make`. This will give you a list of possible targets to
execute. You will basically want to download all the sources and compile.

Here's a short summary:

- `make help`				- This help message
- `make all`				- Download sdk, ndk and dependencies, configure, compile and install all software in DISTILLERY_INSTALL_DIR
- `make init_depend` 		- Download sdk, ndk and dependencies, compile and install all dependencies in DISTILLERY_INSTALL
- `make update`				- update hicn to the right commit
- `make install-all` 		- Configure, compile and install all software in DISTILLERY_INSTALL_DIR
- `make curl-clean`				- Clean curl files and libs
- `make openssl-clean`			- Clean opennssl files and libs
- `make asio-clean`				- Clean asio files
- `make event-clean`				- Clean libevent files and libs
- `make ffmpeg-clean`			- Clean ffmpeg files and libs
- `make libconfig-clean`			- Clean libconfig files and libs
- `make xml2-clean`				- Clean libxml2 files and libs
- `make libdash-clean`			- Clean libdash files and libs
- `make viper-clean`				- Clean viper files
- `make dependencies-clean`	 	- Clean all dependencies files and libs
- `make sdk-clean`				- Clean sdk files
- `make ndk-clean`				- Clean ndk files
- `make androidsdk-clean`		- Clean sdk, ndk and cmake files
- `make libparc-clean`			- Clean libparc files and libs
- `make hicn-clean`				- Clean hicn files and libs
- `make all-clean`				- Clean	all files and libs
- `make android_hicnforwarder`	- Build HicnForwader apk for android
- `make android_hicnforwarder_debug`	- Build HicnForwader apk for android in debug mode
- `make android_hicntools`		- Build HicnForwader apk for android
- `make android_hicntools_debug`	- Build HicnForwader apk for android in debug mode
- `make android_viper`			- Build Viper apk for android apk in debug mode (only arm64)
- `make android_viper_debug`		- Build Viper apk for android apk (only arm64)
- `make version`		- Print the version of installed modules


## Configuration ##

Distillery can be configured in multiple ways.  Please check the config directory (specifically `config/config.mk`) for more information.
