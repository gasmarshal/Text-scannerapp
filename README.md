# Text Scanner App

Aplikasi ini memungkinkan pengguna untuk menangkap gambar dari kamera, mendeteksi teks di dalam gambar menggunakan ML Kit dari Google, dan menyimpan hasilnya ke dalam galeri dengan teks yang di-overlay pada gambar.

## Fitur Utama

- **Tangkapan Kamera**: Ambil gambar langsung dari kamera.
- **Deteksi Teks**: Mengenali teks dalam gambar menggunakan Google ML Kit Text Recognition.
- **Overlay Teks**: Teks yang terdeteksi akan ditambahkan langsung pada gambar yang diambil.
- **Simpan ke Galeri**: Gambar dengan teks dapat disimpan langsung ke galeri perangkat.
- **Responsif**: Teks di-overlay secara otomatis tanpa terpotong, dengan warna putih untuk visibilitas maksimal.

## Teknologi yang Digunakan

- **Android Jetpack Compose**: Untuk membangun antarmuka pengguna modern dan deklaratif.
- **Google ML Kit**: Untuk pengenalan teks.
- **CameraX API**: Untuk menangkap gambar langsung dari kamera.
- **Kotlin**: Sebagai bahasa pemrograman utama.

## Cara Instalasi

```bash
# Clone atau Unduh Repository
$ git clone https://github.com/gasmarshal/Text-scannerapp.git
$ cd text-scanner-app

# Buka di Android Studio
# 1. Buka Android Studio.
# 2. Pilih File > Open dan arahkan ke folder proyek.

# Setel Dependensi
# Pastikan Anda memiliki koneksi internet aktif untuk mengunduh dependensi proyek.
# Sync Gradle dengan menjalankan Sync Now di Android Studio.

# Jalankan Aplikasi
# 1. Sambungkan perangkat Android atau emulator.
# 2. Klik tombol Run di Android Studio.
```

## Cara Penggunaan

1. **Izinkan Akses Kamera**:
    - Saat aplikasi pertama kali dijalankan, izinkan akses ke kamera perangkat.

2. **Tangkap Gambar**:
    - Klik tombol `Capture Text` untuk mengambil gambar dari kamera.

3. **Deteksi Teks**:
    - Aplikasi secara otomatis akan mendeteksi teks dalam gambar yang diambil.

4. **Hasil dengan Overlay**:
    - Teks yang terdeteksi akan ditambahkan langsung ke gambar sebagai overlay.

5. **Simpan ke Galeri**:
    - Gambar dengan overlay teks akan secara otomatis disimpan di galeri perangkat dalam folder `TextScans`.

## Struktur Proyek

```bash
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/newscanfixed/
│   │   │   ├── ui/camera/CameraScreen.kt       # Fungsi utama untuk menangkap gambar dan deteksi teks
│   │   │   ├── ui/theme/Color.kt              # Palet warna untuk antarmuka aplikasi
│   │   │   ├── ui/theme/Theme.kt              # Tema Jetpack Compose
│   │   ├── res/
│   │   │   ├── layout/                        # File XML layout (jika diperlukan)
│   │   │   ├── values/strings.xml             # String yang digunakan dalam aplikasi
│   │   │   ├── drawable/                      # Ikon dan gambar
├── build.gradle                               # Konfigurasi Gradle untuk modul aplikasi
├── settings.gradle                            # Pengaturan global untuk proyek
```

## Dependensi

Pastikan file `build.gradle` memiliki dependensi berikut:

```bash
// Google ML Kit Text Recognition
implementation "com.google.mlkit:text-recognition:16.0.0"

// CameraX API
implementation "androidx.camera:camera-core:1.2.3"
implementation "androidx.camera:camera-view:1.2.3"
implementation "androidx.camera:camera-lifecycle:1.2.3"

// Jetpack Compose
implementation "androidx.compose.ui:ui:1.5.0"
implementation "androidx.compose.material:material:1.5.0"
implementation "androidx.compose.ui:ui-tooling-preview:1.5.0"
```

## Palet Warna

Warna-warna berikut digunakan untuk mendukung tampilan aplikasi yang konsisten:

```bash
// File: Color.kt
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Warna putih untuk teks overlay
val White = Color(0xFFFFFFFF)
```

## Kredit

Proyek ini terinspirasi dan dimodifikasi dari kode berikut:
- [Google ML Kit Vision Quickstart](https://github.com/googlesamples/mlkit/tree/master/android/vision-quickstart)
- [Jetpack Compose ML Kit Tutorial](https://github.com/YanneckReiss/JetpackComposeMLKitTutorial) oleh Yanneck Reiss

Terima kasih kepada para pembuat repositori ini atas kontribusinya kepada komunitas open-source.

