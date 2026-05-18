# ТЗ сәйкестік есебі

Бұл файл жобадағы негізгі ТЗ талаптарының ағымдағы орындалуын қысқа түрде көрсетеді.

## Жабылған негізгі бөліктер

- Multi-module құрылым: `core`, `user-module`, `test-module`, `ai-generator`, `result-module`, `analytics-module`, `notification-module`, `app`.
- JWT login, refresh token, logout/revoke, OAuth2 Google callback endpoint және security конфигурациясы.
- RBAC және ABAC: оқытушы өз тесттерін ғана өзгерте/экспорттай/көре алады, студент өз сессиясын ғана қолданады.
- Rate limiting, brute-force lockout және audit log filter/service.
- LLM провайдерлері: DeepSeek/OpenAI-compatible, OpenAI, Ollama, mock.
- Resilience4j retry/circuit breaker және Redis cache-aside.
- Prompt template, prompt log және 30 күн retention cleanup.
- CSV/PDF export, рейтинг, статистика, нәтижелер модулі.
- Token usage API және analytics dashboard API.
- Docker compose кеңейтілді: `app`, `postgres`, `redis`, `ollama`, `prometheus`, `grafana`, `mailpit`, `nginx`.
- Kubernetes deployment/service/ingress және TLS secret example.
- GitHub Actions: Maven test, SonarQube scan, DockerHub push, dev/prod deploy jobs secret бар кезде іске қосылады.
- Postman collection, prompt README, user/system guide, k6 және Playwright smoke scripts.
- Testcontainers және WireMock тесттері қосылды; Docker жоқ ортада Testcontainers skip болады.

## Қосу/іске қосу үшін керек сыртқы құпиялар

- Google OAuth2 үшін `GOOGLE_CLIENT_ID` және `GOOGLE_CLIENT_SECRET`.
- AI production үшін `DEEPSEEK_API_KEY` немесе `OPENAI_API_KEY`.
- CI үшін `SONAR_TOKEN`, `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `KUBE_CONFIG`.
- TLS үшін нақты certificate/key немесе Kubernetes cert-manager.

## Толық дәлелдеу үшін қалған тексерулер

- `mvn test` өтеді, бірақ нақты 80% coverage жобада әлі тест санымен дәлелденбейді.
- Docker daemon осы ортада қолжетімсіз болғандықтан Testcontainers smoke тесттері skip болды.
- Production OAuth2 және Kubernetes deploy нақты cloud/secret ортада қолмен тексерілуі керек.
- JavaDoc package деңгейінде қосылды, бірақ әр public class/method үшін толық академиялық JavaDoc әлі кеңейтілуі мүмкін.
