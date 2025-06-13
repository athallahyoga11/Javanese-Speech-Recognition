## 📱 Android App Setup

### Installation Steps

1. **Transfer APK**
   - Via USB: Copy `app-debug.apk` ke folder Download
   - Via Bluetooth: Share file ke perangkat target
   - Via Cloud: Upload ke Google Drive/OneDrive

2. **Install APK**
   ```
   1. Buka File Manager di Android
   2. Navigate ke lokasi app-debug.apk
   3. Tap file APK
   4. Izinkan "Install from Unknown Sources" jika diminta
   5. Tap "Install"
   6. Tap "Open" setelah instalasi selesai
   ```

3. **Permissions Required**
   - Microphone access (untuk recording)
   - Internet access (untuk API calls)
   - Storage access (untuk temporary files)

## 🌐 Konfigurasi VPN ITS

### Setup OpenVPN

1. **Download OpenVPN Connect**
   - [Google Play Store](https://play.google.com/store/apps/details?id=net.openvpn.openvpn)
   - Atau search "OpenVPN Connect" di Play Store

2. **Download Konfigurasi VPN ITS**
   ```
   1. Buka browser, akses: https://vpn.its.ac.id
   2. Login dengan akun SSO ITS
   3. Download file konfigurasi (.ovpn)
   4. Save ke folder Download
   ```

3. **Import Konfigurasi**
   ```
   1. Buka aplikasi OpenVPN Connect
   2. Tap "+" atau "Import Profile"
   3. Pilih "File" tab
   4. Browse dan pilih file .ovpn yang sudah didownload
   5. Tap "Import"
   ```

4. **Connect ke VPN**
   ```
   1. Tap profile VPN ITS yang sudah diimport
   2. Masukkan username dan password ITS
   3. Tap "Connect"
   4. Tunggu hingga status "Connected"
   ```
