from pydantic_settings import BaseSettings
from typing import List, Optional
import os

class Settings(BaseSettings):
    PROJECT_NAME: str = "WengPixel AI Backend"
    VERSION: str = "1.0.0"
    API_V1_STR: str = "/v1"
    
    # Server settings
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    
    # File limits
    MAX_FILE_SIZE_MB: int = 20
    SUPPORTED_MIME_TYPES: List[str] = [
        "image/jpeg",
        "image/png",
        "image/webp"
    ]
    
    # AI Providers: "rembg" (local), "clipdrop" (cloud), "replicate" (cloud), "mock" (unconfigured)
    BG_REMOVE_PROVIDER: str = "rembg"
    UPSCALE_PROVIDER: str = "pillow_lanczos"
    
    # Optional Cloud API keys (NEVER commit real keys)
    CLIPDROP_API_KEY: Optional[str] = None
    REPLICATE_API_TOKEN: Optional[str] = None
    
    # CORS
    CORS_ORIGINS: List[str] = ["*"]
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = True

settings = Settings()
