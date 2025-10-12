import io
import json
import logging
import os
import tempfile
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from starlette.requests import Request
from PIL import Image

from model_loader import predict_garbage

# Structured logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(message)s')
logger = logging.getLogger("garbage_api")

app = FastAPI(title="Garbage Classifier API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.middleware("http")
async def add_process_time_header(request: Request, call_next):
    try:
        response = await call_next(request)
        return response
    except Exception as e:
        logger.exception("Unhandled error")
        return JSONResponse(status_code=500, content={"error": str(e)})

@app.get("/health")
async def health():
    return {"status": "ok"}

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    if not file:
        raise HTTPException(status_code=400, detail="file is required")
    suffix = os.path.splitext(file.filename or "upload.jpg")[1].lower() or ".jpg"
    try:
        # Read into temp file to be compatible with TF loaders
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            content = await file.read()
            tmp.write(content)
            tmp_path = tmp.name
        # Validate image
        try:
            Image.open(tmp_path).verify()
        except Exception:
            os.unlink(tmp_path)
            raise HTTPException(status_code=400, detail="Invalid image file")
        result = predict_garbage(tmp_path)
        logger.info(json.dumps({"event": "predicted", "class": result["class"], "confidence": result["confidence"]}))
        return result
    finally:
        try:
            if 'tmp_path' in locals() and os.path.exists(tmp_path):
                os.unlink(tmp_path)
        except Exception:
            logger.warning("Failed to cleanup temp file")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
