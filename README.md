# BlokAtUGent
Have a look at our [Wiki](https://github.ugent.be/bravdwal/dsa/wiki)

## Development

### Backend
**Requirements**
- java
- maven
- postgresql

**Configure**

- Install the certificate at `/blokAtUGent/backend/src/main/resources/client.p12`. Do this following the instructions of your browser for custom certificates.


**Commands**
```shell
mvn clean # Make sure the project is clean
mvn install # Install the dependencies
mvn spring-boot:run # Start the webserver
```

### Frontend
**Requirements**
- node
- angular

**Configure**
**Commands**
```shell
npm install # Install the dependencies
ng build
```
