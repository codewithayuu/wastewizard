from fastapi.testclient import TestClient
from app import app
from io import BytesIO
from PIL import Image

client = TestClient(app)

def test_health():
    r = client.get("/health")
    assert r.status_code == 200
    assert r.json() == {"status":"ok"}


def test_predict_mock_image():
    # create an in-memory image
    img = Image.new('RGB', (224, 224), color=(123, 222, 64))
    buf = BytesIO()
    img.save(buf, format='JPEG')
    buf.seek(0)

    files = { 'file': ('test.jpg', buf, 'image/jpeg') }
    r = client.post('/predict', files=files)
    assert r.status_code == 200
    data = r.json()
    assert 'class' in data and 'confidence' in data
