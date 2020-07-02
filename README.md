# blok2 - BlokAtUGent

## Structuur repository
### Broncode
De broncode bevindt zich in de map `blokAtUGent`. Deze map bevat twee projecten: 
* backend:  Dit is een Spring Boot project en bevat alle code van de REST Controllers en de dataklassen
* frontend:  Dit is een Angular project en voorziet de presentatie van het project

### Testen
#### Backend testen
De testen zijn te vinden in blokAtUGent\backend\src\test\java\be\ugent\blok2. Ze testen vooral de REST controllers en enkele aspecten van de dataklassen. Indien u een van de unit testen wil uitvoeren, klik met uw rechtermuisknop dan in IntelliJ op de map of test die u wilt runnen en selecteer dan 'Run (all) test(s)'.
#### Front-end testen
De testen voor de front-end zijn telkens de vinden in een .spec.ts bestand naast het bestand dat getest wordt. U kan de testen runnen door in commandline/terminal in de map blokAtUGent\frontend het command `ng test` uit te voeren, het resultaat komt dan te voorschijn in een apart venster.

### Verslag
In het mapje verslag is een pdf van het verslag van de bachelorproef te vinden.

### Diagrammen
In het mapje diagrammen zijn alle UML diagrammen te vinden. Dit zijn .vpp of .puml bestanden. De .vpp bestanden kunnen geopend worden in [Visual Paradigm](https://www.visual-paradigm.com/). De .puml bestanden zijn [PlantUML](https://plantuml.com/) bestanden en kan je openen in [Visual Studio Code](https://marketplace.visualstudio.com/items?itemName=jebbs.plantuml). Een overzicht met foto's van alle diagrammen is te zien op de [Wiki pagina](https://github.ugent.be/bp-2020/blok2/wiki/UML-diagrammen)

## Applicatie bekijken

### Online
De webapplicatie is terug te vinden op <https://blok2bp.ugent.be:8081/BlokAtUGent>. Opgelet! [UGent VPN](https://helpdesk.ugent.be/vpn/) is vereist om de website te kunnen bezoeken. Inloggen kan met onderstaande accounts. Elke account heeft andere rechten en toegang tot verschillende pagina's.

De wachtwoorden zijn allemaal 'Test1234'.  
Merk op dat dit geen CAS accounts zijn. U dient dus via "Login voor niet-UGent gebruikers" in te loggen.

|Email|Role|
|:-:|:-:|
|admin|admin|
|student|student|
|student-scanmedewerker|student-scanmedewerker|
|scanmedewerker|scanmedewerker|

### Lokaal runnen
#### Vereisten
* node
* angular
* java
* maven
* postgresql in combinatie met pgadmin
#### Met IntelliJ project runnen
Om de webapplicatie lokaal te lopen heb je een IDE nodig, bij voorkeur IntelliJ, en zijn volgende stappen vereist:
1. Om er zeker van te zijn dat met een proper project gewerkt wordt is het aangeraden dat `mvn clean` uitgevoerd wordt. Dit kan ofwel in de console (indien Maven globaal is geïnstalleerd op de computer) in de map `blokAtUGent`, ofwel met behulp van de lokale versie dat in IntelliJ meekomt.
2. De node modules moeten gedownload worden: ga hiervoor met de console naar de map `blokAtUGent/frontend` en voer `npm install` uit
3. Installeer het certificaat `\blokAtUGent\backend\src\main\resources\client.p12` zodat uw gebruik kan maken van HTTPS. Dit kan simpelweg door in File Explorer dubbel te klikken op het certificaat of de procedure volgen die beschreven staat in de documentatie van uw browser.
4. Het frontend project builden: blijf hiervoor in de map `blokAtUGent/frontend` en voer `ng build` uit
5. Start het backend project op: IntelliJ herkent dit normaal automatisch waardoor runnen kan door op het groen pijltje rechts bovenaan te drukken.
6. De webapplicatie is nu beschikbaar op <https://localhost:8080>  
  
Merk op dat indien u met de DBDao's wil werken, de eigenschap `spring.profiles.active` van het `application.properties` bestand (zit in `blok2/blokAtUGent/backend/src/main/resources` moet ingesteld staan op `db`. Daarvoor heeft moet u wel gebruikmaken van VPN omdat een connectie met de databank op de server zal worden gemaakt. Indien u met de DummyDao's wil werken, moet u diezelfde eigenschap op `dummy` zetten.

#### Voor developers
Om kleine aanpassingen in de frontend direct te zien kan het handig zijn om in in de frontend directory `ng build --watch` uit te voeren. De watch optie zorgt ervoor dat elke aanpassing direct wordt doorgevoerd net zoals bij `ng serve`

### Build deployment op Tomcat 9.0: richtlijnen
* Stap 1: clone project, bijvoorbeeld in een tijdelijk bestand
* Stap 2: in `frontend/src/app/environment/environment.ts` alle variabelen goed initialiseren
* Stap 3: in `blokAtUGent/backend`:
  * `mvn clean install -DskipTests`
* Stap 4: in `blokAtUGent/frontend`:
  *	`npm install`
  * `ng build --base-href=/BlokAtUGent/`
    * dankzij `--base-href` worden alle relatieve paden die gebruikt worden in de frontend, omgezet naar {domain}:{port}/BlokAtUGent/
    * op onze server wordt dit dus: blok2bp.ugent.be:8084/BlokAtUGent/
    * dankzij deze base-href kunnen urls op veilige basis relatief gebruikt worden in de frontend code
* Stap 5: war van backend naar `webapps/` van tomcat verhuizen
* Stap 6: war uitpakken in tomcat `webapps/` map (voer `$CATALINA_HOME/bin/catalina.sh start` uit in terminal, dan wordt war automatisch uitgepakt)
* Stap 7: de directory static van de frontend (dat in blokAtUGent/backend/target/classes/static zal staan door configuratie in angular.json bestand (voor gemak tijdens development)) naar `$CATALINA_HOME/webapps/BlokAtUGent/WEB-INF/classes/` kopiëren
* Stap 8: de application.properties file van backend project kopiëren naar `webapps/BlokAtUGent/WEB-INF/classes/`
* Stap 9: tomcat de webapp BlokAtUGent opnieuw laten laden (in manager-gui), anders server stoppen en weer starten

### Databank
Voor de installatie van postgresql kan je deze handleiding volgen: https://www.howtoforge.com/how-to-install-postgresql-and-pgadmin4-on-ubuntu-1804-lts/
Om deze databank te kunnen beheren maken wij gebruik van pgAdmin, deze kan je met volgende handledining installeren op je eigen pc om de remote server te bedienen: https://www.pgadmin.org/download/pgadmin-4-windows/
