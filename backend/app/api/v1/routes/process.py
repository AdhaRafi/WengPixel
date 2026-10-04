import uuid
from fastapi import APIRouter, UploadFile, File, Form, Response, Request
from app.services.image_service import image_service

router = APIRouter(prefix="/process", tags=["Processing"])

@router.post("/remove-background")
async def remove_background(
    request: Request,
    file: UploadFile = File(..., description="Berkas gambar (PNG, JPEG, WEBP)")
):
    request_id = str(uuid.uuid4())
    output_bytes, width, height, elapsed_ms = await image_service.process_remove_background(file, request_id)

    headers = {
        "X-Request-ID": request_id,
        "X-Image-Width": str(width),
        "X-Image-Height": str(height),
        "X-Image-Format": "PNG",
        "X-Processing-Time-Ms": str(elapsed_ms),
        "Content-Disposition": f'inline; filename="wengpixel_nobg_{request_id[:8]}.png"'
    }

    return Response(
        content=output_bytes,
        media_type="image/png",
        headers=headers
    )


@router.post("/upscale")
async def upscale(
    request: Request,
    file: UploadFile = File(..., description="Berkas gambar (PNG, JPEG, WEBP)"),
    scale: int = Form(2, description="Faktor skala resolusi: 2 atau 4")
):
    request_id = str(uuid.uuid4())
    output_bytes, width, height, elapsed_ms = await image_service.process_upscale(file, scale, request_id)

    headers = {
        "X-Request-ID": request_id,
        "X-Image-Width": str(width),
        "X-Image-Height": str(height),
        "X-Image-Format": "PNG",
        "X-Scale-Factor": str(scale),
        "X-Processing-Time-Ms": str(elapsed_ms),
        "Content-Disposition": f'inline; filename="wengpixel_upscale_{scale}x_{request_id[:8]}.png"'
    }

    return Response(
        content=output_bytes,
        media_type="image/png",
        headers=headers
    )
