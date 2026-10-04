from fastapi import APIRouter
from app.services.image_service import image_service
from app.schemas.status import SystemStatusResponse

router = APIRouter(tags=["Status & Health"])

@router.get("/status", response_model=SystemStatusResponse)
async def get_system_status():
    """
    Mengembalikan status provider AI, kesiapan model, serta instruksi setup.
    """
    return image_service.get_system_status()

@router.get("/health")
async def health_check():
    return {"status": "ok", "service": "WengPixel Backend"}
