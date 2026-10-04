# WengPixel AI Backend Service

Layanan microservice berbasis Python FastAPI untuk pemrosesan citra digital WengPixel: penghapusan latar belakang (*background removal*) dan peningkatan resolusi (*image upscaling*).

---

## 1. Arsitektur & Provider AI

Backend menggunakan pola **Provider Interface (`ImageProcessorProvider`)** yang modular. Engine AI dapat diganti atau ditambahkan (misal beralih dari model lokal ke Cloud API) tanpa mengubah kontrak API maupun antarmuka aplikasi Android.

### Provider yang Tersedia:

1. **`RembgLocalProvider` (Aktif / Default)**
   - **Status:** **Berfungsi Penuh (Teruji)**.
   - **Teknologi:** Model *Deep Learning* ONNX Runtime `u2net` dengan *alpha matting*.
   - **Fitur:** Menghasilkan berkas PNG murni dengan channel transparansi (alpha channel) yang sesungguhnya.
   - Model `u2net.onnx` disimpan secara otomatis di direktori cache lokal mesin (`~/.rembg/models/u2net/`).

2. **`PillowLanczosUpscaleProvider` (Aktif / Default)**
   - **Status:** **Berfungsi Penuh (Teruji)**.
   - **Teknologi:** Rekonstruksi sink Lanczos 8-tap berpresisi tinggi dikombinasikan dengan filter *Unsharp Masking*.
   - **Fitur:** Meningkatkan resolusi 2x dan 4x tanpa merusak transparansi alpha. Melaporkan dimensi keluaran secara jujur tanpa mengklaim palsu bahwa detail yang hilang dijamin kembali sempurna.

3. **`ClipDropCloudProvider` (Opsional / Cloud)**
   - **Status:** **Memerlukan Kredensial**.
   - Membutuhkan `CLIPDROP_API_KEY` pada berkas `.env`. Jika belum diisi, backend mengembalikan status HTTP 503 dengan panduan konfigurasi yang jelas.

4. **`UnconfiguredStubProvider` (Pengujian Kegagalan)**
   - Digunakan untuk mensimulasikan respons ketika penyedia AI belum diatur atau mengalami kendala konfigurasi.

---

## 2. Persyaratan Sistem & Instalasi

- **Python:** 3.10 - 3.14
- **Dependensi Utama:** FastAPI, Uvicorn, Pydantic, Pillow, Rembg, ONNX Runtime.

### Langkah Instalasi:

```bash
cd backend
pip install -r requirements.txt
```

---

## 3. Konfigurasi Environment (`.env`)

Salin template konfigurasi:
```bash
cp .env.example .env
```

Isi berkas `.env`:
```env
PROJECT_NAME="WengPixel AI Backend"
HOST="0.0.0.0"
PORT=8000
MAX_FILE_SIZE_MB=20

# Pilihan Provider:
# BG_REMOVE_PROVIDER="rembg" (lokal) atau "clipdrop" (cloud) atau "unconfigured"
BG_REMOVE_PROVIDER=rembg

# UPSCALE_PROVIDER="pillow_lanczos" (lokal) atau "unconfigured"
UPSCALE_PROVIDER=pillow_lanczos

# API Key Penyedia Cloud (Opsional)
# CLIPDROP_API_KEY=masukkan_key_anda_di_sini
```

---

## 4. Cara Menjalankan Server

Jalankan server menggunakan Uvicorn:

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Akses dokumentasi OpenAPI / Swagger interaktif:
- **Swagger UI:** [http://localhost:8000/docs](http://localhost:8000/docs)
- **Status Endpoint:** [http://localhost:8000/v1/status](http://localhost:8000/v1/status)
- **Health Check:** [http://localhost:8000/health](http://localhost:8000/health)

---

## 5. Kontrak Pemrosesan Gambar (API Contract)

### A. Hapus Latar Belakang
- **Endpoint:** `POST /v1/process/remove-background`
- **Tipe Konten:** `multipart/form-data`
- **Body:** `file` (Berkas gambar JPEG/PNG/WEBP, maks 20 MB).
- **Respons:** Berkas citra biner (`image/png`) dengan header:
  - `X-Request-ID`: UUID unik untuk penelusuran kegagalan.
  - `X-Image-Width`: Lebar piksel hasil.
  - `X-Image-Height`: Tinggi piksel hasil.
  - `X-Image-Format`: `PNG`
  - `X-Processing-Time-Ms`: Waktu pemrosesan dalam milidetik.

### B. Tingkatkan Resolusi (Upscale)
- **Endpoint:** `POST /v1/process/upscale`
- **Tipe Konten:** `multipart/form-data`
- **Body:**
  - `file`: Berkas gambar.
  - `scale`: Bilangan bulat `2` atau `4`.
- **Respons:** Berkas citra biner dengan header:
  - `X-Request-ID`: UUID
  - `X-Image-Width`: Lebar piksel hasil yang diperbesar.
  - `X-Image-Height`: Tinggi piksel hasil yang diperbesar.
  - `X-Scale-Factor`: `2` atau `4`
  - `X-Processing-Time-Ms`: Durasi pemrosesan.

### C. Status & Kesiapan Sistem
- **Endpoint:** `GET /v1/status`
- **Respons:**
  ```json
  {
    "app_name": "WengPixel AI Backend",
    "version": "1.0.0",
    "status": "ready",
    "bg_removal": {
      "name": "Rembg (Local ONNX u2net)",
      "is_ready": true,
      "details": "Aktif dan siap memproses."
    },
    "upscaling": {
      "name": "High-Quality Lanczos Upscaler",
      "is_ready": true,
      "details": "Aktif dan siap memproses."
    },
    "max_file_size_mb": 20,
    "supported_formats": ["image/jpeg", "image/png", "image/webp"]
  }
  ```

---

## 6. Menjalankan Pengujian Backend

Jalankan suite pengujian mandiri:

```bash
python test_backend.py
```
*(Seluruh uji health, status, upscale 2x, upscale 4x, validasi input tidak sah, serta penghapusan background telah diverifikasi 100% lulus).*
