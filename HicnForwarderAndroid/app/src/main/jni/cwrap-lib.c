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
#include <hicn/facemgr/cfg.h>

#include <event2/event.h>


static facemgr_cfg_t *facemgr_cfg;
static bool _isRunning = false;
static bool _isRunningFacemgr = false;
//forwarder
Forwarder *hicnFwd = NULL;
//facemgr
static struct event_base *loop;

//facemgr_t *facemgr;

typedef struct {
    void (*cb)(void *, ...);

    void *args;
} cb_wrapper_args_t;

void cb_wrapper(evutil_socket_t fd, short what, void *arg) {
    cb_wrapper_args_t *cb_wrapper_args = arg;
    cb_wrapper_args->cb(cb_wrapper_args->args);
}

int
loop_unregister_event(struct event_base *loop, struct event *event) {
    if (!event)
        return 0;

    event_del(event);
    event_free(event);

    return 0;
}


struct event *
loop_register_fd(struct event_base *loop, int fd, void *cb, void *cb_args) {
    // TODO: not freed
    cb_wrapper_args_t *cb_wrapper_args = malloc(sizeof(cb_wrapper_args_t));
    *cb_wrapper_args = (cb_wrapper_args_t) {
            .cb = cb,
            .args = cb_args,
    };

    evutil_make_socket_nonblocking(fd);
    struct event *event = event_new(loop, fd, EV_READ | EV_PERSIST, cb_wrapper, cb_wrapper_args);
    if (!event)
        goto ERR_EVENT_NEW;

    if (event_add(event, NULL) < 0)
        goto ERR_EVENT_ADD;

    return event;

    ERR_EVENT_ADD:
    event_free(event);
    ERR_EVENT_NEW:
    return NULL;
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


    //TODO remove
    ////   jclass clazz = (*env)->FindClass(env, "com/cisco/hicn/forwarder/supportlibrary/AndroidUtility");
    //JavaVM *jvm = NULL;
    //(*env)->GetJavaVM(env, &jvm);
    ////   jmethodID getNetworkType = (*env)->GetStaticMethodID(env, clazz, "getNetworkType", "(Ljava/lang/String;)I");
    ////   jint aaa = (*env)->CallStaticIntMethod(env, clazz, getNetworkType,
    ////                                          (*env)->NewStringUTF(env, "wlan0"));

    ////    aaa = (*env)->CallStaticIntMethod(env, clazz, getNetworkType,
    ////                                          (*env)->NewStringUTF(env, "radio0"));


    ////    JavaVM *jvm = NULL;
    //(*env)->GetJavaVM(env, &jvm);


    //pass jvm to your library as a parameter

    //jclass class = (jclass)((*env)->NewGlobalRef(env, clazz)));
    //pass class to your library as parameter


    //if you want to call the method in your library:
    //JNIEnv *env;
    //(*jvm)->AttachCurrentThread(jvm, &env, NULL);
    //get the method reference
    //jmethodID getNetworkType = (*env)->GetStaticMethodID(env, clazz, "getNetworkType", "(Ljava/lang/String;)I");
    //call the static method
    //jint aaa = (*env)->CallStaticIntMethod(env, clazz, getNetworkType,
    //                                           (*env)->NewStringUTF(env, "wlan0"));


    ///end remove



    if (!_isRunning) {
        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap", "starting HicnFwd...");

        Logger *logger = NULL;

        PARCLogReporter *stdoutReporter = parcLogReporterTextStdout_Create();
        logger = logger_Create(stdoutReporter, parcClock_Wallclock());
        parcLogReporter_Release(&stdoutReporter);
        int logLevelArray[LoggerFacility_END];
        /*_setLogLevel(logLevelArray, "all=debug");

        for (int i = 0; i < LoggerFacility_END; i++) {
            if (logLevelArray[i] > -1) {
                logger_SetLogLevel(logger, i, logLevelArray[i]);
            }
        }*/

        hicnFwd = forwarder_Create(logger);
        Configuration *configuration = forwarder_GetConfiguration(hicnFwd);
        if (capacity >= 0) {
            configuration_SetObjectStoreSize(configuration, capacity);
        }
        forwarder_SetupLocalListeners(hicnFwd, PORT_NUMBER);
        /*if (path_) {
            const char *configFileName = (*env)->GetStringUTFChars(env, path_, 0);
            FILE *file = fopen(configFileName, "rb");
            char row[255];
            while (fgets(row, sizeof(row), file) != NULL) {
                __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap", "log file %s", row);
            }

            fclose(file);

            //forwarder_SetupAllListeners(hicnFwd, PORT_NUMBER, NULL);
            forwarder_SetupFromConfigFile(hicnFwd, configFileName);
        }*/
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
        event_base_loopbreak(loop);
        sleep(2);
        loop = NULL;
        _isRunningFacemgr = false;
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_startFacemgr(JNIEnv *env, jobject thiz) {


    if (!_isRunningFacemgr) {
        facemgr_t *facemgr = facemgr_create();
        loop = event_base_new();
        facemgr_set_event_loop_handler(facemgr, loop, loop_register_fd, loop_unregister_event);
        facemgr_bootstrap(facemgr);
        _isRunningFacemgr = true;
        event_base_dispatch(loop);
        facemgr_stop(facemgr);
        facemgr_free(facemgr);

    }
    //facemgr =



    /*while (_isRunningFacemgr) {
        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap", "loop!");
        sleep(1);
    }*/

}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_startFacemgrWithConfig(JNIEnv *env,
                                                                                 jobject thiz,
                                                                                 jstring next_hop_ip_v4_wifi,
                                                                                 jint next_hop_port_ip_v4_wifi,
                                                                                 jstring next_hop_ip_v6_wifi,
                                                                                 jint next_hop_port_ip_v6_wifi,
                                                                                 jstring next_hop_ip_v4_radio,
                                                                                 jint next_hop_port_ip_v4_radio,
                                                                                 jstring next_hop_ip_v6_radio,
                                                                                 jint next_hop_port_ip_v6_radio,
                                                                                 jstring next_hop_ip_v4_wired,
                                                                                 jint next_hop_port_ip_v4_wired,
                                                                                 jstring next_hop_ip_v6_wired,
                                                                                 jint next_hop_port_ip_v6_wired) {

    const char *nextHopIpV4Wifi = (*env)->GetStringUTFChars(env, next_hop_ip_v4_wifi, 0);
    const char *nextHopIpV6Wifi = (*env)->GetStringUTFChars(env, next_hop_ip_v6_wifi, 0);
    const char *nextHopIpV4Radio = (*env)->GetStringUTFChars(env, next_hop_ip_v4_radio, 0);
    const char *nextHopIpV6Radio = (*env)->GetStringUTFChars(env, next_hop_ip_v6_radio, 0);
    const char *nextHopIpV4Wired = (*env)->GetStringUTFChars(env, next_hop_ip_v4_wired, 0);
    const char *nextHopIpV6Wired = (*env)->GetStringUTFChars(env, next_hop_ip_v6_wired, 0);

    if (!_isRunningFacemgr) {
        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap",
                            "nextHopIpV4Wifi: %s, nextHopPortIpV4Wifi: %d",
                            nextHopIpV4Wifi, next_hop_port_ip_v4_wifi);

        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap",
                            "nextHopIpV6Wifi: %s, nextHopPortIpV6Wifi: %d",
                            nextHopIpV6Wifi, next_hop_port_ip_v6_wifi);

        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap",
                            "nextHopIpV4Radio: %s, nextHopPortIpV4Radio: %d",
                            nextHopIpV4Radio, next_hop_port_ip_v4_radio);

        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap",
                            "nextHopIpV6Radio: %s, nextHopPortIpV6Radio: %d",
                            nextHopIpV6Radio, next_hop_port_ip_v6_radio);

        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap",
                            "nextHopIpV4Wired: %s, nextHopPortIpV4Wired: %d",
                            nextHopIpV4Wired, next_hop_port_ip_v4_wired);

        __android_log_print(ANDROID_LOG_DEBUG, "HicnFwdWrap",
                            "nextHopIpV6Wired: %s, nextHopPortIpV6Wired: %d",
                            nextHopIpV6Wired, next_hop_port_ip_v6_wired);


        facemgr_t *facemgr = facemgr_create();
        loop = event_base_new();
        facemgr_set_event_loop_handler(facemgr, loop, loop_register_fd, loop_unregister_event);
        facemgr_overlay_t *overlay = malloc(sizeof(facemgr_overlay_t));
        *overlay = FACEMGR_OVERLAY_EMPTY;


#ifndef DEBUG
#endif

        //WIFI
        overlay->v4.local_port = 9695;
        ip_address_pton(nextHopIpV4Wifi, &overlay->v4.remote_addr);
        overlay->v4.remote_port = next_hop_port_ip_v4_wifi;
        overlay->v6.local_port = 9695;
        ip_address_pton(nextHopIpV6Wifi, &overlay->v6.remote_addr);
        overlay->v6.remote_port = next_hop_port_ip_v6_wifi;
//        facemgr_add_overlay(facemgr, "wlan0", overlay);

        //LTE
        overlay = malloc(sizeof(facemgr_overlay_t));
        *overlay = FACEMGR_OVERLAY_EMPTY;

        overlay->v4.local_port = 9695;
        ip_address_pton(nextHopIpV4Radio, &overlay->v4.remote_addr);
        overlay->v4.remote_port = next_hop_port_ip_v4_radio;
        overlay->v6.local_port = 9695;
        ip_address_pton(nextHopIpV6Radio, &overlay->v6.remote_addr);
        overlay->v6.remote_port = next_hop_port_ip_v6_radio;
        //     facemgr_add_overlay(facemgr, "radio0", overlay);


        //WIRED
        overlay = malloc(sizeof(facemgr_overlay_t));
        *overlay = FACEMGR_OVERLAY_EMPTY;

        overlay->v4.local_port = 9695;
        ip_address_pton(nextHopIpV4Wired, &overlay->v4.remote_addr);
        overlay->v4.remote_port = next_hop_port_ip_v4_wired;
        overlay->v6.local_port = 9695;
        ip_address_pton(nextHopIpV6Wired, &overlay->v6.remote_addr);
        overlay->v6.remote_port = next_hop_port_ip_v6_wired;
        //      facemgr_add_overlay(facemgr, "eth0", overlay);


        facemgr_bootstrap(facemgr);
        _isRunningFacemgr = true;
        event_base_dispatch(loop);

        facemgr_stop(facemgr);
        sleep(1);
        facemgr_free(facemgr);
    }
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_initConfig(JNIEnv *env, jobject thiz) {
    facemgr_cfg = facemgr_cfg_create();
    // TODO: implement initConfig()
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_updateInterfaceIPv4(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jint interface_type,
                                                                              jint source_port,
                                                                              jstring next_hop_ip,
                                                                              jint next_hop_port) {
    
    facemgr_cfg_rule_t *rule;
    rule = facemgr_cfg_rule_create();
    netdevice_type_t netdevice_interface_type = NETDEVICE_TYPE_UNDEFINED;
    switch (interface_type) {
        case 0:
            netdevice_interface_type = NETDEVICE_TYPE_WIFI;
            break;
        case 1:
            netdevice_interface_type = NETDEVICE_TYPE_CELLULAR;
            break;
        case 2:
            netdevice_interface_type = NETDEVICE_TYPE_WIRED;
            break;
        default:
            netdevice_interface_type = NETDEVICE_TYPE_UNDEFINED;
    }
    facemgr_cfg_rule_set_match(rule, NULL, netdevice_interface_type);
    const char *next_hop_ip_string = (*env)->GetStringUTFChars(env, next_hop_ip, 0);


    ip_address_t remote_addr;
    ip_address_t *next_hop_ip_p;
    ip_address_pton(next_hop_ip_string, &remote_addr);
    next_hop_ip_p = &remote_addr;
    facemgr_cfg_set_overlay(facemgr_cfg, AF_INET,
                            NULL, source_port,
                            next_hop_ip_p, next_hop_port);
    facemgr_cfg_add_rule(facemgr_cfg, rule);
}


JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_updateInterfaceIPv6(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jint interface_type,
                                                                              jint source_port,
                                                                              jstring next_hop_ip,
                                                                              jint next_hop_port) {

    facemgr_cfg_rule_t *rule;
    rule = facemgr_cfg_rule_create();
    netdevice_type_t netdevice_interface_type = NETDEVICE_TYPE_UNDEFINED;
    switch (interface_type) {
        case 0:
            netdevice_interface_type = NETDEVICE_TYPE_WIFI;
            break;
        case 1:
            netdevice_interface_type = NETDEVICE_TYPE_CELLULAR;
            break;
        case 2:
            netdevice_interface_type = NETDEVICE_TYPE_WIRED;
            break;
        default:
            netdevice_interface_type = NETDEVICE_TYPE_UNDEFINED;
    }
    facemgr_cfg_rule_set_match(rule, NULL, netdevice_interface_type);
    const char *next_hop_ip_string = (*env)->GetStringUTFChars(env, next_hop_ip, 0);
    ip_address_t remote_addr;
    ip_address_t *next_hop_ip_p;
    ip_address_pton(next_hop_ip_string, &remote_addr);
    next_hop_ip_p = &remote_addr;

    facemgr_cfg_set_overlay(facemgr_cfg, AF_INET6,
                            NULL, source_port,
                            next_hop_ip_p, next_hop_port);
    facemgr_cfg_add_rule(facemgr_cfg, rule);
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_disableDiscovery(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jboolean disable_discovery) {
    facemgr_cfg_rule_t *rule;
    rule = facemgr_cfg_rule_create();
    facemgr_cfg_rule_set_discovery(rule, disable_discovery);
    facemgr_cfg_add_rule(facemgr_cfg, rule);
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_disableIPv4(JNIEnv *env, jobject thiz,
                                                                      jboolean disable_ipv4) {

#if 0
    facemgr_cfg_rule_t *rule;
    rule = facemgr_cfg_rule_create();
    facemgr_cfg_set_ipv4(rule, disable_ipv4);
    facemgr_cfg_add_rule(facemgr_cfg, rule);
#endif
}

JNIEXPORT void JNICALL
Java_com_cisco_hicn_forwarder_supportlibrary_NativeAccess_disableIPv6(JNIEnv *env, jobject thiz,
                                                                      jboolean disable_ipv6) {

#if 0
    facemgr_cfg_rule_t *rule;
    rule = facemgr_cfg_rule_create();
    facemgr_cfg_set_ipv4(rule, disable_ipv6);
    facemgr_cfg_add_rule(facemgr_cfg, rule);
#endif
}