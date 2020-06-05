#include <jni.h>
#include <string>
#include <android/log.h>


#ifdef ENABLE_HPROXY

#include <hicn/hproxy/proxy/proxy.h>

#include <arpa/inet.h>
#include <net/if.h>
#include <linux/if.h>
#include <linux/if_tun.h>
#include <netinet/ether.h>
#include <linux/if_packet.h>
#include <sys/ioctl.h>

#define HPROXY_ATTRIBUTE "mProxyPtr"

#define HPROXY_TAG "HproxyWrap"

using HicnProxy = hproxy::HicnProxy;

struct JniContext {
    JniContext() : env(nullptr), instance(nullptr) {}

    JNIEnv *env;
    jobject *instance;
};

#endif

// Get pointer field straight from `JavaClass`
jfieldID getPtrFieldId(JNIEnv *env, jobject obj, std::string attribute_name) {
    static jfieldID ptrFieldId = 0;

    if (!ptrFieldId) {
        jclass c = env->GetObjectClass(obj);
        ptrFieldId = env->GetFieldID(c, attribute_name.c_str(), "J");
        env->DeleteLocalRef(c);
    }

    return ptrFieldId;
}

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_initConfig(JNIEnv *env, jobject this_obj) {

}

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_start(JNIEnv *env, jobject instance,
                                                          jstring remote_address,
                                                          jint remote_port) {
#ifdef ENABLE_HPROXY
    JniContext *context = new JniContext();
    context->env = env;
    context->instance = &instance;

    const char *_remote_address = env->GetStringUTFChars(remote_address, 0);

    hproxy::connectors::ConnectorConfig config_connector(
            ConnectorType::UDP_TUNNEL_CONNECTOR, _remote_address, std::to_string(remote_port),
            std::string("0.0.0.0"), "udp-tunnel");

    uint64_t secret = 12345678910;
    hproxy::config::ClientConfiguration config_automation;
    config_automation.secret = secret;


    auto proxy = HicnProxy::createAsClient(config_connector, config_automation).release();
    proxy->setJniContext(context);
    env->SetLongField(instance, getPtrFieldId(env, instance, HPROXY_ATTRIBUTE), (jlong) proxy);
    proxy->run();
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_destroy(JNIEnv *env, jobject instance) {
#ifdef ENABLE_HPROXY
    HicnProxy *proxy = (HicnProxy *) env->GetLongField(instance, getPtrFieldId(env, instance,
                                                                               HPROXY_ATTRIBUTE));
    JniContext *jni_context = (JniContext *) proxy->getJniContext();
    delete jni_context;
    delete proxy;
#endif
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_isRunning(JNIEnv *env, jobject instance) {
#ifdef ENABLE_HPROXY
    HicnProxy *proxy = (HicnProxy *) env->GetLongField(instance, getPtrFieldId(env, instance,
                                                                               HPROXY_ATTRIBUTE));
    if (proxy) {
        return jboolean(proxy->isRunning());
    }
#endif

    return JNI_FALSE;
}

static int
createTunDeviceWrap(JNIEnv *env, jobject instance, const char *vpn_address, uint16_t prefix_length,
                    const char *route_address,
                    uint16_t route_prefix_length, const char *dns) {
    jclass clazz = env->GetObjectClass(instance);
    jmethodID methodID = env->GetMethodID(clazz, "createTunDevice",
                                          "(Ljava/lang/String;ILjava/lang/String;ILjava/lang/String;)I");

    int ret = -1;
    if (methodID) {
        jstring vpnAddress = env->NewStringUTF(vpn_address);
        jint prefixLength(prefix_length);
        jstring routeAddress = env->NewStringUTF(route_address);
        jint routePrefixLength(route_prefix_length);
        jstring dnsAddress = env->NewStringUTF(dns);
        ret = env->CallIntMethod(instance, methodID, vpnAddress, prefixLength, routeAddress,
                                 routePrefixLength, dnsAddress);
    }

    return ret;
}

static int
closeTunDeviceWrap(JNIEnv *env, jobject instance) {
    jclass clazz = env->GetObjectClass(instance);
    jmethodID methodID = env->GetMethodID(clazz, "closeTunDevice", "()I");

    int ret = -1;
    if (methodID) {
        ret = env->CallIntMethod(instance, methodID);
    }

    return ret;
}

extern "C" int createTunDevice(const char *vpn_address, uint16_t prefix_length,
                               const char *route_address,
                               uint16_t route_prefix_length, const char *dns, void *context) {
#ifdef ENABLE_HPROXY
    JniContext *jni_context = (JniContext *) (context);

    if (!jni_context->env || !jni_context->instance) {
        __android_log_print(ANDROID_LOG_ERROR, HPROXY_TAG,
                            "Call createTunDevice, but _env and _instance variables are not initialized.");
        return -1;
    }

    return createTunDeviceWrap(jni_context->env, *jni_context->instance, vpn_address, prefix_length,
                               route_address,
                               route_prefix_length, dns);
#else
    return 0;
#endif
}

extern "C" JNIEXPORT int JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_getTunFd(JNIEnv *env, jobject instance,
                                                             jstring device_name) {
#ifdef ENABLE_HPROXY
    const int fd = open("/dev/tun", O_RDWR | O_NONBLOCK);
    if (fd != -1) {
        struct ifreq ifr;

        memset(&ifr, 0, sizeof(ifr));
        ifr.ifr_flags = IFF_TUN | IFF_NO_PI;

        const char *_device_name = env->GetStringUTFChars(device_name, 0);
        __android_log_print(ANDROID_LOG_INFO, HPROXY_TAG,
                            "Opened device %s. PID: %d", _device_name, getpid());

        strncpy(ifr.ifr_name, _device_name, IFNAMSIZ);

        if (::ioctl(fd, TUNSETIFF, &ifr) < 0) {
            __android_log_print(ANDROID_LOG_ERROR, HPROXY_TAG,
                                "FD of tun device not retrieved.");
            __android_log_print(ANDROID_LOG_ERROR, HPROXY_TAG, "ioctl failed and returned errno %s",
                                strerror(errno));
        }

        __android_log_print(ANDROID_LOG_INFO, HPROXY_TAG, "TUN device allocated successfully. FD: %d", fd);
    } else {
        __android_log_print(ANDROID_LOG_INFO, HPROXY_TAG,
                            "Device not opened.");
    }

        return fd;
#else
    return -1;
#endif
}

extern "C" int closeTunDevice(void *context) {
#ifdef ENABLE_HPROXY
    JniContext *jni_context = (JniContext *) (context);
    if (!jni_context->env || !jni_context->instance) {
        __android_log_print(ANDROID_LOG_ERROR, HPROXY_TAG,
                            "Call createTunDevice, but _env and _instance variables are not initialized.");
        return -1;
    }

    return closeTunDeviceWrap(jni_context->env, *jni_context->instance);
#else
    return 0;
#endif
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_isHProxyEnabled(JNIEnv *env,
                                                                    jclass instance) {
#ifdef ENABLE_HPROXY
    return JNI_TRUE;
#else
    return JNI_FALSE;
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_stop(JNIEnv *env, jobject instance) {
#ifdef ENABLE_HPROXY
    HicnProxy *proxy = (HicnProxy *) env->GetLongField(instance, getPtrFieldId(env, instance,
                                                                               HPROXY_ATTRIBUTE));
    if (proxy) {
        proxy->stop();
    }
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_getProxifiedAppName(JNIEnv *env, jclass thiz) {
#ifdef ENABLE_HPROXY
    return env->NewStringUTF(HicnProxy::getProxifiedAppName());
#else
    std::string appName = "App";
    return env->NewStringUTF(appName.c_str());
#endif

}

extern "C" JNIEXPORT jstring JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_getHicnServiceName(JNIEnv *env, jclass thiz) {
#ifdef ENABLE_HPROXY
    return env->NewStringUTF(HicnProxy::getHicnServiceName());
#else
    std::string appName = "App";
    return env->NewStringUTF(appName.c_str());
#endif

}

extern "C" JNIEXPORT jstring JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_getProxifiedPackageName(JNIEnv *env,
                                                                            jclass thiz) {
#ifdef ENABLE_HPROXY
    return env->NewStringUTF(HicnProxy::getProxifiedPackageName());
#else
    std::string packageName = "fd.io.hicn";
    return env->NewStringUTF(packageName.c_str());
#endif

}
