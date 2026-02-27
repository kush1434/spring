#!/bin/bash
set +H
source "$(dirname "$0")/../.env"

TOKEN=$(curl -s -X POST http://localhost:8585/authenticate \
  -H "Content-Type: application/json" \
  -d '{"uid": "'${ADMIN_UID}'", "password": "'${ADMIN_PASSWORD}'"}' \
  -c - | grep jwt_java_spring | awk '{print $NF}')

echo "Authenticated. Currently Grading..."

curl -s -X POST http://localhost:8585/api/grade-frqs \
  --cookie "jwt_java_spring=$TOKEN"