import json
import os
from typing import AsyncGenerator

import httpx

CHAT_COMPLETIONS_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"


class QwenService:
    def __init__(self):
        self.api_key = os.getenv("DASHSCOPE_API_KEY", "")
        self.model = os.getenv("QWEN_MODEL", "qwen-max")

    def _check_api_key(self):
        if not self.api_key:
            raise Exception("DASHSCOPE_API_KEY not configured. Set it in ai-proxy/.env or environment variables.")
    async def chat_stream(self, messages: list[dict], functions: list[dict] | None = None) -> AsyncGenerator[str, None]:
        self._check_api_key()

        body = {
            "model": self.model,
            "messages": messages,
            "stream": True,
        }
        if functions:
            body["tools"] = [{"type": "function", "function": f} for f in functions]

        async with httpx.AsyncClient(timeout=120) as client:
            async with client.stream(
                "POST",
                CHAT_COMPLETIONS_URL,
                headers={
                    "Authorization": f"Bearer {self.api_key}",
                    "Content-Type": "application/json",
                },
                json=body,
            ) as resp:
                if resp.status_code != 200:
                    error_body = await resp.aread()
                    raise Exception(f"DashScope API error {resp.status_code}: {error_body.decode()}")

                tool_call_acc: dict[int, dict] = {}

                async for line in resp.aiter_lines():
                    if not line.startswith("data:"):
                        continue
                    data_str = line.removeprefix("data:").strip()
                    if data_str == "[DONE]":
                        for tc in tool_call_acc.values():
                            if tc.get("name"):
                                yield f"__FUNCTION_CALL__:{json.dumps(tc, ensure_ascii=False)}"
                        break
                    try:
                        data = json.loads(data_str)
                        choices = data.get("choices", [])
                        for choice in choices:
                            delta = choice.get("delta", {})
                            content = delta.get("content", "")
                            if content:
                                yield content

                            for tc in delta.get("tool_calls", []):
                                idx = tc.get("index", 0)
                                if idx not in tool_call_acc:
                                    tool_call_acc[idx] = {"name": "", "arguments": ""}
                                func = tc.get("function", {})
                                if func.get("name"):
                                    tool_call_acc[idx]["name"] = func["name"]
                                args = func.get("arguments")
                                if args is not None:
                                    tool_call_acc[idx]["arguments"] += args
                    except json.JSONDecodeError:
                        continue


qwen_service = QwenService()
