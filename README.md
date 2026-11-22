# Last-Mile Tutor

> Find your weak spot in maths, and learn it in your own language, with no teacher needed.
## The problem
Many students have no teacher or tutor within reach. When they fall behind in maths, nobody notices where they are stuck, and most learning apps need money, a fast connection or good English.

## Our solution
Last-Mile Tutor finds what a learner is missing, teaches it step by step in English, Telugu or Hindi, and gives hints that guide without revealing the answer.

## Features
- **Quick sign-in:** name and email only; returning learners get a "Welcome back" message with their saved progress
- **Diagnostic quiz** across 5 topics: Algebra, Fractions, Geometry, Percentages, Probability
- **Weak-topic detection** and recommended next steps on the dashboard
- **Lessons in English, Telugu and Hindi**
- **Practice with hints** and a step-by-step explanation after each answer
- **Progress tracking:** score per topic, overall score and daily streak
- **Demo learner** button for a quick tour
- **Light and simple:** no heavy libraries or images, so it suits basic phones and slow connections

## Tech stack
Spring Boot 3 (Java 17), Spring Data JPA, H2 database, React (Vite). Optional LLM for lessons and hints, with built-in text as a fallback.

## Run locally
Prerequisites: Java 17, Maven 3.9+, Node.js 18+

**Backend**
```
cd backend
mvn spring-boot:run
```
Runs on http://localhost:8080. The database is seeded on first start.

**Frontend**
```
cd frontend
npm install
npm run dev
```
Opens on http://localhost:5173.

**Optional AI lessons:** set `LLM_API_KEY` before starting the backend (PowerShell: `$env:LLM_API_KEY = "your-key"`). Without it, built-in lessons and hints are used.

## Future work
Teacher or parent view, more subjects and questions, revision reminders, and offline mode.

