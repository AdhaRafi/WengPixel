from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from app.core.config import settings
from app.api.v1.routes import process, status

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description="Backend microservice pemrosesan AI gambar WengPixel: Background Removal & Upscaling."
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
    expose_headers=[
        "X-Request-ID",
        "X-Image-Width",
        "X-Image-Height",
        "X-Image-Format",
        "X-Scale-Factor",
        "X-Processing-Time-Ms",
        "Content-Disposition"
    ]
)

# Register routes
app.include_router(status.router, prefix=settings.API_V1_STR)
app.include_router(process.router, prefix=settings.API_V1_STR)

# Top level health check
@app.get("/health")
def root_health():
    return {"status": "ok", "app": settings.PROJECT_NAME, "version": settings.VERSION}

@app.get("/")
def root():
    return {
        "message": "Selamat datang di WengPixel AI Service API",
        "docs_url": "/docs",
        "status_url": f"{settings.API_V1_STR}/status"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host=settings.HOST, port=settings.PORT, reload=True)
