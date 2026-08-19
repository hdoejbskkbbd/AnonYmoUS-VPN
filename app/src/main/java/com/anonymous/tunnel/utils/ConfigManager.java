package com.anonymous.tunnel.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.anonymous.tunnel.model.ConfigProfile;
import com.anonymous.tunnel.model.ProtocolType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfigManager {

    private static final String PREFS_NAME = "anonymous_configs";
    private static final String KEY_PROFILES = "profiles";
    private static final String KEY_ACTIVE = "active_profile";

    private static ConfigManager instance;
    private final SharedPreferences prefs;
    private final Gson gson;

    private ConfigManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized ConfigManager getInstance(Context context) {
        if (instance == null) {
            instance = new ConfigManager(context);
        }
        return instance;
    }

    public void saveProfile(ConfigProfile profile) {
        List<ConfigProfile> profiles = getAllProfiles();
        // Remove existing if same ID
        profiles.removeIf(p -> p.getId().equals(profile.getId()));
        profiles.add(profile);
        saveProfiles(profiles);
    }

    public void deleteProfile(String id) {
        List<ConfigProfile> profiles = getAllProfiles();
        profiles.removeIf(p -> p.getId().equals(id));
        saveProfiles(profiles);
    }

    public List<ConfigProfile> getAllProfiles() {
        String json = prefs.getString(KEY_PROFILES, "[]");
        Type type = new TypeToken<List<ConfigProfile>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public ConfigProfile getProfileById(String id) {
        for (ConfigProfile p : getAllProfiles()) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    public ConfigProfile getActiveProfile() {
        String activeId = prefs.getString(KEY_ACTIVE, null);
        if (activeId != null) {
            return getProfileById(activeId);
        }
        List<ConfigProfile> profiles = getAllProfiles();
        return profiles.isEmpty() ? null : profiles.get(0);
    }

    public void setActiveProfile(String id) {
        prefs.edit().putString(KEY_ACTIVE, id).apply();
    }

    public void importFromClipboard() {
        // Implementation for clipboard import
    }

    public void importFromFile() {
        // Implementation for file import
    }

    private void saveProfiles(List<ConfigProfile> profiles) {
        prefs.edit().putString(KEY_PROFILES, gson.toJson(profiles)).apply();
    }

    // Demo configs for testing
    public void addDemoConfigs() {
        List<ConfigProfile> profiles = new ArrayList<>();

        ConfigProfile ssh = new ConfigProfile();
        ssh.setName("SSH Demo");
        ssh.setProtocolType(ProtocolType.SSH);
        ssh.setServerAddress("demo.ssh.server.com");
        ssh.setServerPort(22);
        ssh.setUsername("demo");
        ssh.setPassword("demo");
        profiles.add(ssh);

        ConfigProfile vless = new ConfigProfile();
        vless.setName("VLess WebSocket");
        vless.setProtocolType(ProtocolType.VLESS);
        vless.setServerAddress("newstatic.payu.in");
        vless.setServerPort(80);
        vless.setUuid(UUID.randomUUID().toString());
        vless.setPath("/vless");
        vless.setHost("cloudfront.net");
        vless.setWsEnabled(true);
        profiles.add(vless);

        ConfigProfile trojan = new ConfigProfile();
        trojan.setName("Trojan TLS");
        trojan.setProtocolType(ProtocolType.TROJAN);
        trojan.setServerAddress("trojan.example.com");
        trojan.setServerPort(443);
        trojan.setPassword("trojan-pass");
        profiles.add(trojan);

        saveProfiles(profiles);
    }
}
