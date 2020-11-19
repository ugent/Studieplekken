#!/bin/bash
scriptpath=$(dirname "$0")
currpath=$(pwd)

cd $scriptpath
bash update_database_properties.sh
psql -U postgres -d blokatugent -a -f ../database/reset_schema.sql
cd ..
bash ./gradlew flywayMigrate
psql -U postgres -d blokatugent -a -f database/seed.sql
cd $currpath