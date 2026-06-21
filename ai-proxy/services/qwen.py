import json
import os
from typing import AsyncGenerator

import httpx

DASHSCOPE_API_BASE = "https://dashscope.aliyuncs.com/api/v1"


class QwenService:
    def __init__(self):
        self.api_key = os.getenv("DASHSCOPE_API_KEY", "")
        self.model = os.getenv("QWEN_MODEL", "qwen-max")

    async def chat_stream(self, messages: list[dict], functions: list[dict] | None = None) -> AsyncGenerator[str, None]:
        body = {
            "model": self.model,
            "messages": messages,
            "stream": True,
        }
        if functions:
            body["functions"] = functions

        async with httpx.AsyncClient(timeout=120) as client:
            async with client.stream(
                "POST",
                f"{DASHSCOPE_API_BASE}/services/aigc/text-generation/generation",
                headers={
                    "Authorization": f"Bearer {self.api_key}",
                    "Content-Type": "application/json",
                },
                json=body,
            ) as resp:
                async for line in resp.aiter_lines():
                    if not line.startswith("data:"):
                        continue
                    data_str = line.removeprefix("data:").strip()
                    if data_str == "[DONE]":
                        break
                    try:
                        data = json.loads(data_str)
                        choice = data.get("output", {}).get("choices", [{}])[0]
                        delta = choice.get("delta", {})
                        content = delta.get("content", "")
                        if content:
                            yield content

                        function_call = delta.get("function_call")
                        if function_call:
                            yield f"__FUNCTION_CALL__:{json.dumps(function_call, ensure_ascii=False)}"
                    except json.JSONDecodeError:
                        continue


qwen_service = QwenService()
