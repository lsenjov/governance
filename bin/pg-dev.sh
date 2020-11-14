## Run a postgres db locally
# https://hub.docker.com/r/bitnami/postgresql

docker run -d \
  --name govern_psql \
  -e POSTGRES_PASSWORD=Pass2020! \
  -v "${PWD}/pg_persist:/var/lib/postgresql/data" \
  -p 5432:5432 \
  postgres || docker start govern_psql

