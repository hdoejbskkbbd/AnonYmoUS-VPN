package com.anonymous.tunnel.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.anonymous.tunnel.AnonymousTunnelApp;
import com.anonymous.tunnel.R;
import com.anonymous.tunnel.model.ConfigProfile;
import com.anonymous.tunnel.protocols.ProtocolHandler;
import com.anonymous.tunnel.protocols.ssh.SshHandler;
import com.anonymous.tunnel.protocols.vless.VLessHandler;
import com.anonymous.tunnel.protocols.vmess.VMessHandler;
import com.anonymous.tunnel.protocols.trojan.TrojanHandler;
import com.anonymous.tunnel.protocols.shadowsocks.ShadowsocksHandler;
import com.anonymous.tunnel.protocols.dnstt.DnsttHandler;
import com.anonymous.tunnel.utils.ConfigManager;
import com.anonymous.tunnel.utils.PreferenceManager;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VpnTunnelService extends VpnService {

    private static final String TAG = "AnonymousTunnel";
    private static final int NOTIFICATION_ID = 1001;

    private ParcelFileDescriptor vpnInterface;
    private ExecutorService executor;
    private Handler mainHandler;
    private ProtocolHandler protocolHandler;
    private ConfigProfile currentProfile;
    private PreferenceManager prefs;
    private boolean isRunning = false;

    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("STOP_VPN".equals(intent.getAction())) {
                stopVpn();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        prefs = new PreferenceManager(this);

        IntentFilter filter = new IntentFilter("STOP_VPN");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stopReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(stopReceiver, filter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if ("START".equals(action)) {
            String profileId = intent.getStringExtra("profile_id");
            if (profileId != null) {
                currentProfile = ConfigManager.getInstance(this).getProfileById(profileId);
                if (currentProfile != null) {
                    startVpn();
                }
            }
        } else if ("STOP".equals(action)) {
            stopVpn();
        }

        return START_NOT_STICKY;
    }

    private void startVpn() {
        if (isRunning) return;

        isRunning = true;
        broadcastState("CONNECTING", null);
        startForeground(NOTIFICATION_ID, buildNotification("Connecting..."));

        executor.execute(() -> {
            try {
                Builder builder = new Builder();
                builder.setSession("AnonymousTunnel")
                       .addAddress("10.0.0.2", 24)
                       .addDnsServer("1.1.1.1")
                       .addDnsServer("1.0.0.1")
                       .addRoute("0.0.0.0", 0);

                if (prefs.isSplitTunnelingEnabled()) {
                    for (String pkg : prefs.getBypassApps()) {
                        builder.addDisallowedApplication(pkg);
                    }
                }

                builder.setMtu(prefs.getMtu());
                vpnInterface = builder.establish();

                if (vpnInterface == null) {
                    throw new IOException("Failed to establish VPN interface");
                }

                protocolHandler = createProtocolHandler(currentProfile);

                if (protocolHandler != null) {
                    protocolHandler.connect(currentProfile, new ProtocolHandler.Callback() {
                        @Override
                        public void onConnected(String serverMessage) {
                            mainHandler.post(() -> {
                                broadcastState("CONNECTED", serverMessage);
                                updateNotification("Connected - " + currentProfile.getName());
                            });
                        }

                        @Override
                        public void onDisconnected() {
                            mainHandler.post(() -> {
                                broadcastState("DISCONNECTED", null);
                                stopVpn();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            mainHandler.post(() -> {
                                broadcastState("ERROR", error);
                                stopVpn();
                            });
                        }

                        @Override
                        public void onDataTransferred(long sent, long received) {
                            // Update stats
                        }
                    });

                    startDataRelay();
                }

            } catch (Exception e) {
                Log.e(TAG, "VPN start failed", e);
                mainHandler.post(() -> {
                    broadcastState("ERROR", e.getMessage());
                    stopVpn();
                });
            }
        });
    }

    private ProtocolHandler createProtocolHandler(ConfigProfile profile) {
        switch (profile.getProtocolType()) {
            case SSH:
                return new SshHandler(this);
            case VLESS:
                return new VLessHandler(this);
            case VMESS:
                return new VMessHandler(this);
            case TROJAN:
                return new TrojanHandler(this);
            case SHADOWSOCKS:
                return new ShadowsocksHandler(this);
            case DNSTT:
                return new DnsttHandler(this);
            default:
                return null;
        }
    }

    private void startDataRelay() {
        executor.execute(() -> {
            try {
                while (isRunning && vpnInterface != null) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void stopVpn() {
        isRunning = false;

        if (protocolHandler != null) {
            protocolHandler.disconnect();
            protocolHandler = null;
        }

        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing VPN interface", e);
            }
            vpnInterface = null;
        }

        broadcastState("DISCONNECTED", null);
        stopForeground(true);
        stopSelf();
    }

    private void broadcastState(String state, String message) {
        Intent intent = new Intent("connection_state");
        intent.putExtra("state", state);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private Notification buildNotification(String content) {
        Intent intent = new Intent(this, com.anonymous.tunnel.ui.activities.MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent("STOP_VPN");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, AnonymousTunnelApp.CHANNEL_ID)
            .setContentTitle("AnonymousTunnel")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_vpn)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, "Disconnect", stopPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build();
    }

    private void updateNotification(String content) {
        startForeground(NOTIFICATION_ID, buildNotification(content));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVpn();
        executor.shutdown();
        unregisterReceiver(stopReceiver);
    }
}
