# Анализ соответствия ТЗ

Дата проверки: 2026-05-18.

Источник требований: `тз.docx`.

## Что уже реализовано

- Multi-module структура есть: `core`, `user-module`, `test-module`, `ai-generator`, `result-module`, `analytics-module`, `notification-module`, `app`.
- Backend: Java 17, Spring Boot 3.2.5, Spring Security, Spring Data JPA, Redis dependency, PostgreSQL.
- Auth: регистрация, login, JWT access token, refresh token, logout/revoke, BCrypt strength 12.
- OAuth2 Google flow частично есть: security config, success controller, callback page. Нужны реальные `GOOGLE_CLIENT_ID` и `GOOGLE_CLIENT_SECRET`.
- RBAC есть для STUDENT / TEACHER / ADMIN через `@PreAuthorize`.
- ABAC частично есть: teacher/admin checks для своих тестов, student checks для своих session/results.
- AI generation есть: `PromptBuilder`, `LlmClient`, `MockLlmClient`, `OpenAIClientImpl`, `OllamaClientImpl`, `DeepSeekClient`, `QuestionParser`.
- Три типа вопросов поддержаны: single, multiple, open, mixed.
- Preview и редактирование вопросов есть в `create-test.html`; сохранение идет через `previewQuestions`.
- Redis cache-aside для генерации есть в `QuestionCacheService`.
- Retry/CircuitBreaker для LLM есть через Resilience4j annotations и config.
- LLM timeout 10 секунд есть в client calls.
- Prompt injection hardening частично есть: `PromptBuilder.sanitize(...)`.
- Prompt logs и retention cleanup есть.
- AI usage logging есть через `CostTracker` и `AiUsageLog`.
- Student flow есть: доступные тесты, start session, answer submit, finish, result/review.
- Таймер частично есть: backend закрывает просроченную session при submit/start/finish.
- Закрытые вопросы проверяются автоматически.
- Teacher statistics/rating есть.
- CSV export есть.
- PDF endpoint есть.
- Admin users, roles, audit log, AI usage UI/API есть.
- Audit filter/service есть.
- Rate limiting для `/api/tests/generate...` есть: 10 запросов/мин.
- Swagger/OpenAPI config есть.
- Actuator/Prometheus endpoints включены.
- Dockerfile, docker-compose, Kubernetes manifests, Prometheus/Grafana config есть.
- GitHub Actions CI есть: build/test, OWASP, Sonar, Docker build/push, deploy jobs.
- Postman collection, prompt README, user/system docs, k6/Playwright smoke scripts есть.
- Исправлен баг `Unfinished test session already exists` 500: повторный старт возвращает активную session.

## Частично реализовано

- ТЗ говорит, что вопросы не должны храниться как постоянный банк, а должны генерироваться динамически. В проекте вопросы сохраняются в таблице `questions` и привязаны к `test_configs`.
- ТЗ требует новый набор вопросов при каждой сдаче. Сейчас один сохраненный набор используется повторно для всех session одного test config.
- Участие по коду есть на backend (`/api/tests/code/{accessCode}`), но в UI student dashboard основной путь идет через список опубликованных тестов.
- Открытые вопросы можно оценить teacher вручную и есть endpoint `ai-grade`, но текущая AI-проверка является эвристикой по длине ответа, а не реальным LLM grading.
- PDF export endpoint есть, но это минимальная ручная PDF-строка, не полноценный iText/Apache POI export.
- AI usage dashboard есть как агрегат requests/tokens/cost, но Grafana dashboard с готовыми панелями по token/cost не описан полноценно.
- Monitoring stack есть, но production readiness зависит от запуска Docker/K8s и реальных secret/config.
- OAuth2 есть в коде, но без реальных Google credentials не проверяется end-to-end.
- HTTPS/TLS требования покрыты только инфраструктурными заготовками (`nginx`, `k8s/tls-secret.example.yaml`), не enforced локально.
- JavaDoc есть на уровне package-info, но не для всех public class/method.
- Тесты есть, но фактическое покрытие 80% не доказано: JaCoCo report skipped due to missing execution data в текущем запуске.

## Не реализовано или требует большого отдельного этапа

- Полная модель `generated_questions` с привязкой к `session_id`.
- Динамическая AI генерация вопросов при старте каждой student session.
- Сохранение результатов в отдельной таблице `results`; сейчас result вычисляется из `test_sessions`.
- Настоящая AI-проверка открытых ответов с prompt, LLM call, score/comment.
- Полный Grafana dashboard для response time, error rate, AI latency, token usage, cost.
- Полные security tests для всех ролей и ABAC сценариев.
- Полное E2E покрытие сценария "создать тест -> пройти -> получить результат".
- Реальный production deploy verification.
- Полная пользовательская инструкция на 10-14 страниц и системная инструкция на 5-8 страниц.
- GitHub Issues / Project board процесс нельзя проверить из локального репозитория.

## Что можно доделать самому следующим кодовым этапом

1. Сделать LLM-based grading для open answers:
   - расширить `LlmClient` методом оценки открытого ответа или добавить отдельный `LlmGradingClient`;
   - вернуть score/comment;
   - сохранить comment в отдельное поле или DTO.
2. Сделать session-scoped generated questions:
   - добавить entity/table `GeneratedQuestion`;
   - при `startTest` генерировать новый набор или копировать preview в session;
   - ответы привязать к session questions.
3. Улучшить PDF export:
   - подключить iText/OpenPDF или Apache PDFBox;
   - генерировать валидный отчет со статистикой и рейтингом.
4. Добавить security/integration tests:
   - роли STUDENT/TEACHER/ADMIN;
   - доступ только к своим тестам/session;
   - `/api/tests/generate` rate limit.
5. Довести JaCoCo до измеримого порога:
   - исправить execution data/report;
   - добавить threshold check.

## Проверка

- `mvn test` прошел успешно.
- Testcontainers smoke tests для PostgreSQL/Redis были skipped, потому что Docker daemon в текущей среде недоступен.

## Коммиты по ТЗ

ТЗ требует Conventional Commits. Уже создан первый отдельный коммит:

- `fix: resume existing in-progress test session`

Для этого отчета подходит:

- `docs: add tz gap analysis`
