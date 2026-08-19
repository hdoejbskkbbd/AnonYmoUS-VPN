package com.anonymous.tunnel.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class PreferenceManager {

    private static final String PREFS_NAME = "anonymous_prefs";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSplitTunnelingEnabled() {
        return prefs.getBoolean("split_tunneling", false);
    }

    public void setSplitTunnelingEnabled(boolean enabled) {
        prefs.edit().putBoolean("split_tunneling", enabled).apply();
    }

    public Set<String> getBypassApps() {
        return prefs.getStringSet("bypass_apps", new HashSet<>());
    }

    public void setBypassApps(Set<String> apps) {
        prefs.edit().putStringSet("bypass_apps", apps).apply();
    }

    public boolean isKillSwitchEnabled() {
        return prefs.getBoolean("kill_switch", true);
    }

    public void setKillSwitchEnabled(boolean enabled) {
        prefs.edit().putBoolean("kill_switch", enabled).apply();
    }

    public boolean isAutoReconnectEnabled() {
        return prefs.getBoolean("auto_reconnect", true);
    }

    public int getMtu() {
        return prefs.getInt("mtu", 1500);
    }

    public void setMtu(int mtu) {
        prefs.edit().putInt("mtu", mtu).apply();
    }

    public boolean isStealthModeEnabled() {
        return prefs.getBoolean("stealth_mode", false);
    }

    public String getDnsPrimary() {
        return prefs.getString("dns_primary", "1.1.1.1");
    }

    public String getDnsSecondary() {
        return prefs.getString("dns_secondary", "1.0.0.1");
    }

    public boolean isDarkTheme() {
        return prefs.getBoolean("dark_theme", true);
    }

    public boolean isPerformanceMode() {
        return prefs.getBoolean("performance_mode", false);
    }

    public boolean isCompressionEnabled() {
        return prefs.getBoolean("compression", true);
    }
}
