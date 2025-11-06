# db-migrations/Dockerfile
FROM postgres:16-alpine

# Copy migration file(s)
COPY migrate.sql /migrations/migrate.sql

# Run on container start
# We rely on compose dependency 'service_healthy' so DB is up; still use pg_isready as safety.
CMD sh -c '\
  echo "Waiting for DB..." && \
  until pg_isready -h "$PGHOST" -p "$PGPORT" -d "$PGDATABASE" -U "$PGUSER"; do sleep 1; done && \
  echo "Applying migrations..." && \
  psql -v ON_ERROR_STOP=1 -f /migrations/migrate.sql && \
  echo "Migrations complete." \
'
