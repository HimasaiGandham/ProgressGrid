# Back-End

The ProgressGrid API: Spring Boot 3.1 on Java 17, with MySQL through Spring Data JPA.

## Layout

```
src/main/java/com/progressgrid/api/
  controller/   AuthController (sign-up, login, password reset) and HabitController (habits and ticks)
  service/      AuthService, HabitService (streaks and completion), OtpService (reset codes), EmailService
  security/     TokenService issues and checks login tokens; AuthConfig requires one on /api/habits
  model/        JPA entities: User, Habit, HabitCategory, HabitCompletion
  repository/   Spring Data repositories
  dto/          Request and response objects
```

## Signing in

Login and signup return a JWT signed with `JWT_SECRET`. Every request to `/api/habits` has to send it as `Authorization: Bearer <token>`. The user id is taken from the token, never from the request, and a habit that belongs to someone else is reported as not found.

Passwords are stored as BCrypt hashes. Older accounts that still hold a plain-text password, like the seed user, can sign in once with it, and it's hashed on the spot.

Password reset works in three calls: `send-otp` emails a 6-digit code, `verify-otp` checks it, and `reset-password` sets the new password. The code expires after 10 minutes, and 5 wrong guesses discard it. `reset-password` only accepts a code that has already passed `verify-otp`.

## How habits are scored

Daily habits are counted in days. Weekly habits are counted in Monday-to-Sunday weeks, and a week counts as done if it has at least one tick.

The current streak is the run of days (or weeks) done up to today. If today isn't ticked yet, a run that ended yesterday still counts, so the streak doesn't reset in the morning. The best streak is the longest run so far. Completion is the number of days (or weeks) done divided by the number since the start date, capped at 100%.

A tick has to fall between the habit's start date and today; anything else gets a 400.

## Running

```bash
mvn spring-boot:run
```

It starts on port 8080 and needs the MySQL database from `Data-Base/schema.sql`. Settings are in `src/main/resources/application.properties`, and the environment variables it reads are listed in the main README.

## Tests

```bash
mvn test
```

`AuthSecurityTests` covers sign-in, hashing, sessions, ownership and password reset. `HabitRulesTests` covers habit input, which days can be ticked, and streak and completion scoring. Both run against in-memory H2, set up in `src/test/resources/application-test.properties`.
