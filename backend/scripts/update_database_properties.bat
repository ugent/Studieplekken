cd ../database

perl create_database_properties.pl application_queries.sql application_columns.txt > out
perl -e "while (<>) { print; }" out > ../src/main/resources/database.properties

DEL out
