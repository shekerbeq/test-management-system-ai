# Жүйелік нұсқаулық

## Local іске қосу

```bash
docker compose up -d postgres redis ollama
mvn -pl app -am spring-boot:run
```

Толық ТЗ ортасы:

```bash
docker compose up -d
```

Контейнерлер:

- `tms-app` - Spring Boot backend және static frontend
- `tms-postgres` - негізгі деректер қоры
- `tms-redis` - cache/rate-limit үшін дайын сервис
- `tms-ollama` - жергілікті LLM провайдері
- `tms-prometheus` - метрикалар жинау
- `tms-postgres-exporter` - PostgreSQL метрикаларын экспорттау
- `tms-redis-exporter` - Redis метрикаларын экспорттау
- `tms-grafana` - dashboard

## AI баптау

`.env` ішінде:

```env
LLM_PROVIDER=mock
DEEPSEEK_API_KEY=...
DEEPSEEK_MODEL=deepseek-chat
DEEPSEEK_URL=https://api.deepseek.com/v1/chat/completions
```

`mock` режимі интернетсіз жұмыс істейді. DeepSeek/OpenAI-compatible режимі сыртқы API кілтін талап етеді.

## Мониторинг

- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`
- Grafana: `http://localhost:3000`
- Prometheus: `http://localhost:9090`
- PostgreSQL exporter: `http://localhost:9187/metrics`
- Redis exporter: `http://localhost:9121/metrics`

## Kubernetes

```bash
kubectl apply -f k8s/
```

Production үшін `k8s/secret.example.yaml` файлын нақты secret мәндерімен ауыстыру керек.
