# WengPixel — Background Removal & AI Image Upscaler

**WengPixel** adalah aplikasi Android berbasis Jetpack Compose dan backend FastAPI untuk menghapus latar belakang foto menjadi PNG transparan serta meningkatkan resolusi citra secara presisi dan aman.

---

## 🌟 Fitur Utama (MVP)

1. **Pemilih Gambar Modern:** Memilih gambar langsung dari galeri menggunakan Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) tanpa meminta izin penyimpanan berlebihan.
2. **Hapus Latar Belakang AI:** Menghasilkan PNG transparan sejati dengan model `u2net` (ONNX Runtime). Area transparan ditandai dengan latar kotak-kotak (*checkerboard pattern*).
3. **Peningkatan Resolusi (2x & 4x):** Opsi perbesaran resolusi dengan pelaporan dimensi piksel sebenarnya tanpa manipulasi atau klaim palsu 4K.
4. **Perbandingan Sebelum & Sesudah Interaktif:** 
   - Slider geser horizontal untuk membagi gambar sebelum dan sesudah secara real-time.
   - Tombol tekan-tahan (*press & hold*) untuk melihat gambar asli dengan cepat.
5. **Katalog Warna Latar Pengganti:** Mengganti latar belakang transparan dengan berbagai pilihan warna preset (Putih Studio, Hitam, Abu-abu, Biru, Merah, Pastel, dsb.).
6. **Pemotongan & Pengubahan Ukuran:** Pemotongan rasio aspek (1:1 Persegi, 4:3, 16:9, 9:16 Story/TikTok) dan pengubahan dimensi piksel manual.
7. **Ekspor Galeri & Berbagi:** Menyimpan ke `Pictures/WengPixel` dengan Android MediaStore Scoped Storage dan membagikan gambar melalui FileProvider.
8. **Riwayat Lokal Aman:** Riwayat disimpan di Room Database lokal perangkat. Berkas gambar disimpan di folder privat aplikasi dan dapat dihapus kapan saja bersama berkas fisiknya.
9. **Penanganan Status & Galat Transparan:** Informasi jelas dalam Bahasa Indonesia saat offline, saat server tidak terhubung, atau saat provider AI belum dikonfigurasi.

---

## 🏗️ Struktur Proyek

```text
WengPixel/
├── android/                               # Aplikasi Android (Kotlin, Jetpack Compose)
│   ├── app/
│   │   ├── build.gradle.kts
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/wengpixel/
│   │       │   ├── app/                   # App class, MainActivity, Navigation
│   │       │   ├── core/                  # Utilities, Models, Design System, UI Components
│   │       │   ├── data/                  # Room, DataStore, File Manager, Retrofit, Repositories
│   │       │   ├── domain/                # Use Cases & Repository Interfaces
│   │       │   ├── feature/               # Home, Editor, History, Settings (UI + ViewModel)
│   │       │   ├── di/                    # Dagger Hilt Modules
│   │       │   └── worker/                # WorkManager ImageCleanupWorker
│   │       └── res/                       # Values, Drawables, Mipmap, XML File Provider
│   ├── gradle/
│   │   └── libs.versions.toml             # Gradle Version Catalog
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── README.md
│
├── backend/                               # Layanan Pemrosesan AI (Python FastAPI)
│   ├── app/
│   │   ├── api/v1/routes/                 # Endpoint proses (remove-background, upscale) & status
│   │   ├── core/config.py                 # Pydantic Settings & konfigurasi .env
│   │   ├── providers/                     # Rembg, Upscaler, Cloud Stubs (ClipDrop), Unconfigured
│   │   ├── schemas/                       # Skema validasi & DTO
│   │   ├── services/                      # Validasi gambar, metrik waktu, request ID
│   │   └── main.py                        # Entrypoint FastAPI
│   ├── test_backend.py                    # Suite pengujian otomatis (100% lulus)
│   ├── requirements.txt
│   ├── .env.example
│   └── README.md
│
└── README.md
```

---

## 🚀 Panduan Memulai Cepat

### 1. Menjalankan Backend (Python FastAPI)

```bash
cd backend
pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

- Buka [http://localhost:8000/docs](http://localhost:8000/docs) untuk menguji endpoint secara langsung melalui antarmuka Swagger UI.
- Jalankan verifikasi otomatis dengan `python test_backend.py`.

### 2. Menjalankan Aplikasi Android (Kotlin)

1. Buka folder `android/` di Android Studio.
2. Jalankan pada emulator atau perangkat fisik Android:
   - **Emulator:** Menggunakan URL default `http://10.0.2.2:8000`.
   - **Perangkat Fisik:** Buka tab **Pengaturan** di aplikasi, isi IP komputer Anda (misal: `http://192.168.1.10:8000`), dan tekan **Uji Koneksi Server**.
3. Bangun APK debug melalui terminal:
   ```bash
   cd android
   ./gradlew assembleDebug
   ```
   APK akan tersedia di: `android/app/build/outputs/apk/debug/app-debug.apk`.

---

## 🔒 Kebijakan Privasi & Keamanan Data Pengguna

- **Tidak Ada Overwrite:** Gambar asli milik pengguna tidak akan pernah diubah atau ditimpa. Hasil edit disimpan sebagai berkas baru.
- **Penyimpanan Lokal:** Riwayat gambar disimpan hanya di ruang privat aplikasi (`filesDir`), tidak pernah diunggah ke server tanpa sepengetahuan pengguna.
- **Server Nir-Simpan:** Server backend memproses gambar secara streaming di memori tanpa menyimpan arsip foto pengguna secara permanen.
- **Transparansi Jaringan:** Pengguna selalu diberi tahu ketika berkas harus dikirim ke backend untuk pemrosesan AI.
- **Kontrol Penuh:** Pengguna dapat menghapus riwayat satu per satu atau membersihkan seluruh riwayat sekaligus menghapus berkas fisiknya dari penyimpanan perangkat.
