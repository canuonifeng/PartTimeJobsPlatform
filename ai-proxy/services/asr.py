import os

import httpx

ALIBABA_ASR_URL = "https://nls-meta.cn-shanghai.aliyuncs.com/api/v1/recognizer"


class ASRService:
    def __init__(self):
        self.access_key_id = os.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID", "")
        self.access_key_secret = os.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET", "")
        self.region = os.getenv("ALIBABA_CLOUD_REGION", "cn-shanghai")

    async def recognize(self, audio_bytes: bytes, audio_format: str = "mp3") -> str:
        if not self.access_key_id or not self.access_key_secret:
            return ""

        async with httpx.AsyncClient(timeout=60) as client:
            resp = await client.post(
                f"{ALIBABA_ASR_URL}/{audio_format}",
                headers={
                    "Content-Type": "application/octet-stream",
                    "X-Access-Key-Id": self.access_key_id,
                    "X-Access-Key-Secret": self.access_key_secret,
                },
                content=audio_bytes,
            )
            result = resp.json()
            return result.get("result", "")


asr_service = ASRService()
