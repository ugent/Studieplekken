#!/bin/sh

docker-compose exec db pg_dump -c -U blokat -d blokatugent > dump_`date +%Y-%m-%d"_"%H_%M_%S`.sql
