#include <jni.h>
#include <string>
#include <android/log.h>

#ifdef ENABLE_HPROXY
#include <hicn/hproxy/proxy/proxy.h>

using HicnProxy = hproxy::HicnProxy;
HicnProxy *proxy = nullptr;
#endif

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
#endif

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_initConfig(JNIEnv *env, jobject this_obj) {
#if 0
    HicnProxy *self = new HicnProxy();
    _set_self(env, this_obj, self);
#else

#ifdef ENABLE_HPROXY
    proxy = new HicnProxy();
#endif

#endif
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_isRunning(JNIEnv *env, jobject instance) {
#if 0
    HicnProxy *proxy = _get_self(env, instance);
#endif

#ifdef ENABLE_HPROXY
    return jboolean(proxy->isRunning());
#else
    return JNI_FALSE;
#endif

}

extern "C" JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_start(JNIEnv *env, jobject instance) {
#if 0
    HicnProxy *proxy = _get_self(env, instance);
#endif
#ifdef ENABLE_HPROXY
    proxy->run();
#endif
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
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_addIcnConnectorInternal(JNIEnv *env,
                                                                            jobject instance,
                                                                            jint connector_id,
                                                                            jstring consumer_name,
                                                                            jstring producer_name) {
#ifdef ENABLE_HPROXY
    // Create connector configuration object
    const char *_consumer_name = env->GetStringUTFChars(consumer_name, 0);
    const char *_producer_name = env->GetStringUTFChars(producer_name, 0);
    hproxy::connectors::ConnectorConfig config(
            ConfigConnectorType::ICN_CONNECTOR, _producer_name, _consumer_name);
    return _add_connector(env, instance, connector_id, config);
#else
    return 0;
#endif

}

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


extern "C" JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_HProxy_isHProxyEnabled(JNIEnv *env,
                                                                    jobject instance) {
#ifdef ENABLE_HPROXY
    return JNI_TRUE;
#else
    return JNI_FALSE;
#endif
}