# WengPixel Android Application

Aplikasi Android modern untuk menghapus latar belakang gambar secara instan menjadi PNG transparan, meningkatkan resolusi citra (2x dan 4x), membandingkan hasil sebelum & sesudah, mengganti latar dengan katalog warna, memotong rasio, serta menyimpan dan membagikan hasil karya.

---

## 1. Arsitektur & Prinsip Desain

Aplikasi dibangun mengikuti standar rekayasa perangkat lunak Android modern:
- **Clean Architecture:** Membagi lapisan menjadi **Presentation (Feature UI & ViewModel)**, **Domain (Use Cases & Repository Interfaces)**, dan **Data (Local Room, DataStore, File Manager & Remote Retrofit)**.
- **MVVM & Unidirectional Data Flow (UDF):** Aliran status satu arah (`UiState`) yang diamati melalui Kotlin `StateFlow` dan peristiwa aksi (`UiEvent` / `NavigationEvent`).
- **Repository Pattern:** Sumber data lokal dan jarak jauh dienkapsulasi rapi di balik abstraksi antarmuka domain.
- **Dependency Injection:** Dikelola penuh dengan Dagger Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`).
- **Single Activity:** `MainActivity.kt` sebagai gerbang navigasi deklaratif Jetpack Compose Navigation.

---

## 2. Pilihan Versi & Toolchain (`libs.versions.toml`)

| Komponen / Pustaka | Versi | Alasan Pemilihan |
| :--- | :--- | :--- |
| **Android Gradle Plugin (AGP)** | `8.7.3` | Versi stabil mutakhir yang didukung penuh oleh Android Studio dan Gradle 8.x/9.x. |
| **Kotlin** | `2.0.21` | Versi stabil Kotlin 2.0 dengan compiler K2 yang cepat dan handal. |
| **KSP** | `2.0.21-1.0.28` | Pengganti kapt berkinerja tinggi untuk pemrosesan anotasi Room dan Hilt. |
| **Compose BOM** | `2024.10.01` | Menjamin keselarasan versi seluruh pustaka Jetpack Compose & Material 3 tanpa konflik transitif. |
| **Dagger Hilt** | `2.52` | Standar industri untuk Dependency Injection pada arsitektur MVVM di Android. |
| **Room Database** | `2.6.1` | Pustaka stabil ORM SQLite resmi AndroidX dengan dukungan penuh Kotlin Coroutines Flow. |
| **DataStore Preferences** | `1.1.1` | Solusi penyimpanan pengaturan asinkron pengganti SharedPreferences yang aman terhadap UI-thread blocking. |
| **WorkManager** | `2.10.0` | Penjadwalan pekerjaan background andal (`CoroutineWorker`) untuk pembersihan berkas cache otomatis. |
| **Coil** | `2.7.0` | Image loader modern berbasis Kotlin Coroutines yang ringan dan terintegrasi mulus dengan Compose. |
| **Retrofit & OkHttp** | `2.11.0` / `4.12.0` | Klien HTTP tangguh dengan interceptor dinamis dan dukungan timeout unggah/unduh berkas besar. |
| **Kotlinx Serialization** | `1.7.3` | Parser JSON tipe-aman bawaan Kotlin tanpa beban refleksi berlebih. |
| **AndroidX ExifInterface** | `1.3.7` | Membaca dan menormalkan orientasi foto yang diambil dari kamera perangkat secara presisi. |

### Kebijakan SDK:
- **`minSdk = 24` (Android 7.0 Nougat):**
  - **Alasan:** Menjangkau lebih dari 95% perangkat Android aktif di seluruh dunia. Mendukung fitur Java 8/21 dan API Android modern. Fitur modern seperti **Android Photo Picker** (`PickVisualMedia`) tetap kompatibel ke belakang (*backported*) hingga API 19 melalui pembaruan sistem Google Play Services.
- **`targetSdk = 35` & `compileSdk = 35` (Android 15):**
  - **Alasan:** Memenuhi ketentuan publikasi Google Play Store terbaru dan mendukung penuh fitur keamanan Scoped Storage terkini.

---

## 3. Fitur Utama & Kepatuhan Fungsional

1. **Pemilih Gambar Tanpa Izin Berbahaya:**
   - Menggunakan kontrak `ActivityResultContracts.PickVisualMedia()` (Android Photo Picker). Pengguna tidak perlu memberikan izin `READ_EXTERNAL_STORAGE` yang invasif.
2. **Penghapusan Latar Belakang (True Alpha PNG):**
   - Menghasilkan berkas PNG dengan transparansi nyata. Area transparan dipratinjau dengan pola papan catur (*checkerboard pattern*) agar pengguna dapat memastikan transparansi secara visual.
3. **Peningkatan Resolusi Jujur (Upscaling 2x & 4x):**
   - Menampilkan perbandingan dimensi piksel sebenarnya sebelum dan sesudah pemrosesan (misal `800 × 600 px → 1600 × 1200 px`). Tidak ada janji palsu atau manipulasi hasil.
4. **Perbandingan Interaktif Sebelum & Sesudah:**
   - **Slider Geser:** Pengguna dapat menggeser garis pembatas secara horizontal untuk membandingkan foto asli dan hasil edit secara berdampingan dalam satu kanvas.
   - **Tombol Tahan:** Tombol *"Tahan untuk Melihat Asli"* yang menampilkan gambar asli saat ditekan dan kembali ke hasil saat dilepas.
5. **Katalog Warna Latar Belakang:**
   - Menyediakan pilihan transparan murni atau berbagai warna latar (Putih Studio, Hitam, Abu-abu Slate, Biru Langit, Pink Pastel, dll.) yang di-render di bawah subjek tanpa merusak kualitas gambar.
6. **Pemotongan & Pengubahan Ukuran:**
   - Menyediakan pilihan rasio aspek populer (Bebas, 1:1 Persegi, 4:3 Foto Standar, 16:9 Lanskap, 9:16 Story/Reels) serta pengubahan resolusi piksel manual.
7. **Penyimpanan Galeri Publik & Berbagi:**
   - Mengekspor ke galeri (`Pictures/WengPixel`) menggunakan **MediaStore Scoped Storage** dengan penanganan `IS_PENDING` (Android 10+). Format PNG dipertahankan untuk transparansi.
   - Berbagi langsung melalui Android Share Sheet menggunakan `FileProvider`.
8. **Riwayat Lokal Aman (Privasi Terjamin):**
   - Metadata disimpan di Room Database lokal (`id`, `processType`, `dimensions`, `fileSize`, `timestamp`).
   - Berkas fisik disimpan di penyimpanan privat aplikasi (`filesDir/history_images`), bukan di database.
   - Pengguna dapat membuka kembali gambar, membagikan, menghapus satu per satu (sekaligus menghapus berkas fisiknya), atau membersihkan seluruh riwayat.
9. **Penanganan Galat yang Jelas:**
   - Pesan ramah dalam Bahasa Indonesia untuk kondisi: offline tanpa internet, server belum aktif, atau penyedia AI belum diatur di backend.
   - Tautan langsung dari banner peringatan ke menu **Pengaturan** untuk menguji dan memperbarui URL server.

---

## 4. Cara Menjalankan Aplikasi di Android Studio

1. Buka folder `WengPixel/android` melalui **Android Studio**.
2. Biarkan Gradle melakukan sinkronisasi dependensi.
3. Pastikan server backend telah dijalankan di komputer (port `8000`).
4. Jalankan aplikasi pada Emulator atau Perangkat Fisik Android:
   - **Pada Android Emulator:** URL default `http://10.0.2.2:8000` langsung terhubung ke localhost mesin host.
   - **Pada Perangkat Fisik:** Buka menu **Pengaturan** di aplikasi WengPixel, masukkan alamat IP Wi-Fi lokal komputer Anda (misal `http://192.168.1.50:8000`), lalu ketuk **Uji Koneksi Server**.
5. Untuk kompilasi langsung via CLI:
   ```bash
   ./gradlew assembleDebug
   ```
   Berkas APK hasil build berada di:
   `app/build/outputs/apk/debug/app-debug.apk`
