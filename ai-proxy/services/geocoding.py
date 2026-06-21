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
                g = data["geocodes"][0]
                loc = g.get("location", "")
                lng = lat = None
                if loc and "," in loc:
                    lng_str, lat_str = loc.split(",")
                    lng = float(lng_str)
                    lat = float(lat_str)
                province = g.get("province") or ""
                city_name = g.get("city") or ""
                district = g.get("district") or ""
                township = g.get("township") or ""
                street = g.get("street") or ""
                street_number = g.get("streetNumber") or g.get("number") or ""

                if not city_name:
                    city_name = province

                detail_parts = [p for p in [township, street, street_number] if p]
                detail_addr = " ".join(detail_parts) if detail_parts else ""

                return {
                    "latitude": lat,
                    "longitude": lng,
                    "province": province,
                    "city": city_name,
                    "district": district,
                    "address": detail_addr or address,
                }
    except Exception:
        pass
    return None
