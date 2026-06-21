import json
from datetime import date

from fastapi import APIRouter, Request
from pydantic import BaseModel
from sse_starlette.sse import EventSourceResponse

from prompts.job import FUNCTION_CALLING_SCHEMA, SYSTEM_PROMPT as _SYSTEM_PROMPT
from services.enterprise_client import enterprise_client
from services.geocoding import geocode
from services.qwen import qwen_service


def _system_prompt() -> str:
    return f"{_SYSTEM_PROMPT}\n\n当前真实日期：{date.today().isoformat()}"

router = APIRouter()


class ChatRequest(BaseModel):
    messages: list[dict]
    history: list[dict] = []


async def _check_job_duplicate(func_data: dict, token: str) -> str | None:
    if func_data.get("name") != "create_job_and_schedules":
        return None
    try:
        args = func_data.get("arguments", {})
        if isinstance(args, str):
            args = json.loads(args)
        title = args.get("title", "").strip()
        if not title:
            return None
        existing = await enterprise_client.search_jobs_by_title(title, token)
        if existing:
            job = existing[0]
            msg = (
                f"⚠️ 已有一个同名岗位「{title}」（ID: {job.get('id')}，"
                f"状态: {job.get('status', '未知')}）。"
                f"请问您要：\n"
                f"1️⃣ 修改已有岗位\n"
                f"2️⃣ 新增班次到已有岗位\n"
                f"3️⃣ 重新创建新岗位\n"
                f"请直接回复数字或说明您的选择。"
            )
            return msg
    except Exception:
        pass
    return None


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
                    duplicate_msg = await _check_job_duplicate(func_data, token)
                    if duplicate_msg:
                        buffer += duplicate_msg
                        yield {"event": "message", "data": json.dumps({"content": duplicate_msg}, ensure_ascii=False)}
                        continue
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
    token = request.headers.get("Authorization", "").removeprefix("Bearer ")
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
                func_data = json.loads(chunk[len("__FUNCTION_CALL__:"):])
                duplicate_msg = await _check_job_duplicate(func_data, token)
                if duplicate_msg:
                    full_text += duplicate_msg
                else:
                    function_call = func_data
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


async def _enrich_location(data: dict) -> dict:
    result = None
    address = (
        data.get("address")
        or f"{data.get('province', '')}{data.get('city', '')}{data.get('district', '')}"
    )
    if address:
        city = data.get("city") or ""
        result = await geocode(address, city)

    if result:
        if not data.get("province") or (result["province"] and result["province"] != data.get("province")):
            data["province"] = result["province"]
        if not data.get("city") or (result["city"] and result["city"] != data.get("city")):
            data["city"] = result["city"]
        if not data.get("district") or (result["district"] and result["district"] != data.get("district")):
            data["district"] = result["district"]
        if not data.get("address"):
            data["address"] = result["address"]
        if not data.get("latitude") and result.get("latitude"):
            data["latitude"] = result["latitude"]
        if not data.get("longitude") and result.get("longitude"):
            data["longitude"] = result["longitude"]
    return data


@router.post("/chat/execute")
async def execute_action(request: Request, body: ActionRequest):
    token = body.token or request.headers.get("Authorization", "").removeprefix("Bearer ") or ""
    try:
        if body.action == "create_job":
            data = await _enrich_location(body.data)
            result = await enterprise_client.create_job(data, token)
            job_id = result.get("data", {}).get("id")
            final = result
            if job_id:
                try:
                    pub = await enterprise_client.publish_job(job_id, token)
                    final = pub
                except Exception:
                    pass
            return {"code": 200, "data": final, "message": "岗位创建成功"}
        elif body.action == "update_job":
            data = await _enrich_location(body.data)
            job_id = data.get("jobId") or data.get("id")
            result = await enterprise_client.update_job(job_id, data, token)
            return {"code": 200, "data": result, "message": "岗位更新成功"}
        elif body.action == "add_schedule":
            result = await enterprise_client.create_schedule(body.data, token)
            return {"code": 200, "data": result, "message": "班次创建成功"}
        elif body.action == "create_schedules":
            result = await enterprise_client.batch_create_schedules(body.data, token)
            return {"code": 200, "data": result, "message": "班次创建成功"}
        else:
            return {"code": 400, "message": f"Unknown action: {body.action}"}
    except Exception as e:
        return {"code": 500, "message": str(e)}
