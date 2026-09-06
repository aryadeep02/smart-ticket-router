from fastapi import FastAPI
from pydantic import BaseModel


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


@app.get("/api/v1/health")
def health():
    return {
        "status": "UP",
        "service": "smart-ticket-router-ai"
    }


@app.post("/api/v1/classify", response_model=ClassificationResponse)
def classify_ticket(request: ClassificationRequest):

    return ClassificationResponse(
        category="PAYMENT",
        priority="HIGH",
        confidence=0.90
    )