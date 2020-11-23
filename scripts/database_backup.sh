#!/bin/sh

DESTINATION="/mnt/dsashare/fireball/backup/database"
FILENAME="backup_`date +%Y-%m-%d"_"%H_%M_%S`.sql.gz"

docker-compose exec db pg_dump -c -U blokat -d blokatugent | gzip > "${DESTINATION}/${FILENAME}"
