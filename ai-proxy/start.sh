#!/bin/bash
set -e

cd "$(dirname "$0")"

if [ ! -d "venv" ]; then
  python3 -m venv venv
fi

source venv/bin/activate
pip install -q -r requirements.txt

HOST="${AI_PROXY_HOST:-0.0.0.0}"
PORT="${AI_PROXY_PORT:-8000}"

exec uvicorn main:app --host "$HOST" --port "$PORT" --reload
