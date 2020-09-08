#!/bin/bash -

cd ../database || { echo "Could not change directory into ../database" ; exit ; }

perl application_queries_formatting.pl application_queries.sql ../src/main/resources/database.properties > out
mv out ../src/main/resources/database.properties
