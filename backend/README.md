













# Backend
**Requirements**
- Java 8
- PostgreSQL 12

**Configure**  
***PostgreSQL***

Make sure that a user `postgres` with password `postgres` has following databases, running on (<b>default</b>) port 5432:
- `blokatugent`
- `blokatugent_test`

For the `blokatugent` database, run the script `scripts/database/seed.sql` to provide some dummy data.

Configure your correct database configuration in de `application.yml`properties file in the resources folder.

***Self-signed certificate for HTTPS***

To be able to use CAS in development, the callback URL that the UGent CAS Server uses, <i>has</i> to be `https://localhost:8080/**`.

Therefore, we need to set up HTTPS within development. This is done by creating a self-signed certificate. The self signed certificate is located in a keystore at `backend/src/main/resources/keystore/blokat.p12`. When the server is started, the self signed certificate should be found by the server.

Not just the backend is secured with HTTPS, but the frontend as well. This is done by setting the `start` script which is defined in `package.json` from `ng serve` to `ng serve --ssl`. When starting the server, ignore the security warning that the browser will give.

You can create a certificate by running following command. Make sure to use `***REMOVED***` as the password for the keystore, or if another password is used, change the file `application.yml`.
``` shell script
keytool -genkeypair -alias blokat -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore blokat.p12 -validity 3650
```


***Keystore for SAML***

The SAML 2.0 protocol requires a keystore. There already is one at `backend/src/main/resources/keystore/samlKeystore.jks`.

In case you would need to re-generate this keystore, following commands were used to generate it. The same credentials were used as the other keystore above.

```
keytool -genkeypair -alias blokat -keyalg RSA -keysize 2048 -storetype JKS -keystore samlKeystore.jks -validity 3650
```

**Attention!** Arteveldehogeschool uses signed metadata for their SAML endpoint. Therefore, we have to add the certificate to our keystore to be able to validate their metadata file.
It is important that you do not reformat their metadata file (`backend/src/main/resources/saml/metadata/sso-artevelde.xml`) because when you do, the checksum will no longer match and the application will be unable to validate the metadata resulting in not being able to use their IdP.

To add their certificate to our keystore `samlKeystore.jks` we run following command (this is already done for the current file but you will have to redo this in case you re-generate the keystore).
Before we are able to run following command, we should extract their certificate and place it in a separate file between begin/end certificate tags. This is already done in `backend/src/main/resources/keystore/arteveldeSignature.cer`. The value in `Signature.KeyInfo.X509Data.X509Certificate` from their metadata file is used.

```
keytool -import -alias adfscert -file arteveldeSignature.cer -keystore samlKeystore.jks
```


**Commands**
```shell script
./gradlew clean bootRunDev
```
or for windows
```shell script
gradlew.bat clean bootRunDev
```