# Docker secrets

Copy each `*.example` file to the same name without `.example` before running the full Docker Compose stack.

Required files:

- `jwt_secret.txt`
- `db_password.txt`
- `deepseek_api_key.txt`
- `openai_api_key.txt`

For local mock mode, `deepseek_api_key.txt` and `openai_api_key.txt` may contain any placeholder value.
