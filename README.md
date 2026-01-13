
# Secure Notes API (Kotlin + Spring Boot + SQLite)

Implements Labs 10–12 requirements:
- HTTP REST API
- SQLite + Flyway migrations
- Authentication (JWT)
- Authorization (user owns notes)
- Validation, bcrypt passwords
- Swagger/OpenAPI docs

## Run
```bash
cp .env.example .env
./gradlew bootRun
```

## API Docs
- Swagger UI: http://localhost:8080/docs
## UI
- Open UI: http://localhost:8080/
- Open UI: http://localhost:8080/index.html

## Endpoints
- POST /auth/register
- POST /auth/login
- GET /api/notes
- POST /api/notes

JWT token is returned on login and must be sent as `Authorization: Bearer <token>`.
