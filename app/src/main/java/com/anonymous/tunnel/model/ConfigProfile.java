package com.anonymous.tunnel.model;

import java.util.UUID;

public class ConfigProfile {
    private String id;
    private String name;
    private ProtocolType protocolType;
    private String serverAddress;
    private int serverPort;
    private String username;
    private String password;
    private String uuid;
    private String path;
    private String host;
    private String sni;
    private String tlsFingerprint;
    private boolean wsEnabled;
    private boolean grpcEnabled;
    private boolean muxEnabled;
    private String payload;
    private String proxyAddress;
    private int proxyPort;
    private boolean active;
    private long createdAt;

    public ConfigProfile() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ProtocolType getProtocolType() { return protocolType; }
    public void setProtocolType(ProtocolType protocolType) { this.protocolType = protocolType; }

    public String getServerAddress() { return serverAddress; }
    public void setServerAddress(String serverAddress) { this.serverAddress = serverAddress; }

    public int getServerPort() { return serverPort; }
    public void setServerPort(int serverPort) { this.serverPort = serverPort; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public String getSni() { return sni; }
    public void setSni(String sni) { this.sni = sni; }

    public String getTlsFingerprint() { return tlsFingerprint; }
    public void setTlsFingerprint(String tlsFingerprint) { this.tlsFingerprint = tlsFingerprint; }

    public boolean isWsEnabled() { return wsEnabled; }
    public void setWsEnabled(boolean wsEnabled) { this.wsEnabled = wsEnabled; }

    public boolean isGrpcEnabled() { return grpcEnabled; }
    public void setGrpcEnabled(boolean grpcEnabled) { this.grpcEnabled = grpcEnabled; }

    public boolean isMuxEnabled() { return muxEnabled; }
    public void setMuxEnabled(boolean muxEnabled) { this.muxEnabled = muxEnabled; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getProxyAddress() { return proxyAddress; }
    public void setProxyAddress(String proxyAddress) { this.proxyAddress = proxyAddress; }

    public int getProxyPort() { return proxyPort; }
    public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
