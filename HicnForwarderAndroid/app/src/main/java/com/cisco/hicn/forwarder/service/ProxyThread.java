package com.cisco.hicn.forwarder.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.cisco.hicn.forwarder.preferences.HproxyPreferencesFragment;
import com.cisco.hicn.forwarder.supportlibrary.Forwarder;
import com.cisco.hicn.forwarder.supportlibrary.HProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ProxyThread implements Runnable {

    public interface OnEstablishListener {
        void onEstablish(ParcelFileDescriptor tunInterface);
    }

    /**
     * Maximum packet size is constrained by the MTU, which is given as a signed short.
     */
    private static final int MAX_PACKET_SIZE = 1400;

    /**
     * Time to wait in between losing the connection and retrying.
     */
    private static final long RECONNECT_WAIT_MS = TimeUnit.SECONDS.toMillis(3);

    /**
     * Number of periods of length {@IDLE_INTERVAL_MS} to wait before declaring the handshake a
     * complete and abject failure.
     */
    private static final int MAX_HANDSHAKE_ATTEMPTS = 50;

    private final VpnService mService;
    private final int mConnectionId;
    private final String mServerName;
    private final int mServerPort;
    private final String mSharedSecret;
    private PendingIntent mConfigureIntent;
    private OnEstablishListener mOnEstablishListener;
    private String mHicnConsumerName;
    private String mHicnProducerName;
    private BackendProxyService mBackendProxyService;


    private HProxy mProxyInstance;
    private ParcelFileDescriptor mInterface;
    private Properties mParameters;

    public ProxyThread(final VpnService service, final int connectionId,
                       final String serverName, final int serverPort, final String secret) {
        mService = service;
        mConnectionId = connectionId;
        mServerName = serverName;
        mServerPort = serverPort;
        mSharedSecret = secret;
    }

    /**
     * Optionally, set an intent to configure the VPN. This is {@code null} by default.
     */
    public void setConfigureIntent(PendingIntent intent) {
        mConfigureIntent = intent;
    }

    public void setOnEstablishListener(OnEstablishListener listener) {
        mOnEstablishListener = listener;
    }

    @Override
    public void run() {
        try {
            Log.i(getTag(), "Starting");
            // If anything needs to be obtained using the network, get it now.
            // This greatly reduces the complexity of seamless handover, which
            // tries to recreate the tunnel without shutting down everything.
            // In this demo, all we need to know is the server address.
            final SocketAddress serverAddress = new InetSocketAddress(mServerName, mServerPort);
            // We try to create the tunnel several times.
            // TODO: The better way is to work with ConnectivityManager, trying only when the
            // network is available.
            // Here we just use a counter to keep things simple.
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

    private boolean run(SocketAddress server)
            throws IOException, InterruptedException, IllegalArgumentException {

        // If we arrived here it means no error occurred during configuration.
        // Now create the instance of the proxy and configure it.
        mProxyInstance = HProxy.getInstance();

        // start Service and block
        // Get instance of forwarder

        Forwarder forwarder = Forwarder.getInstance();

        while (!forwarder.isRunningForwarder()) {
            // wait for the forwarder ro run.
            Log.d(getTag(), "Hicn forwarder is not started yet. Waiting before activating the proxy.");
            TimeUnit.MILLISECONDS.sleep(500);
        }

        mProxyInstance.setProxyInstance(this);
        mProxyInstance.start(mServerName, mServerPort);

        Log.i(getTag(), "HProxy stopped.");

        return true;
    }

    public int configureTun(Properties parameters) {
        // If the old interface has exactly the same parameters, use it!
        if (mInterface != null && parameters.equals(mParameters)) {
            Log.i(getTag(), "Using the previous interface");
            return -1;
        }

        VpnService.Builder builder = mService.new Builder();
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

        PackageManager packageManager = mService.getPackageManager();
        HProxy hproxy = HProxy.getInstance();
        try {
            packageManager.getPackageInfo(hproxy.getProxifiedPackageName(), 0);
            builder.addAllowedApplication(hproxy.getProxifiedPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(getTag(), hproxy.getProxifiedAppName() + " is not installed.");
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

        synchronized (mService) {
            mInterface = builder.establish();
            if (mOnEstablishListener != null) {
                mOnEstablishListener.onEstablish(mInterface);
            }
        }

        Log.i(getTag(), "New interface: " + mInterface + " (" + parameters + ")");

        return mInterface.getFd();
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
        return ProxyThread.class.getSimpleName() + "[" + mConnectionId + "]";
    }
}
