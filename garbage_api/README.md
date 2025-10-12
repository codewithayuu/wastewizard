# Garbage Classifier API (MobileNetV2)

A FastAPI backend that serves a MobileNetV2-based garbage classifier. Accepts multipart/form-data image uploads and returns the predicted class and confidence.

- Backbone: MobileNetV2 (TensorFlow/Keras)
- Input size: 224x224x3
- Normalization: [0, 1]
- Labels: `['cardboard','glass','metal','paper','plastic','trash']`

If `garbage_classifier.h5` is not present, a fallback MobileNetV2 is built (untrained) so you can run the API and tests; provide a trained model for real predictions.

## Setup

```bash
pip install -r requirements.txt
```

## Run

```bash
uvicorn app:app --host 0.0.0.0 --port 8000
```

## Test using curl

```bash
curl -X POST -F "file=@sample.jpg" http://127.0.0.1:8000/predict
```

### Expected output

```json
{ "class": "glass", "confidence": 0.91 }
```

## React Native integration

```javascript
const API_URL = 'http://<SERVER_IP>:8000/predict';

export async function predictGarbage(imageUri) {
  const formData = new FormData();
  formData.append('file', {
    uri: imageUri,
    name: 'photo.jpg',
    type: 'image/jpeg'
  });

  const res = await fetch(API_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'multipart/form-data' },
    body: formData
  });
  return await res.json();
}
```

Replace `<SERVER_IP>` with your backend machine’s IP address.

## Tests

Run pytest from this directory after installing requirements:

```bash
python -m pytest -q
```

## Docker

```dockerfile
FROM python:3.11-slim
WORKDIR /app
COPY . .
RUN pip install -r requirements.txt
EXPOSE 8000
CMD ["uvicorn","app:app","--host","0.0.0.0","--port","8000"]
```
