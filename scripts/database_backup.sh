#!/bin/sh

DESTINATION="/mnt/dsashare/fireball/backup/database"
FILENAME="backup_`date +%Y-%m-%d_%H_%M_%S`.sql.gz"


if [ $# -eq 1 ]
  then
    FILENAME="${FILENAME}_$1"
fi



docker-compose exec -T db pg_dump -c -U blokat -d blokatugent | gzip > "${DESTINATION}/${FILENAME}"
