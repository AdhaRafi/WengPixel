from abc import ABC, abstractmethod
from typing import Tuple, Optional

class ImageProcessorProvider(ABC):
    """
    Abstraksi penyedia AI untuk memisahkan integrasi vendor dari API utama.
    Memungkinkan penggantian engine (misal: Rembg -> ClipDrop -> Replicate)
    tanpa merombak struktur endpoint atau aplikasi Android.
    """

    @property
    @abstractmethod
    def provider_name(self) -> str:
        pass

    @abstractmethod
    def is_configured(self) -> Tuple[bool, Optional[str]]:
        """
        Mengembalikan (True, None) jika provider siap dipakai.
        Mengembalikan (False, "Panduan konfigurasi") jika belum siap/kredensial belum disetel.
        """
        pass

    @abstractmethod
    def remove_background(self, image_bytes: bytes) -> Tuple[bytes, int, int]:
        """
        Menghapus latar belakang gambar.
        Mengembalikan tuple: (png_bytes_dengan_transparansi, width, height)
        """
        pass

    @abstractmethod
    def upscale(self, image_bytes: bytes, scale: int) -> Tuple[bytes, int, int]:
        """
        Meningkatkan resolusi gambar berdasarkan faktor skala (2 atau 4).
        Mengembalikan tuple: (output_bytes, width, height)
        """
        pass
