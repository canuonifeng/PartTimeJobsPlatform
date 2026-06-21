import os

import httpx


class EnterpriseClient:
    def __init__(self):
        self.base_url = os.getenv("ENTERPRISE_SERVICE_BASE_URL", "http://localhost:8081/enterprise")

    def _sanitize_token(self, token: str) -> str:
        return token.replace("\r", "").replace("\n", "").strip()

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

    async def create_job(self, data: dict, token: str) -> dict:
        return await self._post("/jobs", data, token)

    async def create_schedule(self, data: dict, token: str) -> dict:
        return await self._post("/jobs/schedules", data, token)

    async def batch_create_schedules(self, data: dict, token: str) -> dict:
        return await self._post("/schedules/batch-create", data, token)

    def _handle(self, resp: httpx.Response) -> dict:
        if resp.status_code >= 400:
            raise Exception(f"Enterprise service error: {resp.status_code} {resp.text}")
        return resp.json()


enterprise_client = EnterpriseClient()
