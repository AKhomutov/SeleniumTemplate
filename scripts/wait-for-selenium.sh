#!/usr/bin/env bash
set -euo pipefail

SELENIUM_URL="${SELENIUM_URL:-http://localhost:4444}"
MAX_ATTEMPTS="${MAX_ATTEMPTS:-60}"
SLEEP_SECONDS="${SLEEP_SECONDS:-2}"

status_url="${SELENIUM_URL%/}/status"

for attempt in $(seq 1 "$MAX_ATTEMPTS"); do
  if response="$(curl -sf "$status_url" 2>/dev/null)" && [[ "$response" == *'"ready": true'* ]]; then
    echo "Selenium is ready at ${SELENIUM_URL} (attempt ${attempt}/${MAX_ATTEMPTS})"
    exit 0
  fi
  echo "Waiting for Selenium at ${status_url} (${attempt}/${MAX_ATTEMPTS})..."
  sleep "$SLEEP_SECONDS"
done

echo "Selenium did not become ready at ${status_url} within $((MAX_ATTEMPTS * SLEEP_SECONDS))s" >&2
exit 1
