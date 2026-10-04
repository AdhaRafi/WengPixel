from pydantic import BaseModel
from typing import Optional, List

class ProviderStatus(BaseModel):
    name: str
    is_ready: bool
    details: str
    setup_guide: Optional[str] = None

class SystemStatusResponse(BaseModel):
    app_name: str
    version: str
    status: str
    bg_removal: ProviderStatus
    upscaling: ProviderStatus
    max_file_size_mb: int
    supported_formats: List[str]

class ErrorDetailResponse(BaseModel):
    error: str
    message: str
    request_id: Optional[str] = None
    setup_guide: Optional[str] = None
