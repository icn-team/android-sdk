#include <jni.h>
#include <string>
#include <android/log.h>

#ifdef ENABLE_HPROXY

#include <hicn/hproxy/proxy/proxy.h>

using HicnProxy = hproxy::HicnProxy;

static HicnProxy *proxy = nullptr;
static JNIEnv *_env;
static jobject *_instance;
#endif

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_initConfig(JNIEnv *env, jobject this_obj) {

}

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_start(JNIEnv *env, jobject instance,
                                                          jstring remote_address,
                                                          jint remote_port) {
#ifdef ENABLE_HPROXY
    _env = env;
    _instance = &instance;

    const char *_remote_address = env->GetStringUTFChars(remote_address, 0);

    hproxy::connectors::ConnectorConfig config_connector(
            ConfigConnectorType::UDP_TUNNEL_CONNECTOR, _remote_address, std::to_string(remote_port),
            std::string("0.0.0.0"), "udp-tunnel");

    uint64_t secret = 12345678910;
    hproxy::config::ClientConfiguration config_automation;
    config_automation.secret = secret;
    proxy = HicnProxy::createAsClient(config_connector, config_automation).release();
    proxy->run();
#endif
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_isRunning(JNIEnv *env, jobject instance) {
#ifdef ENABLE_HPROXY
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
    jmethodID methodID = env->GetMethodID(clazz, "createTunDevice", "(Ljava/lang/String;ILjava/lang/String;ILjava/lang/String;)I");

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

extern "C" int createTunDevice(const char *vpn_address, uint16_t prefix_length,
                    const char *route_address,
                    uint16_t route_prefix_length, const char *dns) {
#ifdef ENABLE_HPROXY
    if (!_env || !_instance) {
        __android_log_print(ANDROID_LOG_ERROR, "HProxyWrap",
                            "Call createTunDevice, but _env and _instance variables are not initialized.");
        return -1;
    }

    return createTunDeviceWrap(_env, *_instance, vpn_address, prefix_length, route_address,
                               route_prefix_length, dns);
#else
    return 0;
#endif
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_isHProxyEnabled(JNIEnv *env,
                                                                    jobject instance) {
#ifdef ENABLE_HPROXY
    return JNI_TRUE;
#else
    return JNI_FALSE;
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_stop(JNIEnv *env, jobject instance) {
#ifdef ENABLE_HPROXY
    if (proxy) {
      proxy->stop();
    }
#endif
}
