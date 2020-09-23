# BlokAtUGent
Have a look at our [Wiki](https://github.ugent.be/bravdwal/dsa/wiki)

## Development

### Backend
**Requirements**
- Java 8
- PostgreSQL 12

**Configure**  
1. PostgreSQL

Make sure that a user `postgres` with password `postgres` has following databases, running on (<b>default</b>) port 5432:
- `blokatugent`
- `blokatugent_test`

For both databases, run the sql queries in `scripts/database/create_schema.sql`. For the `blokatugent`, run the script `scripts/database/seed.sql` to provide some dummy data.

**Commands**
```shell script
./gradlew clean bootRunDev
```
or for windows
```shell script
gradlew.bat clean bootRunDev
```

**Remarks**
* `database.properties`

This file contains all queries that the application uses, as well as all names of the columns. But, <b>do not change this file</b>. The file will be overwritten if someone executes `update_database_properties.bat` or `update_database_properties.sh`.

For development convenience of SQL queries, the application queries are provided in the file `backend/database/application_queries.sql`. Please do read the explanatory comment before writing any queries.

If a table has been added, or a column name has changed, update the file `backend/database/application_columns.txt`.

When all updates have been made, execute one of the scripts `update_database_properties.bat` or `update_database_properties.sh`, depending on the OS you are using. Now, the `database.properties` file should be updated accordingly.



### Frontend
**Requirements**
- Node
- Angular

**Configure**
**Commands**
```shell
npm install # Install the dependencies
ng serve -o # Compile source files and open project in browser
```
