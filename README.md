## 📱 Android App Setup

### Installation Steps

**Build APK**

**Via Android Studio:**
```
1. Open project in Android Studio
2. Build > Build Bundle(s) / APK(s) > Build APK(s)
3. Wait for build to complete
4. Click "locate" to find generated APK
```

**Via Command Line:**
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# APK location
# Debug: app/build/outputs/apk/debug/app-debug.apk
# Release: app/build/outputs/apk/release/app-release.apk
```

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

## ⚙️ Konfigurasi Aplikasi Android

### 1. AndroidManifest.xml
Pastikan permissions sudah ditambahkan:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

### 2. Network Configuration
Tambahkan network security config untuk HTTP:
```xml
<!-- AndroidManifest.xml -->
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ... >
```

**res/xml/network_security_config.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">[SERVER_IP]</domain>
    </domain-config>
</network-security-config>
```

### 3. API Configuration
Update BASE_URL di konfigurasi Retrofit:
```kotlin
object ApiConfig {
    // Use BuildConfig values from gradle
    private const val BASE_URL = "http://${BuildConfig.SERVER_IP}:${BuildConfig.SERVER_PORT}/"
    
    fun getApiService(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}
```

## 🧪 Testing & Debugging

### Backend Testing

**1. API Documentation**
```
URL: http://[SERVER_IP]:8000/docs
```

**2. Health Check**
```bash
curl -X GET "http://[SERVER_IP]:8000/health"
```

**3. Test Upload Audio**
```bash
curl -X POST "http://[SERVER_IP]:8000/transcribe" \
  -H "Content-Type: multipart/form-data" \
  -F "audio=@test_audio.wav"
```

### Android Testing

1. **Check VPN Connection**
   - Status bar harus menunjukkan icon VPN
   - Test akses ke server via browser

2. **Check App Permissions**
   - Settings > Apps > Jasper > Permissions
   - Pastikan Microphone dan Storage enabled

3. **Debug Logs**
   ```bash
   # Via ADB
   adb logcat | grep Jasper
   ```

## 🔍 Troubleshooting
