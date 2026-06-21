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
        tool_turn = 0
        try:
            async for chunk in qwen_service.chat_stream(full_messages, FUNCTION_CALLING_SCHEMA):
                if chunk.startswith("__FUNCTION_CALL__:"):
                    func_data = json.loads(chunk[len("__FUNCTION_CALL__:"):])
                    name = func_data.get("name")

                    if name in ("search_jobs", "query_data"):
                        result = await _execute_inline(func_data, tool_turn, token)
                        tool_turn += 1
                        args = func_data.get("arguments", {})
                        if isinstance(args, str):
                            args = json.loads(args)
                        full_messages.append({
                            "role": "assistant", "content": None,
                            "tool_calls": [{
                                "id": f"{name}_{tool_turn}", "type": "function",
                                "function": {"name": name, "arguments": json.dumps(args, ensure_ascii=False)}
                            }]
                        })
                        full_messages.append({
                            "role": "tool", "tool_call_id": f"{name}_{tool_turn}",
                            "content": result or "无数据"
                        })
                        continue

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
        return await _chat_sync_with_tools(full_messages, token)
    except Exception as e:
        return {"code": 500, "message": str(e)}


async def _execute_inline(func_data: dict, turn: int, token: str) -> str | None:
    """Execute inline function (search_jobs, query_data) and return tool result content."""
    name = func_data.get("name")
    args = func_data.get("arguments", {})
    if isinstance(args, str):
        args = json.loads(args)

    if name == "search_jobs":
        keyword = args.get("keyword", "")
        try:
            results = await enterprise_client.search_jobs_by_title(keyword, token)
        except Exception:
            results = []
        if results:
            lines = [f"ID: {j.get('id')}, 名称: {j.get('title')}, 状态: {j.get('status', '未知')}" for j in results]
            return f"找到以下岗位：\n" + "\n".join(lines)
        else:
            return f"未找到名称包含「{keyword}」的岗位"

    if name == "query_data":
        qtype = args.get("type")
        try:
            if qtype == "jobs":
                resp = await enterprise_client.list_jobs(args.get("status", ""), token)
                items = resp.get("data") or []
                return "\n".join([f"ID:{j.get('id')} {j.get('title')} {j.get('status','')} 人数:{j.get('headcount','')} 联系人:{j.get('contactName','')}" for j in items]) or "暂无岗位"

            elif qtype == "job_detail":
                resp = await enterprise_client.get_job(args.get("jobId"), token)
                j = resp if isinstance(resp, dict) else {}
                if not j or not j.get("id"):
                    return "未找到该岗位"
                return (f"岗位: {j.get('title')}\n状态: {j.get('status')}\n职责: {j.get('description','')}\n"
                        f"要求: {j.get('requirements','')}\n人数: {j.get('headcount')}\n"
                        f"联系人: {j.get('contactName')} {j.get('contactPhone')}\n"
                        f"地址: {j.get('province','')}{j.get('city','')}{j.get('district','')}{j.get('address','')}")

            elif qtype == "schedules":
                resp = await enterprise_client.list_job_schedules(args.get("jobId"), token)
                items = resp.get("data") or []
                return "\n".join([f"ID:{s.get('id')} {s.get('scheduleDate')} {str(s.get('startTime',''))[:5]}-{str(s.get('endTime',''))[:5]} 容量:{s.get('slotsAvailable','')} 状态:{s.get('status','')}" for s in items]) or "暂无班次"

            elif qtype == "applications":
                params = {}
                if args.get("jobId"): params["jobId"] = args["jobId"]
                if args.get("jobTitle"): params["jobTitle"] = args["jobTitle"]
                if args.get("status"): params["status"] = args["status"]
                if args.get("scheduleId"): params["scheduleId"] = args["scheduleId"]
                params["pageSize"] = 100
                resp = await enterprise_client.list_applications(params, token)
                data = resp.get("data") or {}
                items = data.get("records") or data.get("data") or data or []
                if isinstance(items, dict):
                    items = items.get("records") or []
                return "\n".join([f"ID:{a.get('id')} 工人:{a.get('workerName')} 岗位:{a.get('jobTitle')} 班次:{a.get('scheduleDate')} 状态:{a.get('status')}" for a in items]) or "暂无报名记录"

            elif qtype == "attendance":
                params = {}
                if args.get("jobId"): params["jobId"] = args["jobId"]
                if args.get("jobTitle"): params["jobTitle"] = args["jobTitle"]
                if args.get("workerName"): params["workerName"] = args["workerName"]
                if args.get("settlementStatus"): params["settlementStatus"] = args["settlementStatus"]
                if args.get("dateFrom"): params["dateFrom"] = args["dateFrom"]
                if args.get("dateTo"): params["dateTo"] = args["dateTo"]
                params["pageSize"] = 100
                resp = await enterprise_client.list_attendance(params, token)
                data = resp.get("data") or {}
                items = data.get("records") or data.get("data") or data or []
                if isinstance(items, dict):
                    items = items.get("records") or []
                return "\n".join([f"ID:{a.get('id')} 工人:{a.get('workerName')} 岗位:{a.get('jobTitle')} 日期:{a.get('shiftDate')} 工时:{a.get('totalHours')} 应付:{a.get('payablePay')} 状态:{a.get('settlementStatus')} 考勤:{a.get('status','')}" for a in items]) or "暂无考勤记录"
        except Exception as e:
            return f"查询失败: {str(e)}"

    return None


async def _chat_sync_with_tools(messages: list[dict], token: str) -> dict:
    full_text = ""
    function_call = None
    max_turns = 5
    inline_funcs = {"search_jobs", "query_data"}
    confirm_funcs = {"execute_action", "batch_action"}

    for turn in range(max_turns):
        current_text = ""
        current_function_call = None

        async for chunk in qwen_service.chat_stream(messages, FUNCTION_CALLING_SCHEMA):
            if chunk.startswith("__FUNCTION_CALL__:"):
                func_data = json.loads(chunk[len("__FUNCTION_CALL__:"):])
                name = func_data.get("name")

                if name in inline_funcs:
                    result = await _execute_inline(func_data, turn, token)
                    args = func_data.get("arguments", {})
                    if isinstance(args, str):
                        args = json.loads(args)

                    messages.append({
                        "role": "assistant",
                        "content": None,
                        "tool_calls": [{
                            "id": f"{name}_{turn}",
                            "type": "function",
                            "function": {"name": name, "arguments": json.dumps(args, ensure_ascii=False)}
                        }]
                    })
                    messages.append({
                        "role": "tool",
                        "tool_call_id": f"{name}_{turn}",
                        "content": result or "无数据"
                    })
                    break

                if name in confirm_funcs:
                    current_function_call = func_data
                    continue

                duplicate_msg = await _check_job_duplicate(func_data, token)
                if duplicate_msg:
                    current_text += duplicate_msg
                else:
                    current_function_call = func_data
                continue

            current_text += chunk
        else:
            full_text += current_text
            function_call = current_function_call
            break

        full_text += current_text
        if current_function_call:
            function_call = current_function_call
            break

    return {
        "code": 200,
        "data": {
            "content": full_text,
            "function_call": function_call
        }
    }


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
        elif body.action == "update_schedule":
            result = await enterprise_client.update_schedule(body.data, token)
            return {"code": 200, "data": result, "message": "班次更新成功"}
        elif body.action == "copy_schedule":
            result = await enterprise_client.copy_schedule(body.data, token)
            return {"code": 200, "data": result, "message": "班次复制成功"}
        elif body.action == "create_schedules":
            result = await enterprise_client.batch_create_schedules(body.data, token)
            return {"code": 200, "data": result, "message": "班次创建成功"}
        elif body.action == "accept_application":
            result = await enterprise_client.accept_application(body.data.get("targetId"), token)
            return {"code": 200, "data": result, "message": "已通过报名"}
        elif body.action == "reject_application":
            result = await enterprise_client.reject_application(body.data.get("targetId"), token)
            return {"code": 200, "data": result, "message": "已拒绝报名"}
        elif body.action == "close_job":
            result = await enterprise_client.close_job(body.data.get("targetId"), token)
            return {"code": 200, "data": result, "message": "岗位已关闭"}
        elif body.action == "reopen_job":
            result = await enterprise_client.reopen_job(body.data.get("targetId"), token)
            return {"code": 200, "data": result, "message": "岗位已重新开放"}
        elif body.action == "cancel_schedule":
            result = await enterprise_client.update_schedule({"id": body.data.get("targetId"), "status": "CANCELLED"}, token)
            return {"code": 200, "data": result, "message": "班次已取消"}
        elif body.action == "update_attendance_hours":
            data = {"id": body.data.get("targetId")}
            updates = body.data.get("updates") or {}
            data.update(updates)
            result = await enterprise_client.update_attendance_hours(data, token)
            return {"code": 200, "data": result, "message": "工时已更新"}
        elif body.action == "pay_attendance":
            target_id = body.data.get("targetId")
            result = await enterprise_client.batch_pay_attendance([target_id], token)
            return {"code": 200, "data": result, "message": "结算成功"}
        elif body.action == "unsettle_attendance":
            result = await enterprise_client.unsettle_attendance(body.data.get("targetId"), token)
            return {"code": 200, "data": result, "message": "已撤回结算"}
        elif body.action == "batch_accept":
            target_ids = body.data.get("targetIds") or []
            results = []
            for tid in target_ids:
                try:
                    r = await enterprise_client.accept_application(tid, token)
                    results.append(r)
                except Exception as e:
                    results.append({"id": tid, "error": str(e)})
            ok = sum(1 for r in results if "error" not in r)
            return {"code": 200, "data": results, "message": f"批量通过完成：成功{ok}条，失败{len(results)-ok}条"}
        elif body.action == "batch_pay":
            target_ids = body.data.get("targetIds") or []
            result = await enterprise_client.batch_pay_attendance(target_ids, token)
            return {"code": 200, "data": result, "message": f"批量结算完成，共{len(target_ids)}条"}
        else:
            return {"code": 400, "message": f"Unknown action: {body.action}"}
    except Exception as e:
        return {"code": 500, "message": str(e)}
