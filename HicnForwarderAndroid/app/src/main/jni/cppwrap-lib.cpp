#include <jni.h>
#include <string>
#include <android/log.h>


extern "C" JNIEXPORT jstring JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_test(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap", "ciao!!!");
    return env->NewStringUTF(hello.c_str());
}
