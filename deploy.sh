#!/bin/sh

./scripts/database_backup.sh "deploy"

git stash || exit 1
git pull || exit 1
git stash pop || exit 1

docker-compose up --detach --build
docker-compose ps

