package com.cisco.hicn.forwarder.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.supportlibrary.Forwarder;
import com.cisco.hicn.forwarder.supportlibrary.HProxy;
import com.cisco.hicn.forwarder.utility.Constants;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ProxyBackend implements Runnable, Handler.Callback {
    /**
     * Maximum packet size is constrained by the MTU, which is given as a signed short.
     */
    private static final int MAX_PACKET_SIZE = 1400;

    protected static Object sHicnManager = null;
    protected static Class<?> sIHicnManagerClass = null;
    protected static boolean sHicnServiceGot = false;

    private final AtomicReference<Thread> mConnectingThread = new AtomicReference<>();

    private String mServerName = "";
    private int mServerPort = -1;
    private String mSharedSecret = "";
    private PendingIntent mConfigureIntent;
    private ParcelFileDescriptor mInterface;

    protected Service mParentService;
    protected int mTunFd = -1;
    protected Handler mHandler;
    protected Properties mParameters;
    protected HProxy mProxyInstance = null;
    protected String[] mProxiedPackages;

    public static ProxyBackend getProxyBackend(final Service parentService) {
        if (getHicnService()) {
            return new ProxyBackendNative(parentService);
        } else {
            return new ProxyBackend(parentService);
        }
    }

    public static boolean getHicnService() {
        if (HProxy.isHProxyEnabled()) {
            try {
                if (!sHicnServiceGot) {
                    Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
                    Method getServiceMethod = serviceManagerClass.getMethod("getService", new Class[]{String.class});
                    Object serviceManager = getServiceMethod.invoke(null, new Object[]{"hicn_service"});
                    Class<?> stubClass = Class.forName("com.samsung.android.hicnservice.IHicnService$Stub");
                    Method[] methodNames = stubClass.getMethods();
                    int counter = 0;
                    for (; counter < methodNames.length; counter++) {
                        if (methodNames[counter].getName().equals("asInterface"))
                            break;
                    }
                    sHicnManager = methodNames[counter].invoke(null, serviceManager);
                    if (sHicnManager != null) {
                        sIHicnManagerClass = sHicnManager.getClass();
                    }
                    sHicnServiceGot = true;
                }

                return sHicnServiceGot;

            } catch (Throwable e) {
            }
        }

        return sHicnServiceGot;
    }

    protected ProxyBackend(final Service parentService) {
        mParentService = parentService;
        mHandler = new Handler(this);

        updateForegroundNotification(R.string.hproxy_connecting);
        mHandler.sendEmptyMessage(R.string.hproxy_connecting);
        mProxyInstance = new HProxy();
    }

    @Override
    public boolean handleMessage(Message message) {
        Toast.makeText(mParentService, message.what, Toast.LENGTH_SHORT).show();
        if (message.what != R.string.hproxy_disconnected) {
            updateForegroundNotification(message.what);
        }

        return true;
    }

    public void disconnect() {
        mHandler.sendEmptyMessage(R.string.hproxy_disconnect);
        stop();

        setConnectingThread(null);
    }

    public boolean connect() {
        try {
            mParentService.getPackageManager().getPackageInfo(HProxy.getProxifiedPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            String stringMessage = HProxy.getProxifiedAppName() + " " + mParentService.getString(R.string.not_found);
            Toast.makeText(mParentService, stringMessage, Toast.LENGTH_LONG).show();
            updateForegroundNotification(stringMessage);

            disconnect();
            return false;
        }

        doConnect();
        return true;
    }

    /**
     * Optionally, set an intent to configure the VPN. This is {@code null} by default.
     */
    public void setConfigureIntent(PendingIntent intent) {
        mConfigureIntent = intent;
    }

    @Override
    public void run() {
        try {
            Log.i(getTag(), "Starting");
            final SocketAddress serverAddress = new InetSocketAddress(mServerName, mServerPort);
            for (int attempt = 0; attempt < 10; ++attempt) {
                // Reset the counter if we were connected.
                if (run(serverAddress)) {
                    attempt = 0;
                }
                // Sleep for a while. This also checks if we got interrupted.
                Thread.sleep(3000);
            }
            Log.i(getTag(), "Giving up");
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            Log.e(getTag(), "Proxy stopped, exiting", e);
        }
    }

    private void doConnect() {
        // Get an instance of the proxy
        // Replace any existing connecting thread with the  new one.
        final Thread thread = new Thread(this, "HProxyBackendThread");
        setConnectingThread(thread);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mParentService);
        mServerName = prefs.getString(mParentService.getString(R.string.hproxy_server_key), mParentService.getString(R.string.default_hproxy_server));
        final String serverPortString = prefs.getString(mParentService.getString(R.string.hproxy_server_port_key), mParentService.getString(R.string.default_hproxy_server_port));
        mServerPort = Integer.parseInt(serverPortString);
        mSharedSecret = prefs.getString(mParentService.getString(R.string.hproxy_secret_key), mParentService.getString(R.string.default_hproxy_secret));
        mProxiedPackages = new String[]{HProxy.getProxifiedPackageName()};

        thread.start();
    }

    private void setConnectingThread(final Thread thread) {
        final Thread oldThread = mConnectingThread.getAndSet(thread);
        if (oldThread != null) {
            oldThread.interrupt();
        }
    }

    private void updateForegroundNotification(final int message) {
        final String NOTIFICATION_CHANNEL_ID = "12345";
        NotificationManager mNotificationManager = (NotificationManager) mParentService.getSystemService(
                mParentService.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW));
        mParentService.startForeground(Constants.FOREGROUND_SERVICE, new Notification.Builder(mParentService, NOTIFICATION_CHANNEL_ID)
                .setContentText(mParentService.getString(message))
//                .setContentIntent(mConfigureIntent)
                .build());
    }

    private void updateForegroundNotification(final String message) {
        final String NOTIFICATION_CHANNEL_ID = "12345";
        NotificationManager mNotificationManager = (NotificationManager) mParentService.getSystemService(
                mParentService.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT));
        mParentService.startForeground(Constants.FOREGROUND_SERVICE, new Notification.Builder(mParentService, NOTIFICATION_CHANNEL_ID)
                .setContentText(message)
//                .setContentIntent(mConfigureIntent)
                .build());
    }

    private boolean run(SocketAddress server)
            throws IOException, InterruptedException, IllegalArgumentException {

        Forwarder forwarder = Forwarder.getInstance();

        while (!forwarder.isRunningForwarder()) {
            // wait for the forwarder ro run.
            Log.d(getTag(), "Hicn forwarder is not started yet. Waiting before activating the proxy.");
            TimeUnit.MILLISECONDS.sleep(500);
        }

        mProxyInstance.setProxyInstance(this);
        mProxyInstance.start(mServerName, mServerPort);

        Log.i(getTag(), "HProxy stopped.");

        mProxyInstance.destroy();

        return true;
    }

    public void stop() {
        mProxyInstance.stop();
    }

    public int configureTun(Properties parameters) {
        // If the old interface has exactly the same parameters, use it!
        if (mInterface != null && parameters.equals(mParameters)) {
            Log.i(getTag(), "Using the previous interface");
            return -1;
        }

        VpnService.Builder builder = ((VpnService)mParentService).new Builder();
        try {
            String param1 = null;
            String param2 = null;

            builder.setMtu(MAX_PACKET_SIZE);

            param1 = parameters.getProperty("ADDRESS");
            param2 = parameters.getProperty("PREFIX_LENGTH");
            builder.addAddress(param1, Integer.parseInt(param2));

            param1 = parameters.getProperty("ROUTE_ADDRESS");
            param2 = parameters.getProperty("ROUTE_PREFIX_LENGTH");
            builder.addRoute(param1, Integer.parseInt(param2));

            param1 = parameters.getProperty("DNS");
            builder.addDnsServer(param1);

        } catch (Exception e) {
            throw new IllegalArgumentException("Bad parameter");
        }


        for (String s : mProxiedPackages) {
            try {
                builder.addAllowedApplication(s);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(getTag(), HProxy.getProxifiedAppName() + " is not installed.");
            }
        }

        // Close the old interface since the parameters have been changed.
        try {
            mInterface.close();
        } catch (Exception e) {
            // ignore
        }

        // Create a new interface using the builder and save the parameters.
        mInterface = builder.setSession("HProxy VPN session. Connected to server: "
                + mServerName
                + ":"
                + mServerPort)
                .setConfigureIntent(mConfigureIntent)
                .establish();
        mParameters = parameters;

        synchronized (mParentService) {
            mInterface = builder.establish();
            mHandler.sendEmptyMessage(R.string.hproxy_connected);
        }

        mTunFd = mInterface.getFd();;

        Log.i(getTag(), "New interface: " + mInterface + " (" + parameters + ")");

        return mTunFd;
    }

    public int closeTun() {
        try {
            mInterface.close();
        } catch(IOException e) {
            Log.e(getTag(), "Closing VPN interface", e);
        }

        return 0;
    }

    private final String getTag() {
        return ProxyBackend.class.getSimpleName();
    }
}
