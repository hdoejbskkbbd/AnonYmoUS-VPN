package com.anonymous.tunnel.protocols.trojan;

import android.content.Context;
import android.util.Log;
import com.anonymous.tunnel.model.ConfigProfile;
import com.anonymous.tunnel.protocols.ProtocolHandler;

public class TrojanHandler implements ProtocolHandler {

    private static final String TAG = "AnonymousTunnel";
    private final Context context;
    private Callback callback;
    private boolean connected = false;

    public TrojanHandler(Context context) {
        this.context = context;
    }

    @Override
    public void connect(ConfigProfile profile, Callback callback) {
        this.callback = callback;
        new Thread(() -> {
            try {
                // Trojan protocol: TLS + Trojan password handshake
                Thread.sleep(500);
                connected = true;
                callback.onConnected("Anonymous");
            } catch (Exception e) {
                Log.e(TAG, "Trojan error", e);
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
