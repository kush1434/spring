#!/bin/bash

TOKEN=$(curl -s -X POST http://localhost:8585/authenticate \
  -H "Content-Type: application/json" \
  -d '{"uid": "alan", "password": "123Qwerty!"}' \
  -c - | grep jwt_java_spring | awk '{print $NF}')

curl -s -X POST http://localhost:8585/api/grade-frqs \
  --cookie "jwt_java_spring=$TOKEN"