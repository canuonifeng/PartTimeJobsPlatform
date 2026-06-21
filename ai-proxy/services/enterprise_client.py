import os

import httpx


class EnterpriseClient:
    def __init__(self):
        self.base_url = os.getenv("ENTERPRISE_SERVICE_BASE_URL", "http://localhost:8081/api")

    def _sanitize_token(self, token: str) -> str:
        return token.replace("\r", "").replace("\n", "").strip()

    async def _get(self, path: str, params: dict | None, token: str) -> dict:
        token = self._sanitize_token(token)
        if not token:
            raise Exception("Missing or empty auth token")
        async with httpx.AsyncClient(timeout=30) as client:
            resp = await client.get(
                f"{self.base_url}{path}",
                params=params,
                headers={"Authorization": f"Bearer {token}"},
            )
            return self._handle(resp)

    async def _post(self, path: str, data: dict, token: str) -> dict:
        token = self._sanitize_token(token)
        if not token:
            raise Exception("Missing or empty auth token - user may need to log in again")
        async with httpx.AsyncClient(timeout=30) as client:
            resp = await client.post(
                f"{self.base_url}{path}",
                json=data,
                headers={"Authorization": f"Bearer {token}"},
            )
            return self._handle(resp)

    async def search_jobs_by_title(self, title: str, token: str) -> list[dict]:
        resp = await self._get("/enterprise/jobs", {"status": ""}, token)
        jobs = resp.get("data") or []
        matched = [j for j in jobs if j.get("title") == title]
        return matched

    async def get_job(self, job_id: int, token: str) -> dict:
        resp = await self._get("/enterprise/jobs", {"id": job_id}, token)
        return resp.get("data") or {}

    async def update_job(self, job_id: int, data: dict, token: str) -> dict:
        body = {"id": job_id}
        for field in ["title", "description", "requirements", "headcount", "contactName",
                       "contactPhone", "province", "city", "district", "address",
                       "latitude", "longitude", "categoryId"]:
            if field in data and data[field] is not None:
                body[field] = data[field]
        if "deadline" in data and data["deadline"] is not None:
            body["deadline"] = self._fmt_deadline(data["deadline"])
        if "salaryType" in data or "salaryAmount" in data:
            body["rates"] = [{
                "type": data.get("salaryType", "HOURLY"),
                "amount": data.get("salaryAmount", 0),
                "currency": "CNY",
            }]
        return await self._post("/enterprise/jobs/update", body, token)

    async def publish_job(self, job_id: int, token: str) -> dict:
        return await self._post("/enterprise/jobs/publish", {"id": job_id}, token)

    @staticmethod
    def _fmt_deadline(value: str | None) -> str | None:
        if not value:
            return None
        if ":" not in value:
            return value + " 23:59:59"
        return value

    async def create_job(self, data: dict, token: str) -> dict:
        body = {
            "title": data.get("title"),
            "description": data.get("description"),
            "requirements": data.get("requirements"),
            "headcount": data.get("headcount"),
            "contactName": data.get("contactName"),
            "contactPhone": data.get("contactPhone"),
            "province": data.get("province"),
            "city": data.get("city"),
            "district": data.get("district"),
            "address": data.get("address"),
            "latitude": data.get("latitude"),
            "longitude": data.get("longitude"),
            "categoryId": data.get("categoryId"),
            "deadline": self._fmt_deadline(data.get("deadline")),
            "rates": [{"type": data.get("salaryType", "HOURLY"), "amount": data.get("salaryAmount", 0), "currency": "CNY"}],
            "schedules": [
                {
                    "scheduleDate": s["scheduleDate"],
                    "startTime": s["startTime"],
                    "endTime": s["endTime"],
                    "slotsAvailable": s.get("slotsAvailable"),
                    "scheduleName": s.get("scheduleName"),
                    "contactName": s.get("contactName"),
                    "contactPhone": s.get("contactPhone"),
                }
                for s in (data.get("schedules") or [])
            ],
        }
        return await self._post("/enterprise/jobs", body, token)

    async def create_schedule(self, data: dict, token: str) -> dict:
        body = {
            "jobId": data.get("jobId"),
            "scheduleDate": data.get("scheduleDate"),
            "startTime": data.get("startTime"),
            "endTime": data.get("endTime"),
        }
        if data.get("slotsAvailable") is not None:
            body["slotsAvailable"] = data["slotsAvailable"]
        if data.get("scheduleName"):
            body["scheduleName"] = data["scheduleName"]
        if data.get("contactName"):
            body["contactName"] = data["contactName"]
        if data.get("contactPhone"):
            body["contactPhone"] = data["contactPhone"]
        return await self._post("/enterprise/jobs/schedules", body, token)

    async def update_schedule(self, data: dict, token: str) -> dict:
        return await self._post("/enterprise/schedules/update", data, token)

    async def copy_schedule(self, data: dict, token: str) -> dict:
        return await self._post("/enterprise/schedules/copy", data, token)

    async def batch_create_schedules(self, data: dict, token: str) -> dict:
        return await self._post("/enterprise/schedules/batch-create", data, token)

    async def list_jobs(self, status: str, token: str) -> dict:
        return await self._get("/enterprise/jobs", {"status": status}, token)

    async def close_job(self, job_id: int, token: str) -> dict:
        return await self._post("/enterprise/jobs/close", {"id": job_id}, token)

    async def reopen_job(self, job_id: int, token: str) -> dict:
        return await self._post("/enterprise/jobs/reopen", {"id": job_id}, token)

    async def list_job_schedules(self, job_id: int, token: str) -> dict:
        return await self._get("/enterprise/jobs/schedules", {"jobId": job_id}, token)

    async def list_applications(self, params: dict, token: str) -> dict:
        return await self._get("/enterprise/applications", params, token)

    async def accept_application(self, application_id: int, token: str) -> dict:
        return await self._post("/enterprise/applications/accept", {"applicationId": application_id}, token)

    async def reject_application(self, application_id: int, token: str) -> dict:
        return await self._post("/enterprise/applications/reject", {"applicationId": application_id}, token)

    async def list_attendance(self, params: dict, token: str) -> dict:
        return await self._get("/enterprise/attendance/hours", params, token)

    async def update_attendance_hours(self, data: dict, token: str) -> dict:
        return await self._post("/enterprise/attendance/hours/update", data, token)

    async def batch_pay_attendance(self, ids: list[int], token: str) -> dict:
        return await self._post("/enterprise/attendance/hours/pay", {"ids": ids}, token)

    async def unsettle_attendance(self, attendance_record_id: int, token: str) -> dict:
        return await self._post("/enterprise/settlement/unsettle", {"attendanceRecordId": attendance_record_id}, token)

    def _handle(self, resp: httpx.Response) -> dict:
        if resp.status_code >= 400:
            raise Exception(f"Enterprise service error: {resp.status_code} {resp.text}")
        return resp.json()


enterprise_client = EnterpriseClient()
