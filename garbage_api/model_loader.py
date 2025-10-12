import os
import numpy as np
import logging
from tensorflow.keras.models import load_model
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D
from tensorflow.keras import Model, Input
from tensorflow.keras.preprocessing import image

logger = logging.getLogger("garbage_api.model")

CLASS_LABELS = ['cardboard','glass','metal','paper','plastic','trash']

# Class mapping from notebook 12 classes to mobile app 6 classes
CLASS_MAPPING = {
    0: 3,  # paper -> paper
    1: 0,  # cardboard -> cardboard
    2: 4,  # plastic -> plastic
    3: 2,  # metal -> metal
    4: 5,  # trash -> trash
    5: 5,  # battery -> trash (hazardous waste)
    6: 5,  # shoes -> trash (textile waste)
    7: 5,  # clothes -> trash (textile waste)
    8: 1,  # green-glass -> glass
    9: 1,  # brown-glass -> glass
    10: 1, # white-glass -> glass
    11: 5  # biological -> trash (organic waste)
}
MODEL_PATH = os.getenv('MODEL_PATH', 'garbage_classifier.h5')

_model = None

def check_model_status():
    """Check if trained model exists and return status"""
    if os.path.exists(MODEL_PATH):
        return f"Using trained model: {MODEL_PATH}"
    else:
        return f"Using fallback model. Place trained model at: {MODEL_PATH}"

def _build_fallback_model():
    # Use weights=None to avoid external downloads in environments without internet
    base = MobileNetV2(weights=None, include_top=False, input_shape=(224, 224, 3))
    x = base.output
    x = GlobalAveragePooling2D()(x)
    out = Dense(len(CLASS_LABELS), activation='softmax')(x)
    model = Model(inputs=base.input, outputs=out)
    logger.warning("Using fallback MobileNetV2 model (untrained). Provide garbage_classifier.h5 for real predictions.")
    return model

def get_model():
    global _model
    if _model is None:
        if os.path.exists(MODEL_PATH):
            logger.info(f"Loading model from {MODEL_PATH}")
            _model = load_model(MODEL_PATH)
            logger.info("Model loaded successfully")
        else:
            logger.warning(f"Model file not found at {MODEL_PATH}. Using fallback model.")
            _model = _build_fallback_model()
    return _model

def preprocess(img_path: str):
    img = image.load_img(img_path, target_size=(224, 224))
    arr = image.img_to_array(img)
    arr = np.expand_dims(arr, axis=0)
    arr = arr / 255.0
    return arr

def predict_garbage(img_path: str):
    model = get_model()
    logger.info(f"Using model: {'trained' if os.path.exists(MODEL_PATH) else 'fallback'}")
    arr = preprocess(img_path)
    logger.info(f"Preprocessed image shape: {arr.shape}")
    preds = model.predict(arr, verbose=0)
    logger.info(f"Raw predictions: {preds[0]}")
    idx = int(np.argmax(preds[0]))
    conf = float(preds[0][idx])
    logger.info(f"Predicted class index: {idx}, confidence: {conf}")
    return {"class": CLASS_LABELS[idx], "confidence": conf}
