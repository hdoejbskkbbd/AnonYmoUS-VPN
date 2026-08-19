package com.anonymous.tunnel.protocols.vless;

import android.content.Context;
import android.util.Log;
import com.anonymous.tunnel.model.ConfigProfile;
import com.anonymous.tunnel.protocols.ProtocolHandler;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.nio.ByteBuffer;

public class VLessHandler implements ProtocolHandler {

    private static final String TAG = "AnonymousTunnel";
    private final Context context;
    private WebSocketClient wsClient;
    private Callback callback;
    private boolean connected = false;

    public VLessHandler(Context context) {
        this.context = context;
    }

    @Override
    public void connect(ConfigProfile profile, Callback callback) {
        this.callback = callback;

        try {
            String wsUrl;
            if (profile.isWsEnabled()) {
                wsUrl = "ws://" + profile.getServerAddress() + ":" + profile.getServerPort() + profile.getPath();
            } else {
                wsUrl = "ws://" + profile.getServerAddress() + ":" + profile.getServerPort();
            }

            URI uri = new URI(wsUrl);

            wsClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    connected = true;
                    // Send VLess handshake
                    sendVLessHandshake(profile);
                    callback.onConnected("Anonymous");
                }

                @Override
                public void onMessage(String message) {
                    // Handle VLess protocol messages
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    // Handle binary messages
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected = false;
                    callback.onDisconnected();
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "VLess error", ex);
                    callback.onError(ex.getMessage());
                }
            };

            wsClient.addHeader("Host", profile.getHost());
            wsClient.addHeader("User-Agent", "Mozilla/5.0");
            wsClient.addHeader("Upgrade", "websocket");
            wsClient.addHeader("Connection", "Upgrade");

            wsClient.connect();

        } catch (Exception e) {
            Log.e(TAG, "VLess connection failed", e);
            callback.onError(e.getMessage());
        }
    }

    private void sendVLessHandshake(ConfigProfile profile) {
        // VLess protocol handshake implementation
        // UUID + command + target address
        try {
            byte[] uuidBytes = hexToBytes(profile.getUuid().replace("-", ""));
            // Send handshake packet
        } catch (Exception e) {
            Log.e(TAG, "Handshake failed", e);
        }
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    public void disconnect() {
        connected = false;
        if (wsClient != null) {
            wsClient.close();
        }
        if (callback != null) {
            callback.onDisconnected();
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}
