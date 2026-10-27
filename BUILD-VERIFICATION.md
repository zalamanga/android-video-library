# Hasil pemeriksaan

Tanggal: 27 Oktober 2026. Toolchain: JDK 17, Gradle 8.10.2, AGP 8.8.2, Android SDK 35 dan Build Tools 35.0.0.

| Pemeriksaan | Hasil |
| --- | --- |
| `assembleDebug` | Berhasil, 2 APK debug terbentuk |
| `testDebugUnitTest` | 2 test, 0 gagal |
| `lintDebug` | Berhasil, 0 error dan 71 warning |
| Emulator/perangkat dan screenshot | Belum dijalankan |

Perintah pemeriksaan: `assembleDebug testDebugUnitTest lintDebug`. Laporan lengkap dapat dibuat ulang di direktori `build/reports` pada setiap modul. APK dan cache diabaikan Git.

SDK dan cache pemeriksaan berada di drive D. Mesin pemeriksa memakai init script lokal untuk alamat resmi alternatif Maven Central, `https://repo1.maven.org/maven2/`, karena sertifikat endpoint default ditolak mesin tersebut. Verifikasi TLS tetap aktif; script khusus mesin tidak ikut repo.

Hasil ini mencakup kompilasi, unit test yang tersedia, dan analisis statis. Tampilan serta perilaku pada perangkat belum diuji.

Build memakai kondisi tanpa `google-services.json`. Test contoh bawaan tidak membuktikan login, aturan Firebase, CRUD cloud, upload, atau playback. Layanan Firebase dan media nyata belum dihubungkan dalam pemeriksaan ini.
