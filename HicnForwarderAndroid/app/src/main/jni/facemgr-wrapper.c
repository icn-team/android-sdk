/*
 * Copyright (c) 2019 Cisco and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <event2/event.h>
#include <android/log.h>
#include <stdbool.h>
#include <hicn/core/forwarder.h>
#include <parc/security/parc_Security.h>
#include <parc/security/parc_IdentityFile.h>

#include <parc/algol/parc_Memory.h>
#include <parc/algol/parc_SafeMemory.h>
#include <parc/algol/parc_List.h>
#include <parc/algol/parc_ArrayList.h>
#include <hicn/core/dispatcher.h>
#include <parc/algol/parc_FileOutputStream.h>
#include <parc/logging/parc_LogLevel.h>
#include <parc/logging/parc_LogReporterFile.h>
#include <parc/logging/parc_LogReporterTextStdout.h>

#include <parc/assert/parc_Assert.h>

#include <hicn/facemgr.h>
#include <hicn/policy.h>

#include <hicn/util/ip_address.h>
#include <hicn/util/log.h>

#include <hicn/facemgr/cfg.h>
#include <hicn/facemgr/api.h>
#include <hicn/facemgr/loop.h>
#include <event2/event.h>


static facemgr_cfg_t *facemgr_cfg;
static bool _isRunningFacemgr = false;
static JNIEnv *_env;
//facemgr
static loop_t *loop;
facemgr_t *facemgr;

JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_isRunningFacemgr(JNIEnv *env,
                                                                           jobject thiz) {
    return _isRunningFacemgr;
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_stopFacemgr(JNIEnv *env, jobject thiz) {
    if (_isRunningFacemgr) {
        loop_break(loop);
        _isRunningFacemgr = false;
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_startFacemgr(JNIEnv *env, jobject thiz) {


    if (!_isRunningFacemgr) {
        facemgr_face_type_t face_type = FACEMGR_FACE_TYPE_OVERLAY_UDP;
        facemgr_cfg_set_face_type(facemgr_cfg, &face_type);
        facemgr = facemgr_create_with_config(facemgr_cfg);
        JavaVM *jvm = NULL;
        (*env)->GetJavaVM(env, &jvm);
        facemgr_set_jvm(facemgr, jvm);
        loop = loop_create();
        facemgr_set_callback(facemgr, loop, (void *) loop_callback);
        facemgr_bootstrap(facemgr);
        _isRunningFacemgr = true;
        loop_dispatch(loop);
        facemgr_stop(facemgr);
        loop_undispatch(loop);
        loop_free(loop);
        facemgr_free(facemgr);
    }
}


JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_initConfig(JNIEnv *env, jobject thiz) {
    facemgr_cfg = facemgr_cfg_create();

#ifdef NDEBUG
    log_conf.log_level = LOG_INFO;
#else
    log_conf.log_level = LOG_DEBUG;
#endif
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_updateInterfaceIPv4(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jint interface_type,
                                                                              jint source_port,
                                                                              jstring next_hop_ip,
                                                                              jint next_hop_port) {

    netdevice_type_t netdevice_interface_type = (netdevice_type_t) interface_type;

    ip_address_t remote_addr;
    ip_address_t *next_hop_ip_p;
    const char *next_hop_ip_string = (*env)->GetStringUTFChars(env, next_hop_ip, 0);
    ip_address_pton(next_hop_ip_string, &remote_addr);
    next_hop_ip_p = &remote_addr;

    facemgr_cfg_rule_t *rule;
    facemgr_cfg_get_rule(facemgr_cfg, NULL, netdevice_interface_type, &rule);
    if (!rule) {
        rule = facemgr_cfg_rule_create();
        facemgr_cfg_rule_set_match(rule, NULL, netdevice_interface_type);

        facemgr_cfg_rule_set_overlay(rule, AF_INET,
                                     NULL, source_port,
                                     next_hop_ip_p, next_hop_port);
        facemgr_cfg_add_rule(facemgr_cfg, rule);
    } else {
        facemgr_cfg_rule_set_overlay(rule, AF_INET,
                                     NULL, source_port,
                                     next_hop_ip_p, next_hop_port);
    }
}


JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_updateInterfaceIPv6(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jint interface_type,
                                                                              jint source_port,
                                                                              jstring next_hop_ip,
                                                                              jint next_hop_port) {


    netdevice_type_t netdevice_interface_type = (netdevice_type_t) interface_type;


    ip_address_t remote_addr;
    ip_address_t *next_hop_ip_p;
    const char *next_hop_ip_string = (*env)->GetStringUTFChars(env, next_hop_ip, 0);
    ip_address_pton(next_hop_ip_string, &remote_addr);
    next_hop_ip_p = &remote_addr;

    facemgr_cfg_rule_t *rule;
    facemgr_cfg_get_rule(facemgr_cfg, NULL, netdevice_interface_type, &rule);
    if (!rule) {
        rule = facemgr_cfg_rule_create();
        facemgr_cfg_rule_set_match(rule, NULL, netdevice_interface_type);

        facemgr_cfg_rule_set_overlay(rule, AF_INET6,
                                     NULL, source_port,
                                     next_hop_ip_p, next_hop_port);
        facemgr_cfg_add_rule(facemgr_cfg, rule);

    } else {
        facemgr_cfg_rule_set_overlay(rule, AF_INET6,
                                     NULL, source_port,
                                     next_hop_ip_p, next_hop_port);
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_unsetInterfaceIPv4(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jint interface_type) {
    netdevice_type_t netdevice_interface_type = (netdevice_type_t) interface_type;
    facemgr_cfg_rule_t *rule;
    facemgr_cfg_get_rule(facemgr_cfg, NULL, netdevice_interface_type, &rule);
    if (rule) {
        facemgr_rule_unset_overlay(rule, AF_INET);
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_unsetInterfaceIPv6(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jint interface_type) {
    netdevice_type_t netdevice_interface_type = (netdevice_type_t) interface_type;
    facemgr_cfg_rule_t *rule;
    facemgr_cfg_get_rule(facemgr_cfg, NULL, netdevice_interface_type, &rule);
    if (rule) {
        facemgr_rule_unset_overlay(rule, AF_INET6);
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_enableDiscovery(JNIEnv *env,
                                                                          jobject thiz,
                                                                          jboolean enable_discovery) {
    facemgr_cfg_set_discovery(facemgr_cfg, enable_discovery);
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_enableIPv6(JNIEnv *env, jobject thiz,
                                                                     jint enable_ipv6) {
    int enableIPv6 = enable_ipv6;
    __android_log_print(ANDROID_LOG_DEBUG, "HicnFacemgrWrap", "enableIPv6: %d", enableIPv6);
    facemgr_cfg_set_ipv6(facemgr_cfg, enableIPv6);
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_enableIPv4(JNIEnv *env, jobject thiz,
                                                                     jint enable_ipv4) {
    int enableIPv4 = enable_ipv4;

    __android_log_print(ANDROID_LOG_DEBUG, "HicnFacemgrWrap", "enableIPv4: %d", enableIPv4);
    facemgr_cfg_set_ipv4(facemgr_cfg, enableIPv4);
}

JNIEXPORT jstring JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_Facemgr_getListFacelets(JNIEnv *env, jobject thiz) {
    jstring jstrBuffer = NULL;

    if (facemgr != NULL) {
        char *buffer;

        facemgr_list_facelets_json(facemgr, &buffer);
        jstrBuffer = (*env)->NewStringUTF(env, buffer);
        free(buffer);
    }
    return jstrBuffer;
}
