#!/bin/sh

./scripts/database_backup.sh "deploy"

docker compose -f docker-compose.yml -f docker-compose.override.yml -f docker-compose-staging.yml -f docker-compose-staging.override.yml up --detach --build
docker compose ps
