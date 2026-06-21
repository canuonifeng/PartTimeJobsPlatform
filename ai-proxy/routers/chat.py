import json
from datetime import date

from fastapi import APIRouter, Request
from pydantic import BaseModel
from sse_starlette.sse import EventSourceResponse

from prompts.job import FUNCTION_CALLING_SCHEMA, SYSTEM_PROMPT as _SYSTEM_PROMPT
from services.enterprise_client import enterprise_client
from services.qwen import qwen_service


def _system_prompt() -> str:
    return f"{_SYSTEM_PROMPT}\n\n当前真实日期：{date.today().isoformat()}"

router = APIRouter()


class ChatRequest(BaseModel):
    messages: list[dict]
    history: list[dict] = []


@router.post("/chat")
async def chat(request: Request, body: ChatRequest):
    company_id = getattr(request.state, "company_id", None)
    token = request.headers.get("Authorization", "").removeprefix("Bearer ")

    full_messages = [{"role": "system", "content": _system_prompt()}]
    for msg in body.history:
        full_messages.append(msg)
    for msg in body.messages:
        full_messages.append(msg)

    async def event_generator():
        buffer = ""
        try:
            async for chunk in qwen_service.chat_stream(full_messages, FUNCTION_CALLING_SCHEMA):
                if chunk.startswith("__FUNCTION_CALL__:"):
                    func_data = json.loads(chunk[len("__FUNCTION_CALL__:"):])
                    yield {"event": "function_call", "data": json.dumps(func_data, ensure_ascii=False)}
                    continue
                buffer += chunk
                yield {"event": "message", "data": json.dumps({"content": chunk}, ensure_ascii=False)}
            yield {"event": "done", "data": json.dumps({"content": buffer})}
        except Exception as e:
            yield {"event": "error", "data": json.dumps({"message": str(e)}, ensure_ascii=False)}

    return EventSourceResponse(event_generator())


class SyncChatRequest(BaseModel):
    messages: list[dict]
    history: list[dict] = []


@router.post("/chat/sync")
async def chat_sync(request: Request, body: SyncChatRequest):
    full_messages = [{"role": "system", "content": _system_prompt()}]
    for msg in body.history:
        full_messages.append(msg)
    for msg in body.messages:
        full_messages.append(msg)

    try:
        full_text = ""
        function_call = None
        async for chunk in qwen_service.chat_stream(full_messages, FUNCTION_CALLING_SCHEMA):
            if chunk.startswith("__FUNCTION_CALL__:"):
                function_call = json.loads(chunk[len("__FUNCTION_CALL__:"):])
                continue
            full_text += chunk

        return {
            "code": 200,
            "data": {
                "content": full_text,
                "function_call": function_call
            }
        }
    except Exception as e:
        return {"code": 500, "message": str(e)}


class ActionRequest(BaseModel):
    action: str
    data: dict
    token: str = ""


@router.post("/chat/execute")
async def execute_action(request: Request, body: ActionRequest):
    token = body.token or request.headers.get("Authorization", "").removeprefix("Bearer ") or ""
    try:
        if body.action == "create_job":
            result = await enterprise_client.create_job(body.data, token)
            return {"code": 200, "data": result, "message": "岗位创建成功"}
        elif body.action == "create_schedules":
            result = await enterprise_client.batch_create_schedules(body.data, token)
            return {"code": 200, "data": result, "message": "班次创建成功"}
        else:
            return {"code": 400, "message": f"Unknown action: {body.action}"}
    except Exception as e:
        return {"code": 500, "message": str(e)}
