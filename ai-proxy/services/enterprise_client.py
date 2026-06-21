import os

import httpx


class EnterpriseClient:
    def __init__(self):
        self.base_url = os.getenv("ENTERPRISE_SERVICE_BASE_URL", "http://localhost:8081/enterprise")

    async def create_job(self, data: dict, token: str) -> dict:
        async with httpx.AsyncClient(timeout=30) as client:
            resp = await client.post(
                f"{self.base_url}/jobs",
                json=data,
                headers={"Authorization": f"Bearer {token}"},
            )
            return self._handle(resp)

    async def create_schedule(self, data: dict, token: str) -> dict:
        async with httpx.AsyncClient(timeout=30) as client:
            resp = await client.post(
                f"{self.base_url}/jobs/schedules",
                json=data,
                headers={"Authorization": f"Bearer {token}"},
            )
            return self._handle(resp)

    async def batch_create_schedules(self, data: dict, token: str) -> dict:
        async with httpx.AsyncClient(timeout=30) as client:
            resp = await client.post(
                f"{self.base_url}/schedules/batch-create",
                json=data,
                headers={"Authorization": f"Bearer {token}"},
            )
            return self._handle(resp)

    def _handle(self, resp: httpx.Response) -> dict:
        if resp.status_code >= 400:
            raise Exception(f"Enterprise service error: {resp.status_code} {resp.text}")
        return resp.json()


enterprise_client = EnterpriseClient()
