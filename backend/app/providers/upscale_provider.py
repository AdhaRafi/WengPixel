import io
from typing import Tuple, Optional
from PIL import Image, ImageFilter
from app.providers.base import ImageProcessorProvider

class PillowLanczosUpscaleProvider(ImageProcessorProvider):
    """
    Provider resolusi tinggi lokal menggunakan algoritma Lanczos antialiasing
    dengan unsharp mask filter untuk mempertahankan ketajaman kontur gambar.
    Mendukung transparansi (RGBA) dan format RGB biasa secara presisi.
    """

    @property
    def provider_name(self) -> str:
        return "High-Quality Lanczos Upscaler"

    def is_configured(self) -> Tuple[bool, Optional[str]]:
        return (True, None)

    def remove_background(self, image_bytes: bytes) -> Tuple[bytes, int, int]:
        raise NotImplementedError("PillowLanczosUpscaleProvider dikhususkan untuk peningkatan resolusi.")

    def upscale(self, image_bytes: bytes, scale: int) -> Tuple[bytes, int, int]:
        if scale not in (2, 4):
            raise ValueError(f"Faktor skala '{scale}' tidak didukung. Pilihan valid: 2 atau 4.")

        input_image = Image.open(io.BytesIO(image_bytes))
        orig_width, orig_height = input_image.size
        
        target_width = orig_width * scale
        target_height = orig_height * scale

        # Mempertahankan mode transparansi jika gambar asli memiliki alpha channel
        has_alpha = input_image.mode in ("RGBA", "LA") or (input_image.mode == "P" and "transparency" in input_image.info)
        work_image = input_image.convert("RGBA" if has_alpha else "RGB")

        # Upscale dengan Resampling.LANCZOS (filter rekonstruksi sink 8-tap berpresisi tinggi)
        upscaled = work_image.resize((target_width, target_height), resample=Image.Resampling.LANCZOS)

        # Terapkan filter penajaman kontur terkontrol
        upscaled = upscaled.filter(ImageFilter.UnsharpMask(radius=1.5, percent=120, threshold=3))

        output_buffer = io.BytesIO()
        out_format = "PNG" if has_alpha else "JPEG"
        if out_format == "PNG":
            upscaled.save(output_buffer, format="PNG", optimize=True)
        else:
            upscaled.save(output_buffer, format="JPEG", quality=95, optimize=True)

        return output_buffer.getvalue(), target_width, target_height
