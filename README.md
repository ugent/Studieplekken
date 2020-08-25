# BlokAtUGent
Have a look at our [Wiki](https://github.ugent.be/bravdwal/dsa/wiki)

## Development

### Backend
**Requirements**
- Java 8
- Maven
- PostgreSQL 11

**Configure**  
1. PostgreSQL

Make sure that a user `postgres` with password `***REMOVED***` has following databases, running on (<b>default</b>) port 5432:
- `blokatugent`
- `blokatugent_test`

For both databases, run the sql queries in `scripts/database/blokatugent.sql`. For the `blokatugent`, run the script `scripts/database/frontend_development_setup.sql` to provide some dummy data.

**Commands**
```shell
mvn clean # Make sure the project is clean
mvn install # Install the dependencies
mvn spring-boot:run # Start the webserver
```

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
