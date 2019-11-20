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
            std::string("0.0.0.0"));

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
    return jboolean(proxy->isRunning());
#else
    return JNI_FALSE;
#endif
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
    if (!_env || !_instance) {
        __android_log_print(ANDROID_LOG_ERROR, "HProxyWrap",
                            "Call createTunDevice, but _env and _instance variables are not initialized.");
        return -1;
    }

    return createTunDeviceWrap(_env, *_instance, vpn_address, prefix_length, route_address,
                               route_prefix_length, dns);
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
    proxy->stop();
#endif
}


# if 0
static jfieldID _get_self_id(JNIEnv *env, jobject this_obj) {
    static int init = 0;
    static jfieldID fid_self_ptr;

    if (!init) {
        jclass this_class = env->GetObjectClass(this_obj);
        fid_self_ptr = env->GetFieldID(this_class, "self_ptr", "J");
    }

    return fid_self_ptr;
}

static HicnProxy *_get_self(JNIEnv *env, jobject this_obj) {
    jlong self_ptr = env->GetLongField(this_obj, _get_self_id(env, this_obj));
    return (HicnProxy *) &self_ptr;
}

static void _set_self(JNIEnv *env, jobject this_obj, HicnProxy *self) {
    jlong self_ptr = *(jlong *) &self;
    env->SetLongField(this_obj, _get_self_id(env, this_obj), self_ptr);
}








extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_stop(JNIEnv *env, jobject instance) {
//    HicnProxy *proxy = _get_self(env, instance);
//    proxy->stop();
}

#ifdef ENABLE_HPROXY
static jint _add_connector(JNIEnv *env,
                           jobject instance, int connector_id,
                           hproxy::connectors::ConnectorConfig &config) {
#if 0
    // Create the new connector
    HicnProxy *proxy = _get_self(env, instance);
#endif
    return jint(proxy->createConnector(connector_id, config));
}

#endif

extern "C" JNIEXPORT jint JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_addUdpTunnelConnectorInternal(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jint connector_id,
                                                                                  jstring remote_address,
                                                                                  jstring remote_port) {
#ifdef ENABLE_HPROXY
    // Create connector configuration object
    const char *_remote_address = env->GetStringUTFChars(remote_address, 0);
    const char *_remote_port = env->GetStringUTFChars(remote_port, 0);
    hproxy::connectors::ConnectorConfig config(
            ConfigConnectorType::UDP_TUNNEL_CONNECTOR, _remote_address, _remote_port, "0.0.0.0");
    return _add_connector(env, instance, connector_id, config);
#else
    return 0;
#endif
}

extern "C" JNIEXPORT jint JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_addVpnConnectorInternal(JNIEnv *env,
                                                                            jobject instance,
                                                                            jint connector_id,
                                                                            jint fd) {
#ifdef ENABLE_HPROXY
    // Create connector configuration object
    hproxy::connectors::ConnectorConfig config(
            ConfigConnectorType::ANDROID_VPN_CONNECTOR, fd);
    return _add_connector(env, instance, connector_id, config);
#else
    return 0;
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_linkTunToConnector(JNIEnv *env,
                                                                       jobject instance,
                                                                       jint connector_id_0,
                                                                       jint connector_id_1) {
#if 0
    HicnProxy *proxy = _get_self(env, instance);
#endif
#ifdef ENABLE_HPROXY
    hproxy::DemuxInfo demuxInfo(0, 0);
    proxy->linkFlowToConnector(Connector::Id(connector_id_0), demuxInfo,
                               Connector::Id(connector_id_1));
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_linkConnectorsInternal(JNIEnv *env,
                                                                           jobject instance,
                                                                           jint connector_id_0,
                                                                           jint connector_id_1) {
#if 0
    HicnProxy *proxy = _get_self(env, instance);
#endif
#ifdef ENABLE_HPROXY
    proxy->linkConnector(
            Connector::Id(connector_id_0), Connector::Id(connector_id_1));
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_removeConnectorInternal(JNIEnv *env,
                                                                            jobject instance,
                                                                            jint connector_id) {
    // Do remove connector from the proxy
}



#endif