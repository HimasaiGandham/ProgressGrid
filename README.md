# ProgressGrid

ProgressGrid is a habit tracker. You list the habits you want to keep up with, tick them off day by day on a weekly grid, and the dashboard shows how consistent you've been: streaks, completion rates and a chart of the week.

Each account only sees its own habits. The backend is a Spring Boot API backed by MySQL, and the frontend is plain HTML, CSS and JavaScript with no build step.

## What you can do

- Sign up, then sign in with your username or email. If you forget your password, you can get a 6-digit code by email and set a new one.
- Add a habit with a name, a category (Health, Fitness, Study, Work or Personal), how often you want to do it (daily or weekly) and a start date.
- Tick or untick days on the weekly grid, from the habit's start date up to today, and move back and forward a week at a time.
- See your total habits, your longest current and best streak, this week's completion so far, and overall completion since each habit started.
- Check the weekly progress chart. Each day's bar shows how many of your daily habits you got done, coloured from red up to green.
- See today's habits in one list and the habits with your best completion rate in another.
- Set your name, email, phone number and a profile photo on the profile page.

Weekly habits are counted in weeks rather than days: any tick between Monday and Sunday completes that week, and their streaks are in weeks too.

## Running it locally

You'll need Java 17 or newer, Maven, MySQL 8, and Python 3 for the small dev server.

### 1. Database

Create the database and the user the backend connects with (run it as the MySQL root user):

```bash
mysql -u root -p < Data-Base/schema.sql
```

For sample data, run `Data-Base/seed.sql` the same way. It adds an `admin` user (password `dummy_hash`), the five categories, five habits and some ticked days. See [Data-Base/README.md](Data-Base/README.md) for details.

### 2. Backend and frontend

On Windows, double-click `run-app.bat`. It starts the dev server in its own window, opens the browser and then starts the backend.

Or start them yourself in two terminals:

```bash
cd Back-End
mvn spring-boot:run
```

```bash
python dev_server.py
```

Then open http://localhost:3000. The backend runs on port 8080, and the dev server serves the `Front-End` folder and forwards anything under `/api` to it, so use the dev server instead of opening the HTML files directly.

If the dashboard says it couldn't load your habits, the backend isn't running or can't reach MySQL.

## Configuration

Settings live in `Back-End/src/main/resources/application.properties`. The database login is set there directly. These can be set through environment variables instead:

| Variable | What it's for |
|---|---|
| `JWT_SECRET` | Signs login sessions. Use at least 32 random characters in production. If it isn't set, a random key is generated on startup and everyone is signed out whenever the backend restarts. |
| `JWT_EXPIRATION_MS` | How long a session lasts. Defaults to 24 hours. |
| `RESEND_API_KEY`, `RESEND_FROM_EMAIL` | Sends password reset emails through Resend. |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP, used if Resend isn't set up. |

If no email option is set up, or sending fails, the reset code is printed in the backend console instead, which is handy while testing.

## API

Everything under `/api/habits` needs the token you get back from login or signup, sent as `Authorization: Bearer <token>`. The `/api/auth` endpoints report errors as `{"status": "error", "message": "..."}`.

| Method | Path | |
|---|---|---|
| POST | `/api/auth/signup` | Create an account |
| POST | `/api/auth/login` | Sign in and get a token |
| POST | `/api/auth/forgot-password/send-otp` | Email a reset code |
| POST | `/api/auth/forgot-password/verify-otp` | Check the code |
| POST | `/api/auth/reset-password` | Set a new password once the code is verified |
| GET | `/api/habits` | Your habits, with streaks and completion stats |
| POST | `/api/habits` | Add a habit |
| POST | `/api/habits/{id}/complete` | Tick or untick a day, e.g. `{"date": "2026-09-19", "completed": true}` |
| DELETE | `/api/habits/{id}` | Delete a habit |

Reset codes are 6 digits, expire after 10 minutes, and stop working after 5 wrong guesses.

## Tests

```bash
cd Back-End
mvn test
```

The 12 tests use an in-memory H2 database, so MySQL doesn't need to be running. They cover signing up and logging in, password hashing, the reset flow, that one user can't see or change another user's habits, which days can be ticked, and how streaks and completion are worked out.

## Project layout

```
Back-End/       Spring Boot API               (see Back-End/README.md)
Front-End/      Login page and dashboard      (see Front-End/README.md)
Data-Base/      MySQL setup and sample data   (see Data-Base/README.md)
dev_server.py   Serves Front-End on port 3000 and forwards /api to the backend
run-app.bat     Starts everything on Windows
```

## Not done yet

- My Habits, Statistics and Calendar in the sidebar just show the dashboard for now.
- You can't edit or delete a habit from the site yet. The delete endpoint exists but nothing on the page calls it.
- Profile changes are only saved in your browser. Changing your username there doesn't change the one you sign in with.
- Asking for a reset code for an account that doesn't exist says so, which lets someone check whether an account exists.
- "Today" is decided by the server's clock, so users in a very different timezone from the server may find the grid a day off.
