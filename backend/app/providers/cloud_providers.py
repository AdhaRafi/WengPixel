from typing import Tuple, Optional
from app.providers.base import ImageProcessorProvider
from app.core.config import settings

class ClipDropCloudProvider(ImageProcessorProvider):
    """
    Provider cloud ClipDrop.
    Memerlukan CLIPDROP_API_KEY di file .env.
    """
    def __init__(self):
        self.api_key = settings.CLIPDROP_API_KEY

    @property
    def provider_name(self) -> str:
        return "ClipDrop API (Cloud)"

    def is_configured(self) -> Tuple[bool, Optional[str]]:
        if not self.api_key or self.api_key.strip() == "":
            return (
                False,
                "Layanan ClipDrop belum dikonfigurasi. "
                "Tambahkan CLIPDROP_API_KEY pada file .env backend untuk mengaktifkan provider ini."
            )
        return (True, None)

    def remove_background(self, image_bytes: bytes) -> Tuple[bytes, int, int]:
        ready, error = self.is_configured()
        if not ready:
            raise RuntimeError(error)
        # Placeholder untuk integrasi HTTP langsung ke https://clipdrop-api.co/remove-background/v1
        raise NotImplementedError("Integrasi jaringan ke ClipDrop aktif setelah API key diisi.")

    def upscale(self, image_bytes: bytes, scale: int) -> Tuple[bytes, int, int]:
        ready, error = self.is_configured()
        if not ready:
            raise RuntimeError(error)
        raise NotImplementedError("Integrasi jaringan ke ClipDrop aktif setelah API key diisi.")


class UnconfiguredStubProvider(ImageProcessorProvider):
    """
    Provider untuk menguji respons ketika layanan AI belum diatur,
    sesuai kriteria pengujian kegagalan dan penanganan status unconfigured.
    """
    def __init__(self, reason: str = "Layanan pemrosesan AI belum dikonfigurasi pada server."):
        self.reason = reason

    @property
    def provider_name(self) -> str:
        return "Unconfigured Provider"

    def is_configured(self) -> Tuple[bool, Optional[str]]:
        return (
            False,
            f"{self.reason} Atur penyedia AI (rembg / cloud API) di file .env backend."
        )

    def remove_background(self, image_bytes: bytes) -> Tuple[bytes, int, int]:
        ready, error = self.is_configured()
        raise RuntimeError(error)

    def upscale(self, image_bytes: bytes, scale: int) -> Tuple[bytes, int, int]:
        ready, error = self.is_configured()
        raise RuntimeError(error)
