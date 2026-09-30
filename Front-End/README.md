# Front-End

Plain HTML, CSS and JavaScript, with no build step. Chart.js is loaded from a CDN for the weekly chart.

- `login.html`, `login.css`, `login.js`: sign in, sign up, and the three-step password reset (request a code, enter it, choose a new password).
- `index.html`, `style.css`, `app.js`: the dashboard (summary cards, weekly grid, chart, today's habits, top habits) and the profile page.
- `logo.png`: the logo.

Serve the pages with `dev_server.py` from the project root. It serves them on port 3000 and forwards `/api` to the backend on port 8080. Opening the files straight from disk won't work. On `localhost` with any other port (e.g. Live Server), the pages call `http://localhost:8080/api` directly. On any other host they call `/api` on their own address.

On GitHub Pages (`*.github.io`) there is no backend: the pages run in an offline demo mode that keeps everything in `localStorage`. See "GitHub Pages runs in demo mode only" in the main README.

## What the browser keeps

Everything is in `localStorage`:

| Key | What it holds |
|---|---|
| `progressgrid_token` | The login token. Without it the dashboard sends you to the sign-in page, and a rejected token signs you out. |
| `username`, `email` | The signed-in account, shown in the sidebar and on the profile page. |
| `userFullName`, `userEmail`, `userMobile`, `userAvatar` | Changes made on the profile page. These only live in the browser for now and aren't sent to the backend. |

Logging out clears all of it.

## How the dashboard works

`app.js` loads your habits from `GET /api/habits`, which includes each habit's ticks, streaks and completion. When you tick a box it shows the tick straight away, sends it to the backend, then reloads the habits so the streaks and percentages match what the server worked out.

The weekly grid, the "Weekly Completion" card and the chart are worked out in the browser from the ticks. Only days from a habit's start date up to today can be ticked. The chart bars only count daily habits, since weekly habits aren't due on any particular day.
