import os
from contextlib import asynccontextmanager

from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from middleware.auth import JWTAuthMiddleware
from routers import chat, upload

load_dotenv()

UPLOAD_DIR = os.getenv("UPLOAD_DIR", "./uploads")


@asynccontextmanager
async def lifespan(app: FastAPI):
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    yield


app = FastAPI(title="AI Proxy", version="1.0.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.add_middleware(JWTAuthMiddleware)

app.include_router(chat.router, prefix="/api")
app.include_router(upload.router, prefix="/api")

if os.path.isdir(UPLOAD_DIR):
    app.mount("/uploads", StaticFiles(directory=UPLOAD_DIR), name="uploads")


@app.get("/api/health")
async def health():
    return {"status": "ok"}
