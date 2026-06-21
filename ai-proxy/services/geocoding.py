import os

import httpx

AMAP_GEO_URL = "https://restapi.amap.com/v3/geocode/geo"


async def geocode(address: str, city: str = "") -> dict | None:
    key = os.getenv("AMAP_API_KEY", "")
    if not key:
        return None
    params = {"key": key, "address": address, "city": city, "output": "json"}
    try:
        async with httpx.AsyncClient(timeout=10) as client:
            resp = await client.get(AMAP_GEO_URL, params=params)
            data = resp.json()
            if data.get("status") == "1" and data.get("geocodes"):
                loc = data["geocodes"][0].get("location", "")
                if loc and "," in loc:
                    lng, lat = loc.split(",")
                    return {"latitude": float(lat), "longitude": float(lng)}
    except Exception:
        pass
    return None
