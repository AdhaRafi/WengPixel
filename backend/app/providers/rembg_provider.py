import io
from typing import Tuple, Optional
from PIL import Image
from app.providers.base import ImageProcessorProvider

class RembgLocalProvider(ImageProcessorProvider):
    """
    Provider lokal menggunakan model u2net berbasis ONNX runtime melalui Rembg.
    Menghasilkan PNG dengan alpha channel transparansi nyata.
    """
    def __init__(self):
        self._session = None
        self._init_error = None
        try:
            import rembg
            # Inisialisasi sesi u2net
            self._rembg = rembg
            self._session = rembg.new_session("u2net")
        except Exception as e:
            self._init_error = str(e)

    @property
    def provider_name(self) -> str:
        return "Rembg (Local ONNX u2net)"

    def is_configured(self) -> Tuple[bool, Optional[str]]:
        if self._init_error:
            return (
                False,
                f"Modul rembg/u2net gagal diinisialisasi: {self._init_error}. "
                "Pastikan dependensi terpasang (`pip install rembg`) dan koneksi internet aktif untuk unduhan awal model."
            )
        return (True, None)

    def remove_background(self, image_bytes: bytes) -> Tuple[bytes, int, int]:
        ready, error_msg = self.is_configured()
        if not ready:
            raise RuntimeError(error_msg)

        input_image = Image.open(io.BytesIO(image_bytes))
        input_image = input_image.convert("RGBA")

        # Proses penghapusan latar belakang instan berbasis neural network u2net
        output_image = self._rembg.remove(
            input_image,
            session=self._session,
            alpha_matting=False,
            post_process_mask=True
        )

        width, height = output_image.size

        # Simpan ke format PNG RGBA untuk mempertahankan transparansi sejati
        output_buffer = io.BytesIO()
        output_image.save(output_buffer, format="PNG")
        return output_buffer.getvalue(), width, height

    def upscale(self, image_bytes: bytes, scale: int) -> Tuple[bytes, int, int]:
        raise NotImplementedError("Rembg dikhususkan untuk penghapusan latar belakang.")
