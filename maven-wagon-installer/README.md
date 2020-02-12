# Introduction
This project builds a ZIP package that contains the artifact-wagon plugin and scripts for a user to install the plugin in their local Maven repository.
This will boostrap the Maven environment so Maven can use OLP `credentials.peroperties` to download dependencies from the OLP Artifact Service.

# Building
```
mvn clean package
```

# Testing
1. Remove (rename) your `.m2/settings.xml` file to ensure you do not have credentials nor configuration to use repo.platform.here.com.
2. Delete ALL ARTIFACTS in `.m2/repository/com/here` e.g. rm -rf `<home>/.m2/repository/com/here`
3. Ensure you have correct .here/credentials.properties file for **Production** HERE account. The pom-test.xml file is configured for production Artifact Service.
4. Unzip `artifact-wagon-install-<version>.zip`
5. Change directory to `artifact-wagon-install-<version>`
6. Run `install.sh` for Mac/Linux or `INSTALL.BAT` for Windows
7. Run `mvn -U --file pom-test.xml clean dependency:resolve` to test. Your installation is successful if there are no errors.

# Deploying
1. Ensure you have correct credentials.properties in .here directory corresponding to the target deployment environment
2. Follow instructions https://confluence.in.here.com/display/OLP/Artifact+Deployment

# Portal Changes
After the installer has been deployed to SIT, PROD, China SIT and China PROD environments - Portal team should update the related
configuration on their side. A ticket should be raised for that team.
