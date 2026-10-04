import io
import time
import uuid
from typing import Tuple, Dict, Any
from PIL import Image
from fastapi import UploadFile, HTTPException, status

from app.core.config import settings
from app.providers.base import ImageProcessorProvider
from app.providers.rembg_provider import RembgLocalProvider
from app.providers.upscale_provider import PillowLanczosUpscaleProvider
from app.providers.cloud_providers import ClipDropCloudProvider, UnconfiguredStubProvider
from app.schemas.status import SystemStatusResponse, ProviderStatus

class ImageService:
    def __init__(self):
        self._bg_provider: ImageProcessorProvider = self._resolve_bg_provider()
        self._upscale_provider: ImageProcessorProvider = self._resolve_upscale_provider()

    def _resolve_bg_provider(self) -> ImageProcessorProvider:
        prov_type = settings.BG_REMOVE_PROVIDER.lower().strip()
        if prov_type == "rembg":
            return RembgLocalProvider()
        elif prov_type == "clipdrop":
            return ClipDropCloudProvider()
        elif prov_type == "unconfigured":
            return UnconfiguredStubProvider("Provider background removal sengaja dimatikan untuk pengujian.")
        else:
            return RembgLocalProvider()

    def _resolve_upscale_provider(self) -> ImageProcessorProvider:
        prov_type = settings.UPSCALE_PROVIDER.lower().strip()
        if prov_type in ("pillow", "pillow_lanczos", "local"):
            return PillowLanczosUpscaleProvider()
        elif prov_type == "unconfigured":
            return UnconfiguredStubProvider("Provider upscaling sengaja dimatikan untuk pengujian.")
        else:
            return PillowLanczosUpscaleProvider()

    def get_bg_removal_provider(self) -> ImageProcessorProvider:
        return self._bg_provider

    def get_upscale_provider(self) -> ImageProcessorProvider:
        return self._upscale_provider

    def validate_image_file(self, file: UploadFile, contents: bytes):
        # 1. Validasi ukuran berkas
        max_bytes = settings.MAX_FILE_SIZE_MB * 1024 * 1024
        if len(contents) > max_bytes:
            raise HTTPException(
                status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                detail=f"Ukuran berkas ({len(contents) / (1024*1024):.1f} MB) melebihi batas maksimal {settings.MAX_FILE_SIZE_MB} MB."
            )

        # 2. Validasi tipe MIME
        content_type = file.content_type or ""
        if content_type.lower() not in settings.SUPPORTED_MIME_TYPES:
            # Toleransi jika ekstensi cocok
            filename = (file.filename or "").lower()
            valid_ext = any(filename.endswith(ext) for ext in [".jpg", ".jpeg", ".png", ".webp"])
            if not valid_ext:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Format berkas '{content_type}' tidak didukung. Format yang didukung: JPEG, PNG, WEBP."
                )

        # 3. Validasi integritas berkas gambar via PIL
        try:
            with Image.open(io.BytesIO(contents)) as img:
                img.verify()
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Berkas gambar rusak atau tidak valid: {str(e)}"
            )

    async def process_remove_background(self, file: UploadFile, request_id: str) -> Tuple[bytes, int, int, int]:
        provider = self.get_bg_removal_provider()
        is_ready, setup_guide = provider.is_configured()
        if not is_ready:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail={
                    "error": "PROVIDER_NOT_CONFIGURED",
                    "message": f"Penyedia AI ({provider.provider_name}) belum siap.",
                    "setup_guide": setup_guide,
                    "request_id": request_id
                }
            )

        contents = await file.read()
        self.validate_image_file(file, contents)

        start_time = time.time()
        try:
            output_bytes, width, height = provider.remove_background(contents)
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail={
                    "error": "PROCESSING_FAILED",
                    "message": f"Gagal menghapus latar belakang: {str(e)}",
                    "request_id": request_id
                }
            )
        elapsed_ms = int((time.time() - start_time) * 1000)
        return output_bytes, width, height, elapsed_ms

    async def process_upscale(self, file: UploadFile, scale: int, request_id: str) -> Tuple[bytes, int, int, int]:
        if scale not in (2, 4):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Faktor skala '{scale}' tidak valid. Pilihan yang didukung: 2 atau 4."
            )

        provider = self.get_upscale_provider()
        is_ready, setup_guide = provider.is_configured()
        if not is_ready:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail={
                    "error": "PROVIDER_NOT_CONFIGURED",
                    "message": f"Penyedia Upscaling ({provider.provider_name}) belum siap.",
                    "setup_guide": setup_guide,
                    "request_id": request_id
                }
            )

        contents = await file.read()
        self.validate_image_file(file, contents)

        start_time = time.time()
        try:
            output_bytes, width, height = provider.upscale(contents, scale)
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail={
                    "error": "PROCESSING_FAILED",
                    "message": f"Gagal meningkatkan resolusi: {str(e)}",
                    "request_id": request_id
                }
            )
        elapsed_ms = int((time.time() - start_time) * 1000)
        return output_bytes, width, height, elapsed_ms

    def get_system_status(self) -> SystemStatusResponse:
        bg_ready, bg_guide = self._bg_provider.is_configured()
        up_ready, up_guide = self._upscale_provider.is_configured()

        return SystemStatusResponse(
            app_name=settings.PROJECT_NAME,
            version=settings.VERSION,
            status="ready" if (bg_ready and up_ready) else "degraded",
            bg_removal=ProviderStatus(
                name=self._bg_provider.provider_name,
                is_ready=bg_ready,
                details="Aktif dan siap memproses." if bg_ready else "Belum dikonfigurasi.",
                setup_guide=bg_guide
            ),
            upscaling=ProviderStatus(
                name=self._upscale_provider.provider_name,
                is_ready=up_ready,
                details="Aktif dan siap memproses." if up_ready else "Belum dikonfigurasi.",
                setup_guide=up_guide
            ),
            max_file_size_mb=settings.MAX_FILE_SIZE_MB,
            supported_formats=settings.SUPPORTED_MIME_TYPES
        )

image_service = ImageService()
