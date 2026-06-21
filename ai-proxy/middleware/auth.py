import os

import jwt
from fastapi import Request
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.types import ASGIApp

EXEMPT_PATHS = {"/api/health", "/docs", "/openapi.json", "/redoc"}
EXEMPT_PREFIXES = {"/uploads"}


class JWTAuthMiddleware(BaseHTTPMiddleware):
    def __init__(self, app: ASGIApp):
        super().__init__(app)
        self.secret = os.getenv("JWT_SECRET", "dev-secret-change-in-production")

    async def dispatch(self, request: Request, call_next):
        path = request.url.path

        if path in EXEMPT_PATHS or any(path.startswith(p) for p in EXEMPT_PREFIXES):
            return await call_next(request)

        auth = request.headers.get("Authorization", "")
        if not auth.startswith("Bearer "):
            return JSONResponse(status_code=401, content={"code": 401, "message": "Missing token"})

        token = auth[7:]
        try:
            payload = jwt.decode(token, self.secret, algorithms=["HS256"])
            request.state.user_id = payload.get("userId") or payload.get("id")
            request.state.company_id = payload.get("companyId")
        except jwt.ExpiredSignatureError:
            return JSONResponse(status_code=401, content={"code": 401, "message": "Token expired"})
        except jwt.InvalidTokenError:
            return JSONResponse(status_code=401, content={"code": 401, "message": "Invalid token"})

        return await call_next(request)
