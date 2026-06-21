import os

import jwt
from fastapi import Request
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.types import ASGIApp

EXEMPT_PATHS = {"/api/health", "/api/chat", "/api/chat/sync", "/api/chat/execute", "/docs", "/openapi.json", "/redoc"}
EXEMPT_PREFIXES = {"/uploads", "/api/upload"}

EXEMPT_PREFIXES_RAW = {"/api/chat", "/api/upload"}


class JWTAuthMiddleware(BaseHTTPMiddleware):
    def __init__(self, app: ASGIApp):
        super().__init__(app)
        self.secret = os.getenv("JWT_SECRET", "parttime-enterprise-jwt-secret-key-must-be-at-least-256-bits")
        print(f"[ai-proxy] JWT_SECRET set: {self.secret[:20]}... (len={len(self.secret)})")

    async def dispatch(self, request: Request, call_next):
        path = request.url.path

        if path in EXEMPT_PATHS or any(path.startswith(p) for p in EXEMPT_PREFIXES):
            return await call_next(request)

        auth = request.headers.get("Authorization", "")
        if not auth.startswith("Bearer "):
            return JSONResponse(status_code=401, content={"code": 401, "message": "Missing token"})

        token = auth[7:]
        try:
            payload = jwt.decode(token, self.secret, algorithms=["HS256", "HS384", "HS512"])
            request.state.user_id = payload.get("sub") or payload.get("userId") or payload.get("id")
            request.state.company_id = payload.get("companyId")
        except jwt.ExpiredSignatureError:
            return JSONResponse(status_code=401, content={"code": 401, "message": "Token expired"})
        except jwt.InvalidTokenError as e:
            return JSONResponse(status_code=401, content={"code": 401, "message": f"Invalid token: {e}"})

        return await call_next(request)
