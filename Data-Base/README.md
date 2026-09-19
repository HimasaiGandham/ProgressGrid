# Data-Base

MySQL scripts for ProgressGrid.

- `schema.sql` creates the `progressgrid_db` database, the `pg_user` account the backend signs in with, and the four tables. It's safe to run again, since nothing gets dropped.
- `seed.sql` adds sample data: an `admin` user (password `dummy_hash`), five categories, five habits, and ticks from the first week of September 2026. Run it once, on a fresh database.

Run both as the MySQL root user:

```bash
mysql -u root -p < schema.sql
mysql -u root -p < seed.sql
```

The backend also creates any missing tables and columns when it starts (`spring.jpa.hibernate.ddl-auto=update`), so the table definitions in `schema.sql` are mostly there as a reference. The database and user still have to exist, though.

If you change the database name, user or password, change them in `Back-End/src/main/resources/application.properties` too.

## Tables

| Table | What's in it |
|---|---|
| `users` | Accounts. `password_hash` holds a BCrypt hash. |
| `habit_categories` | Category name and colour. The app creates a new one if a habit uses a name that isn't there yet. |
| `habits` | Each habit belongs to one user. `frequency` is `Daily` or `Weekly`, and `start_date` is the first day that can be ticked. |
| `habit_completions` | One row per ticked day. Unticking deletes the row. |
