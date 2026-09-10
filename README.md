# ProgressGrid

A personal habit tracker. You define the activities you want to do, tick them off on a
weekly grid, and the dashboard shows how much of your plan you actually completed —
daily, weekly and monthly.

Single user per account: everything you create is yours alone, and no account can see
or change another's data.

## Features

- **Weekly grid** — one row per activity, one column per weekday. Tick a box to record a
  completion; past ticks are loaded back when you return.
- **Daily / weekly / monthly progress** — a doughnut chart for today, a bar chart for the
  week, and a progress bar for the month.
- **Cadence-aware scoring** — a `DAILY` activity is expected once a day, a `WEEKLY` one
  once a week, and the percentages respect that difference.
- **Activity management** — create, rename, re-cadence, and delete activities.
- **Notifications** — you get one when you clear every daily activity for a date. Click a
  notification to mark it read.

## Tech stack

| Layer | What |
|---|---|
| Frontend | HTML, CSS, vanilla JavaScript, Chart.js (CDN) — no build step |
| Backend | Java 17, Spring Boot 4.1.1, Spring Security, JWT (jjwt) |
| Database | MySQL 8 (H2 in-memory for tests) |

## Running it

**1. Create the database and user**

```bash
mysql -u root -p < database/database_setup.sql
```

Tables are created automatically on first start (`spring.jpa.hibernate.ddl-auto=update`).
`database/schema.sql` documents the same schema if you prefer to create it by hand.

**2. Start the backend**

```bash
cd backend && ./mvnw spring-boot:run
```

It listens on `http://localhost:8080`.

**3. Open the frontend**

Open `frontend/index.html` in a browser. It calls `http://localhost:8080/api` directly, so
no web server is needed.

### Configuration

Everything has a working local default; override with environment variables in production.

| Variable | Default | Notes |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/progressgrid_db?...` | |
| `DB_USERNAME` | `pg_user` | |
| `DB_PASSWORD` | `password123` | The local dev password from `database_setup.sql`. Change it anywhere real. |
| `JWT_SECRET` | *(unset)* | **Set this in production.** Unset means a random key is generated at startup, so every token dies on restart. Must be at least 64 characters (HS512). |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | |

## API

All endpoints except `/api/auth/**` require `Authorization: Bearer <token>`.

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/auth/register` | Create an account (`name`, `email`, `password`) |
| `POST` | `/api/auth/login` | Exchange credentials for a JWT (`accessToken`) |
| `GET` | `/api/activities` | List your activities |
| `POST` | `/api/activities` | Create one (`activityName`, `description`, `frequency`) |
| `PUT` | `/api/activities/{id}` | Update one |
| `DELETE` | `/api/activities/{id}` | Delete one, and its completion history |
| `GET` | `/api/activities/completions?start=&end=` | Ticks in a date range, for the grid |
| `POST` | `/api/activities/{id}/complete?date=` | Tick a day (defaults to today) |
| `POST` | `/api/activities/{id}/uncomplete?date=` | Untick a day |
| `GET` | `/api/progress/daily` | Today's completion |
| `GET` | `/api/progress/weekly` | This week, plus a per-weekday breakdown |
| `GET` | `/api/progress/monthly` | This month to date |
| `GET` | `/api/notifications` | Your notifications, newest first |
| `PUT` | `/api/notifications/{id}/read` | Mark one read |

Dates are ISO `yyyy-MM-dd`. Weeks run Monday to Sunday.

### How progress is calculated

`percentage = completed / planned`, capped at 100. What counts as *planned* depends on cadence:

- **Daily** — planned is your `DAILY` activity count. Weekly activities are not due on any
  particular day, so they are excluded from the daily score.
- **Weekly** — planned is `daily activities × days elapsed this week + weekly activities`.
- **Monthly** — planned is `daily activities × days elapsed + weekly activities × weeks elapsed`.

## Project layout

```
backend/     Spring Boot API - controllers, JPA entities, repositories, JWT security
database/    MySQL setup script and schema reference
frontend/    index.html plus css/ and js/ - open it directly, no build
```

## Tests

```bash
cd backend && ./mvnw test
```

13 API tests run against in-memory H2, so no local MySQL is needed. They cover the auth
flow, per-user isolation, grid persistence, cadence-aware progress maths, and notifications.
