package com.anonymous.tunnel.protocols.shadowsocks;

import android.content.Context;
import android.util.Log;
import com.anonymous.tunnel.model.ConfigProfile;
import com.anonymous.tunnel.protocols.ProtocolHandler;

public class ShadowsocksHandler implements ProtocolHandler {

    private static final String TAG = "AnonymousTunnel";
    private final Context context;
    private Callback callback;
    private boolean connected = false;

    public ShadowsocksHandler(Context context) {
        this.context = context;
    }

    @Override
    public void connect(ConfigProfile profile, Callback callback) {
        this.callback = callback;
        new Thread(() -> {
            try {
                Thread.sleep(500);
                connected = true;
                callback.onConnected("Anonymous");
            } catch (Exception e) {
                Log.e(TAG, "Shadowsocks error", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    @Override
    public void disconnect() {
        connected = false;
        if (callback != null) callback.onDisconnected();
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}
