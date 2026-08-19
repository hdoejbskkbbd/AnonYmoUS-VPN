package com.anonymous.tunnel.model;

public enum ProtocolType {
    SSH("SSH"),
    VLESS("VLess"),
    VMESS("VMess"),
    TROJAN("Trojan"),
    SHADOWSOCKS("Shadowsocks"),
    DNSTT("DNSTT");

    private final String displayName;

    ProtocolType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
