run:
	SPRING_PROFILES_ACTIVE=local ./gradlew bootRunDev
reset-db:
	./gradlew flywayClean
	./gradlew flywayMigrate
	psql postgres -h 127.0.0.1 -p 5432 -d blokatugent -f database/seed.sql
reset-db-old:
	psql postgres -h 127.0.0.1 -p 5432 -d blokatugent -f database/create_schema.sql
	psql postgres -h 127.0.0.1 -p 5432 -d blokatugent -f database/seed.sql

reset-db-test:
	psql postgres -h 127.0.0.1 -p 5432 -d blokatugent_test -f database/create_schema.sql
	psql postgres -h 127.0.0.1 -p 5432 -d blokatugent_test -f database/seed.sql
reset-schemas:
	psql postgres -h 127.0.0.1 -p 5432 -d blokatugent -f database/reset_schema.sql
	psql postgres -h 127.0.0.1 -p 5432 -d blokatugent_test -f database/reset_schema.sql
seed:
	psql postgres -h 127.0.0.1 -p 5432 -d blokatugent -f database/seed.sql
