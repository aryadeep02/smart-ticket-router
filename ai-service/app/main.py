from pathlib import Path

import joblib
from fastapi import FastAPI
from pydantic import BaseModel


BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_DIR = BASE_DIR / "models"

CATEGORY_MODEL_PATH = MODEL_DIR / "category_model.joblib"
PRIORITY_MODEL_PATH = MODEL_DIR / "priority_model.joblib"


app = FastAPI(
    title="Smart Ticket Router AI Service",
    version="1.0.0"
)


class ClassificationRequest(BaseModel):
    title: str
    description: str


class ClassificationResponse(BaseModel):
    category: str
    priority: str
    confidence: float


def load_model(path: Path):
    if not path.exists():
        raise RuntimeError(
            f"Model file not found: {path}. "
            "Run train_model.py first."
        )

    try:
        return joblib.load(path)
    except Exception as exception:
        raise RuntimeError(
            f"Failed to load model: {path}. "
            "The model file may be corrupted or incompatible."
        ) from exception


category_model = load_model(CATEGORY_MODEL_PATH)
priority_model = load_model(PRIORITY_MODEL_PATH)


@app.get("/api/v1/health")
def health():
    return {
        "status": "UP",
        "service": "smart-ticket-router-ai"
    }


@app.post(
    "/api/v1/classify",
    response_model=ClassificationResponse
)
def classify_ticket(request: ClassificationRequest):

    text = f"{request.title} {request.description}"

    category_prediction = category_model.predict([text])[0]
    priority_prediction = priority_model.predict([text])[0]

    category_probabilities = category_model.predict_proba([text])[0]
    priority_probabilities = priority_model.predict_proba([text])[0]

    category_confidence = max(category_probabilities)
    priority_confidence = max(priority_probabilities)

    confidence = (
    category_confidence + priority_confidence
) / 2

    return ClassificationResponse(
        category=category_prediction,
        priority=priority_prediction,
        confidence=float(confidence)
    )