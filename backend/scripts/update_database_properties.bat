cd ../database

perl application_queries_formatting.pl application_queries.sql > a
perl -ne "s/\s+$//; print \"$_\n\";" a > database_onlyQuerries.properties
DEL a
