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
static bool _isRunning = false;
static bool _isRunningFacemgr = false;
static JNIEnv *_env;
static jobject *_instance;
//forwarder
Forwarder *hicnFwd = NULL;
//facemgr
static loop_t *loop;


int
loop_unregister_event(struct event_base *loop, struct event *event) {
    if (!event)
        return 0;

    event_del(event);
    event_free(event);

    return 0;
}


JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_isRunningForwarder(JNIEnv *env,
                                                                             jobject instance) {
    return _isRunning;
}

static void _setLogLevelToLevel(int logLevelArray[LoggerFacility_END],
                                LoggerFacility facility,
                                const char *levelString) {
    PARCLogLevel level = parcLogLevel_FromString(levelString);

    if (level < PARCLogLevel_All) {
        // we have a good facility and level
        logLevelArray[facility] = level;
    } else {
        printf("Invalid log level string %s\n", levelString);
    }
}

/**
 * string: "facility=level"
 * Set the right thing in the logger
 */
static void _setLogLevel(int logLevelArray[LoggerFacility_END],
                         const char *string) {
    char *tofree = parcMemory_StringDuplicate(string, strlen(string));
    char *p = tofree;

    char *facilityString = strtok(p, "=");
    if (facilityString) {
        char *levelString = strtok(NULL, "=");

        if (strcasecmp(facilityString, "all") == 0) {
            for (LoggerFacility facility = 0; facility < LoggerFacility_END;
                 facility++) {
                _setLogLevelToLevel(logLevelArray, facility, levelString);
            }
        } else {
            LoggerFacility facility;
            for (facility = 0; facility < LoggerFacility_END; facility++) {
                if (strcasecmp(facilityString, logger_FacilityString(facility)) == 0) {
                    break;
                }
            }

            if (facility < LoggerFacility_END) {
                _setLogLevelToLevel(logLevelArray, facility, levelString);
            } else {
                printf("Invalid facility string %s\n", facilityString);
            }
        }
    }

    parcMemory_Deallocate((void **) &tofree);
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_startForwarder(JNIEnv *env,
                                                                         jobject instance,
                                                                         jint capacity) {


    if (!_isRunning) {
        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap", "starting HicnFwd...");
        _env = env;
        _instance = &instance;

        Logger *logger = NULL;

        PARCLogReporter *stdoutReporter = parcLogReporterTextStdout_Create();
        logger = logger_Create(stdoutReporter, parcClock_Wallclock());
        parcLogReporter_Release(&stdoutReporter);
        int logLevelArray[LoggerFacility_END];

#ifdef NDEBUG
        _setLogLevel(logLevelArray, "all=info");
#else
        _setLogLevel(logLevelArray, "all=debug");
#endif

        for (int i = 0; i < LoggerFacility_END; i++) {
            if (logLevelArray[i] > -1) {
                logger_SetLogLevel(logger, i, logLevelArray[i]);
            }
        }


        hicnFwd = forwarder_Create(logger);
        Configuration *configuration = forwarder_GetConfiguration(hicnFwd);
        if (capacity >= 0) {
            configuration_SetObjectStoreSize(configuration, capacity);
        }
        forwarder_SetupLocalListeners(hicnFwd, PORT_NUMBER);
        Dispatcher *dispatcher = forwarder_GetDispatcher(hicnFwd);
        _isRunning = true;
        dispatcher_Run(dispatcher);
        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap", "HicnFwd stopped...");
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_stopForwarder(JNIEnv *env,
                                                                        jobject instance) {
    if (_isRunning) {
        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap", "stopping HicnFwd...");
        dispatcher_Stop(forwarder_GetDispatcher(hicnFwd));
        sleep(2);
        forwarder_Destroy(&hicnFwd);
        _isRunning = false;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_isRunningFacemgr(JNIEnv *env,
                                                                           jobject thiz) {
    return _isRunningFacemgr;
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_stopFacemgr(JNIEnv *env, jobject thiz) {
    if (_isRunningFacemgr) {
        loop_break(loop);
        _isRunningFacemgr = false;
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_startFacemgr(JNIEnv *env, jobject thiz) {


    if (!_isRunningFacemgr) {
        facemgr_face_type_t face_type = FACEMGR_FACE_TYPE_OVERLAY_UDP;
        facemgr_cfg_set_face_type(facemgr_cfg, &face_type);
        facemgr_t *facemgr = facemgr_create_with_config(facemgr_cfg);
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
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_initConfig(JNIEnv *env, jobject thiz) {
    facemgr_cfg = facemgr_cfg_create();

#ifdef NDEBUG
    log_conf.log_level = LOG_INFO;
#else
    log_conf.log_level = LOG_DEBUG;
#endif
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_updateInterfaceIPv4(JNIEnv *env,
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
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_updateInterfaceIPv6(JNIEnv *env,
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
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_unsetInterfaceIPv4(JNIEnv *env,
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
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_unsetInterfaceIPv6(JNIEnv *env,
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
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_enableDiscovery(JNIEnv *env,
                                                                          jobject thiz,
                                                                          jboolean enable_discovery) {
    facemgr_cfg_set_discovery(facemgr_cfg, enable_discovery);
}

static bool bindSocketWrap(JNIEnv *env, jobject instance, int sock, const char *ifname) {
    jclass clazz = (*env)->GetObjectClass(env, instance);
    jmethodID methodID = (*env)->GetMethodID(env, clazz, "bindSocket", "(ILjava/lang/String;)Z");
    bool ret = false;
    if (methodID) {
        jstring ifnameStr = (*env)->NewStringUTF(env, ifname);
        ret = (*env)->CallBooleanMethod(env, instance, methodID, sock, ifnameStr);
    }
    return ret;
}

int bindSocket(int sock, const char *ifname) {
    if (!_env || !_instance) {
        __android_log_print(ANDROID_LOG_ERROR, "HicnFwdWrap",
                            "Call bindSocket, but JNI env/instance variables are not initialized.");
        return -1;
    }
    return bindSocketWrap(_env, *_instance, sock, ifname) ? 0 : -1;
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_enableIPv6(JNIEnv *env, jobject thiz,
                                                                     jint enable_ipv6) {
    int enableIPv6 = enable_ipv6;
    __android_log_print(ANDROID_LOG_DEBUG, "HicnFacemgrWrap", "enableIPv6: %d", enableIPv6);
    facemgr_cfg_set_ipv6(facemgr_cfg, enableIPv6);
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_enableIPv4(JNIEnv *env, jobject thiz,
                                                                     jint enable_ipv4) {
    int enableIPv4 = enable_ipv4;

    __android_log_print(ANDROID_LOG_DEBUG, "HicnFacemgrWrap", "enableIPv4: %d", enableIPv4);
    facemgr_cfg_set_ipv4(facemgr_cfg, enableIPv4);
}
