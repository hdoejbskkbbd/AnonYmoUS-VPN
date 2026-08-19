package com.anonymous.tunnel.protocols;

import com.anonymous.tunnel.model.ConfigProfile;

public interface ProtocolHandler {
    void connect(ConfigProfile profile, Callback callback);
    void disconnect();
    boolean isConnected();

    interface Callback {
        void onConnected(String serverMessage);
        void onDisconnected();
        void onError(String error);
        void onDataTransferred(long sent, long received);
    }
}
