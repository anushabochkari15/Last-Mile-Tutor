# Maths Practice

Spring Boot 3 (Java 17) + JPA + H2 backend, React (Vite) frontend. Learners take diagnostic and practice quizzes across 5 maths topics (Algebra, Fractions, Geometry, Percentages, Probability), track mastery and streaks, and get lessons and hints.

## Prerequisites

- Java 17
- Maven 3.9+
- Node.js 18+ and npm

## Run the backend

```bash
cd backend
mvn spring-boot:run
```

The backend runs on http://localhost:8080. The database is seeded with 4 questions per topic on first start. The H2 file lives in `backend/data/` and is created automatically.

## Optional: LLM lessons and hints

Set the environment variable `LLM_API_KEY` before starting the backend. In PowerShell:

```powershell
$env:LLM_API_KEY = "your-key"
```

Without it, lessons and hints fall back to the built-in text.

## Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Opens on http://localhost:5173 (the backend allows this origin via CORS).

## H2 console

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/mathsdb`
- User: `sa`
- Password: (empty)

## Build

```bash
mvn -f backend/pom.xml package
npm run build   # run in frontend/
```
