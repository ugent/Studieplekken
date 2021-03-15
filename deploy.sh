#!/bin/sh

echo "Door onbekende reden werkt de db deployen niet. We werken aan een oplossing. Gelieve de database container NIET te herdeployen." >&2

./scripts/database_backup.sh "deploy"

git stash || exit 1
git pull || exit 1
git stash pop || exit 1

docker-compose up --detach --build backend
docker-compose up --detach --build frontend
docker-compose ps
