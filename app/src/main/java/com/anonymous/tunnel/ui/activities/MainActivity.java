package com.anonymous.tunnel.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.anonymous.tunnel.R;
import com.anonymous.tunnel.model.ConfigProfile;
import com.anonymous.tunnel.service.VpnTunnelService;
import com.anonymous.tunnel.ui.views.AnonymousTextView;
import com.anonymous.tunnel.utils.ConfigManager;
import com.anonymous.tunnel.utils.ConnectionState;
import com.anonymous.tunnel.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int VPN_REQUEST_CODE = 1001;

    private CardView statusCard;
    private TextView tvStatus;
    private TextView tvServerInfo;
    private TextView tvConnectionTime;
    private TextView tvDataUsage;
    private MaterialButton btnConnect;
    private MaterialButton btnDisconnect;
    private LinearLayout layoutServerMessage;
    private AnonymousTextView tvServerMessage;
    private ImageView ivStatusIcon;
    private TextView tvProfileName;
    private FloatingActionButton fabAddConfig;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long connectionStartTime;
    private ConfigProfile currentProfile;
    private PreferenceManager prefs;

    private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("state");
            String message = intent.getStringExtra("message");
            updateUI(state, message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new PreferenceManager(this);
        initViews();
        setupListeners();
        loadCurrentProfile();

        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateConnectionTimer();
                timerHandler.postDelayed(this, 1000);
            }
        };

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(connectionReceiver, new IntentFilter("connection_state"));
    }

    private void initViews() {
        statusCard = findViewById(R.id.status_card);
        tvStatus = findViewById(R.id.tv_status);
        tvServerInfo = findViewById(R.id.tv_server_info);
        tvConnectionTime = findViewById(R.id.tv_connection_time);
        tvDataUsage = findViewById(R.id.tv_data_usage);
        btnConnect = findViewById(R.id.btn_connect);
        btnDisconnect = findViewById(R.id.btn_disconnect);
        layoutServerMessage = findViewById(R.id.layout_server_message);
        tvServerMessage = findViewById(R.id.tv_server_message);
        ivStatusIcon = findViewById(R.id.iv_status_icon);
        tvProfileName = findViewById(R.id.tv_profile_name);
        fabAddConfig = findViewById(R.id.fab_add_config);
    }

    private void setupListeners() {
        btnConnect.setOnClickListener(v -> startVpn());
        btnDisconnect.setOnClickListener(v -> stopVpn());
        fabAddConfig.setOnClickListener(v -> showConfigOptions());

        findViewById(R.id.card_server).setOnClickListener(v -> {
            Intent intent = new Intent(this, ServerListActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.card_logs).setOnClickListener(v -> {
            Intent intent = new Intent(this, LogsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.card_settings).setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void loadCurrentProfile() {
        currentProfile = ConfigManager.getInstance(this).getActiveProfile();
        if (currentProfile != null) {
            tvProfileName.setText(currentProfile.getName());
            tvServerInfo.setText(currentProfile.getServerAddress() + ":" + currentProfile.getServerPort());
        } else {
            tvProfileName.setText("No Profile Selected");
            tvServerInfo.setText("Tap + to add config");
        }
    }

    private void startVpn() {
        if (currentProfile == null) {
            Toast.makeText(this, "Please select a config first", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        } else {
            onVpnPermissionGranted();
        }
    }

    private void onVpnPermissionGranted() {
        Intent intent = new Intent(this, VpnTunnelService.class);
        intent.setAction("START");
        intent.putExtra("profile_id", currentProfile.getId());
        startService(intent);
    }

    private void stopVpn() {
        Intent intent = new Intent(this, VpnTunnelService.class);
        intent.setAction("STOP");
        startService(intent);
    }

    private void updateUI(String state, String message) {
        runOnUiThread(() -> {
            switch (state) {
                case "CONNECTING":
                    tvStatus.setText("Connecting...");
                    tvStatus.setTextColor(getColor(R.color.color_warning));
                    statusCard.setCardBackgroundColor(getColor(R.color.color_warning_bg));
                    ivStatusIcon.setImageResource(R.drawable.ic_connecting);
                    btnConnect.setVisibility(View.GONE);
                    btnDisconnect.setVisibility(View.VISIBLE);
                    layoutServerMessage.setVisibility(View.GONE);
                    break;

                case "CONNECTED":
                    tvStatus.setText("Connected");
                    tvStatus.setTextColor(getColor(R.color.color_success));
                    statusCard.setCardBackgroundColor(getColor(R.color.color_success_bg));
                    ivStatusIcon.setImageResource(R.drawable.ic_connected);
                    btnConnect.setVisibility(View.GONE);
                    btnDisconnect.setVisibility(View.VISIBLE);
                    layoutServerMessage.setVisibility(View.VISIBLE);
                    tvServerMessage.setAnonymousText("Anonymous");
                    connectionStartTime = System.currentTimeMillis();
                    timerHandler.post(timerRunnable);
                    break;

                case "DISCONNECTED":
                    tvStatus.setText("Disconnected");
                    tvStatus.setTextColor(getColor(R.color.color_error));
                    statusCard.setCardBackgroundColor(getColor(R.color.color_error_bg));
                    ivStatusIcon.setImageResource(R.drawable.ic_disconnected);
                    btnConnect.setVisibility(View.VISIBLE);
                    btnDisconnect.setVisibility(View.GONE);
                    layoutServerMessage.setVisibility(View.GONE);
                    timerHandler.removeCallbacks(timerRunnable);
                    tvConnectionTime.setText("00:00:00");
                    break;

                case "ERROR":
                    tvStatus.setText("Error: " + message);
                    tvStatus.setTextColor(getColor(R.color.color_error));
                    statusCard.setCardBackgroundColor(getColor(R.color.color_error_bg));
                    btnConnect.setVisibility(View.VISIBLE);
                    btnDisconnect.setVisibility(View.GONE);
                    layoutServerMessage.setVisibility(View.GONE);
                    break;
            }
        });
    }

    private void updateConnectionTimer() {
        long elapsed = System.currentTimeMillis() - connectionStartTime;
        int seconds = (int) (elapsed / 1000) % 60;
        int minutes = (int) ((elapsed / (1000 * 60)) % 60);
        int hours = (int) ((elapsed / (1000 * 60 * 60)) % 24);
        tvConnectionTime.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void showConfigOptions() {
        String[] options = {"New Config", "Import from Clipboard", "Import from File", "Scan QR Code"};
        new AlertDialog.Builder(this)
            .setTitle("Add Configuration")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        startActivity(new Intent(this, ConfigEditorActivity.class));
                        break;
                    case 1:
                        ConfigManager.getInstance(this).importFromClipboard();
                        break;
                    case 2:
                        ConfigManager.getInstance(this).importFromFile();
                        break;
                    case 3:
                        // QR scan
                        break;
                }
            })
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            onVpnPermissionGranted();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(connectionReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
