cd ../database

perl application_queries_formatting.pl application_queries.sql ../src/main/resources/database.properties > out
perl -e "while (<>) { print; }" out > ../src/main/resources/database.properties

DEL out
