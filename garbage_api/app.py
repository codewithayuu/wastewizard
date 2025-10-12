from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from model_loader import predict_garbage, check_model_status
import logging
import os
import tempfile
from PIL import Image

# Set up logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Garbage Classifier API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
async def health():
    model_status = check_model_status()
    return {"status": "ok", "model": model_status}

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    try:
        logger.info("Received prediction request")
        contents = await file.read()
        with tempfile.NamedTemporaryFile(delete=False, suffix='.jpg') as tmp:
            tmp.write(contents)
            tmp_path = tmp.name
        
        # Validate image
        try:
            Image.open(tmp_path).verify()
        except Exception:
            os.unlink(tmp_path)
            raise HTTPException(status_code=400, detail="Invalid image file")
        
        result = predict_garbage(tmp_path)
        logger.info(f"Prediction result: {result}")
        
        os.unlink(tmp_path)
        return result
    except Exception as e:
        logger.error(f"Prediction failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
