#!/bin/sh

./scripts/database_backup.sh "deploy"

docker compose up --detach --build
docker compose ps
