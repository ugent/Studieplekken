#!/bin/bash -

cd ../database || { echo "Could not change directory into ../database" ; exit ; }

perl create_database_properties.pl application_queries.sql application_columns.txt > out
mv out ../src/main/resources/database.properties
