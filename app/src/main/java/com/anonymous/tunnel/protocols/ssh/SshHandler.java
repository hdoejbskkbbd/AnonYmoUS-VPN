package com.anonymous.tunnel.protocols.ssh;

import android.content.Context;
import android.util.Log;
import com.anonymous.tunnel.model.ConfigProfile;
import com.anonymous.tunnel.protocols.ProtocolHandler;
import com.jcraft.jsch.*;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class SshHandler implements ProtocolHandler {

    private static final String TAG = "AnonymousTunnel";
    private final Context context;
    private Session session;
    private Channel channel;
    private Callback callback;
    private boolean connected = false;

    public SshHandler(Context context) {
        this.context = context;
    }

    @Override
    public void connect(ConfigProfile profile, Callback callback) {
        this.callback = callback;

        new Thread(() -> {
            try {
                JSch jsch = new JSch();

                String host = profile.getServerAddress();
                int port = profile.getServerPort();
                String user = profile.getUsername();
                String pass = profile.getPassword();

                session = jsch.getSession(user, host, port);
                session.setPassword(pass);

                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                config.put("UserKnownHostsFile", "/dev/null");

                // Payload injection support
                if (profile.getPayload() != null && !profile.getPayload().isEmpty()) {
                    config.put("PreferredAuthentications", "password,keyboard-interactive");
                }

                session.setConfig(config);
                session.setTimeout(30000);

                session.connect();

                // Open direct TCP channel for tunneling
                channel = session.openChannel("direct-tcpip");
                ((ChannelDirectTCPIP) channel).setHost("127.0.0.1");
                ((ChannelDirectTCPIP) channel).setPort(1080);
                channel.connect();

                connected = true;

                // Get server banner - always show "Anonymous" colored
                String serverMessage = getServerBanner(session);

                callback.onConnected(serverMessage);

            } catch (Exception e) {
                Log.e(TAG, "SSH connection failed", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private String getServerBanner(Session session) {
        // Always return "Anonymous" - the colored text view will handle display
        return "Anonymous";
    }

    @Override
    public void disconnect() {
        connected = false;
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        if (callback != null) {
            callback.onDisconnected();
        }
    }

    @Override
    public boolean isConnected() {
        return connected && session != null && session.isConnected();
    }
}
