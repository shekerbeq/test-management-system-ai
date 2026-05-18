#!/bin/sh
set -eu

read_secret() {
  env_name="$1"
  file_name="$2"
  if [ -f "$file_name" ]; then
    value="$(cat "$file_name")"
    export "$env_name=$value"
  fi
}

read_secret JWT_SECRET /run/secrets/jwt_secret
read_secret SPRING_DATASOURCE_PASSWORD /run/secrets/db_password
read_secret DEEPSEEK_API_KEY /run/secrets/deepseek_api_key
read_secret OPENAI_API_KEY /run/secrets/openai_api_key

exec java -jar /app/app.jar
