package icn.forwarder.com.supportlibrary;

public class Forwarder {

    private static Forwarder sInstance = null;

    static {
        System.loadLibrary("forwarderWrap");
    }

    public static Forwarder getInstance() {
        if (sInstance == null) {
            sInstance = new Forwarder();
        }
        return sInstance;
    }

    private Forwarder() {

    }

    public native boolean isRunning();
    public native void start(String path);
    public native void stop();

}
