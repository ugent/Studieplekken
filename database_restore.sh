#!/bin/sh

if [ $# -eq 0 ]
  then
    echo "Please specify the pgdump file"
    exit 1
fi

[ ! -f $1 ] && echo "Dump file does not exist" && exit 1

# it only drops tables in the database dump, existing tables will keep existing.
# 	-> recreate the docker volume or database from scratch
docker-compose rm -f -s -v db
docker-compose up -d db

./wait-for-postgres.sh localhost

cat $1 | docker-compose exec -T db psql -U blokat -d blokatugent

# the dump could be from an old migration
# 	-> migrate the database = restart the webserver 
docker-compose restart backend


