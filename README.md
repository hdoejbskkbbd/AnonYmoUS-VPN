# AnonymousTunnel

Advanced multi-protocol VPN tunneling app for Android.

## Features

### Protocols
- SSH (with payload injection)
- VLess (WebSocket, gRPC, TCP)
- VMess (V2Ray)
- Trojan (TLS)
- Shadowsocks
- DNSTT (SlowDNS)

### Security
- Kill Switch
- Split Tunneling (App-based)
- Stealth Mode (Hide app icon)
- Auto-Reconnect with retry logic
- Custom DNS (1.1.1.1, 1.0.0.1)

### Advanced
- WebSocket/gRPC transports
- SNI Spoofing
- Payload Injection
- Performance Mode
- Traffic Compression
- QR Code Import/Export
- Subscription Link Support

## Server Message
Every connection displays **Anonymous** in colored text:
- A = Orange
- n = Green
- o = Blue
- Y = Red
- m = Green
- o = Blue
- u = Cyan
- S = Cyan

## Download
Download latest APK from [Releases](../../releases)

## Build
```bash
./gradlew assembleRelease
```

## License
AnonymousTunnel 2026
