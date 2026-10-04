import io
from PIL import Image
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def create_sample_image(width=100, height=100, color=(255, 0, 0)):
    buf = io.BytesIO()
    img = Image.new("RGB", (width, height), color=color)
    img.save(buf, format="PNG")
    buf.seek(0)
    return buf

def test_health():
    res = client.get("/health")
    assert res.status_code == 200
    assert res.json()["status"] == "ok"

def test_status_endpoint():
    res = client.get("/v1/status")
    assert res.status_code == 200
    data = res.json()
    assert "bg_removal" in data
    assert "upscaling" in data
    assert data["bg_removal"]["is_ready"] is True
    assert data["upscaling"]["is_ready"] is True

def test_upscale_2x():
    img_buf = create_sample_image(80, 60)
    files = {"file": ("test.png", img_buf, "image/png")}
    data = {"scale": 2}
    res = client.post("/v1/process/upscale", files=files, data=data)
    assert res.status_code == 200
    assert res.headers["x-scale-factor"] == "2"
    assert res.headers["x-image-width"] == "160"
    assert res.headers["x-image-height"] == "120"
    assert "x-request-id" in res.headers
    # Verify received image
    out_img = Image.open(io.BytesIO(res.content))
    assert out_img.size == (160, 120)

def test_upscale_4x():
    img_buf = create_sample_image(50, 50)
    files = {"file": ("test.png", img_buf, "image/png")}
    data = {"scale": 4}
    res = client.post("/v1/process/upscale", files=files, data=data)
    assert res.status_code == 200
    assert res.headers["x-scale-factor"] == "4"
    assert res.headers["x-image-width"] == "200"
    assert res.headers["x-image-height"] == "200"
    out_img = Image.open(io.BytesIO(res.content))
    assert out_img.size == (200, 200)

def test_upscale_invalid_scale():
    img_buf = create_sample_image(50, 50)
    files = {"file": ("test.png", img_buf, "image/png")}
    data = {"scale": 3}
    res = client.post("/v1/process/upscale", files=files, data=data)
    assert res.status_code == 400

def test_remove_background():
    img_buf = create_sample_image(64, 64)
    files = {"file": ("test.png", img_buf, "image/png")}
    res = client.post("/v1/process/remove-background", files=files)
    assert res.status_code == 200
    assert res.headers["x-image-format"] == "PNG"
    assert "x-request-id" in res.headers
    out_img = Image.open(io.BytesIO(res.content))
    assert out_img.mode == "RGBA"

if __name__ == "__main__":
    print("Testing health...")
    test_health()
    print("Testing status...")
    test_status_endpoint()
    print("Testing upscale 2x...")
    test_upscale_2x()
    print("Testing upscale 4x...")
    test_upscale_4x()
    print("Testing invalid scale...")
    test_upscale_invalid_scale()
    print("Testing remove background...")
    test_remove_background()
    print("ALL BACKEND TESTS PASSED SUCCESSFULLY!")
