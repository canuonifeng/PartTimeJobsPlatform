import os
import uuid
from datetime import datetime, timedelta

from fastapi import APIRouter, UploadFile, File
from fastapi.responses import JSONResponse

router = APIRouter()

UPLOAD_DIR = os.getenv("UPLOAD_DIR", "./uploads")
ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp", "image/gif"}
ALLOWED_AUDIO_TYPES = {"audio/mp3", "audio/mpeg", "audio/wav", "audio/ogg", "audio/x-m4a"}
MAX_FILE_SIZE = 20 * 1024 * 1024


@router.post("/upload")
async def upload_file(file: UploadFile = File(...)):
    if not file.content_type:
        return JSONResponse(status_code=400, content={"code": 400, "message": "Unknown file type"})

    file_type = "image" if file.content_type in ALLOWED_IMAGE_TYPES else "audio" if file.content_type in ALLOWED_AUDIO_TYPES else "unknown"
    if file_type == "unknown":
        return JSONResponse(status_code=400, content={"code": 400, "message": f"Unsupported file type: {file.content_type}"})

    ext = {"image/jpeg": ".jpg", "image/png": ".png", "image/webp": ".webp", "image/gif": ".gif",
           "audio/mp3": ".mp3", "audio/mpeg": ".mp3", "audio/wav": ".wav", "audio/ogg": ".ogg", "audio/x-m4a": ".m4a"}.get(file.content_type, "")

    filename = f"{uuid.uuid4().hex}{ext}"
    filepath = os.path.join(UPLOAD_DIR, filename)

    content = await file.read()
    if len(content) > MAX_FILE_SIZE:
        return JSONResponse(status_code=400, content={"code": 400, "message": "File too large"})

    os.makedirs(UPLOAD_DIR, exist_ok=True)
    with open(filepath, "wb") as f:
        f.write(content)

    return {
        "code": 200,
        "data": {
            "url": f"/uploads/{filename}",
            "filename": filename,
            "fileType": file_type,
            "size": len(content)
        },
        "message": "Upload success"
    }


@router.post("/upload/asr")
async def upload_and_asr(file: UploadFile = File(...)):
    result = await upload_file(file)
    if result.status_code != 200:
        return result

    from services.asr import asr_service
    ext = file.filename.rsplit(".", 1)[-1] if file.filename else "mp3"
    text = await asr_service.recognize(await file.read(), ext)

    return {
        "code": 200,
        "data": {"text": text},
        "message": "ASR success"
    }
